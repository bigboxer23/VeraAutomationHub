package com.bigboxer23.lights.controllers.aggregate;

import com.bigboxer23.govee.IHumidifierCommands;
import com.bigboxer23.lights.controllers.govee.GoveeHumidifierController;
import com.bigboxer23.lights.controllers.govee.HumidifierCluster;
import com.bigboxer23.lights.controllers.switchbot.SwitchBotController;
import com.bigboxer23.switch_bot.IDeviceCommands;
import com.bigboxer23.utils.command.RetryingCommand;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing a system of devices from different ecosystems to do a refill of a
 * humidifier on command
 */
@RestController
public class HumiditySystemController implements InitializingBean, IHumidityEventHandler {
	private static final Logger logger = LoggerFactory.getLogger(HumiditySystemController.class);

	private final SwitchBotController switchbotController;

	private final GoveeHumidifierController goveeController;

	private final Map<String, HumidifierCluster> humidifierMap = new HashMap<>();

	@Value("${humidfier_to_pump_map}")
	private String MAP_KEY;

	public HumiditySystemController(
			SwitchBotController switchbotController, GoveeHumidifierController goveeController) {
		this.switchbotController = switchbotController;
		this.goveeController = goveeController;
		this.goveeController.addHandler(this);
	}

	@Override
	public void afterPropertiesSet() {
		List<HumidifierCluster> humidityClusters =
				new GsonBuilder().create().fromJson(MAP_KEY, new TypeToken<List<HumidifierCluster>>() {}.getType());
		humidityClusters.forEach(cluster -> humidifierMap.put(cluster.getHumidifier(), cluster));
	}

	@GetMapping(
			value = "/S/humidifier/{deviceId}/{deviceName}/{deviceModel}",
			produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			summary = "trigger humidifier refill loop",
			description = "calls switchbot to start pump, turn humidifier on/off (via switchbot plug),"
					+ " then humidifier on")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_BAD_REQUEST + "", description = "Bad request"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public void triggerHumidifierRefillAndRestart(
			@Parameter(description = "deviceId of humidifier") @PathVariable(value = "deviceId") String deviceId,
			@Parameter(description = "deviceName (for logging only)") @PathVariable(value = "deviceName")
					String deviceName,
			@Parameter(description = "deviceModel") @PathVariable(value = "deviceModel") String deviceModel) {
		logger.info(deviceId + " requested via web api");
		outOfWaterEvent(deviceId, deviceName, deviceModel);
	}

	@Scheduled(fixedDelay = 600000) // 10min
	public void manualCheck() {
		logger.info("checking humidity");
		humidifierMap.values().stream()
				.filter(cluster -> !StringUtils.isEmpty(cluster.getHumiditySensor()))
				.forEach(cluster -> {
					try {
						int humidity = RetryingCommand.execute(
								() -> switchbotController
										.getSwitchbotAPI()
										.getDeviceApi()
										.getDeviceStatus(cluster.getHumiditySensor())
										.getHumidity(),
								cluster.getHumiditySensor());
						logger.info(cluster.getHumiditySensor() + " humidity " + humidity);
						if (humidity < 55) {
							logger.info("low humidity detected");
							float watts = RetryingCommand.execute(
									() -> switchbotController
											.getSwitchbotAPI()
											.getDeviceApi()
											.getDeviceStatus(cluster.getOutlet())
											.getWatts(),
									cluster.getOutlet());
							if (watts > 3) {
								logger.info("humidifier is running, detected wattage: " + watts);
								return;
							}
							new Thread(new RefillAction(
											cluster.getPump(),
											cluster.getHumidifierModel(),
											cluster.getHumidifier(),
											cluster.getOutlet()))
									.start();
						}
					} catch (IOException e) {
						logger.error("error ", e);
					}
				});
	}

	@Override
	public void outOfWaterEvent(String deviceId, String deviceName, String deviceModel) {
		HumidifierCluster cluster = humidifierMap.get(deviceId);
		if (cluster == null) {
			logger.warn("No cluster for " + deviceId);
			return;
		}
		new Thread(new RefillAction(cluster.getPump(), deviceModel, deviceId, cluster.getOutlet())).start();
	}

	private class RefillAction implements Runnable {
		private final String pumpId;
		private final String humidifierModel;
		private final String humidifierId;

		private final String humidifierOutletId;

		public RefillAction(String pumpId, String humidifierModel, String humidifierId, String humidifierOutletId) {
			this.pumpId = pumpId;
			this.humidifierModel = humidifierModel;
			this.humidifierId = humidifierId;
			this.humidifierOutletId = humidifierOutletId;
		}

		@Override
		public void run() {
			try {
				logger.info("manual turn off of humidifier " + humidifierOutletId);
				RetryingCommand.execute(
						() -> {
							switchbotController
									.getSwitchbotAPI()
									.getDeviceApi()
									.sendDeviceControlCommands(humidifierOutletId, IDeviceCommands.PLUG_MINI_OFF);
							return null;
						},
						humidifierOutletId);
				logger.info("starting pump " + pumpId);
				RetryingCommand.execute(
						() -> {
							switchbotController
									.getSwitchbotAPI()
									.getDeviceApi()
									.sendDeviceControlCommands(pumpId, IDeviceCommands.PLUG_MINI_ON);
							return null;
						},
						pumpId);
				Thread.sleep(5 * 1000);

				logger.info("manual turn on of humidifier " + humidifierOutletId);
				RetryingCommand.execute(
						() -> {
							switchbotController
									.getSwitchbotAPI()
									.getDeviceApi()
									.sendDeviceControlCommands(humidifierOutletId, IDeviceCommands.PLUG_MINI_ON);
							return null;
						},
						humidifierOutletId);

				Thread.sleep(60 * 1000); // 1 min
				logger.info("starting humidifier " + humidifierId);
				RetryingCommand.execute(
						() -> {
							goveeController.sendDeviceCommand(
									IHumidifierCommands.turnOn(humidifierModel, humidifierId));
							return null;
						},
						humidifierId);

				Thread.sleep(60 * 1000); // 1 min

				logger.info("stopping pump " + pumpId);
				RetryingCommand.execute(
						() -> {
							switchbotController
									.getSwitchbotAPI()
									.getDeviceApi()
									.sendDeviceControlCommands(pumpId, IDeviceCommands.PLUG_MINI_OFF);
							return null;
						},
						pumpId);
			} catch (IOException | InterruptedException e) {
				logger.error("error refilling humidifier, attempting to turn off pump " + pumpId, e);
				try {
					Thread.sleep(5 * 1000); // 5 sec
					RetryingCommand.execute(
							() -> {
								switchbotController
										.getSwitchbotAPI()
										.getDeviceApi()
										.sendDeviceControlCommands(pumpId, IDeviceCommands.PLUG_MINI_OFF);
								return null;
							},
							pumpId);
				} catch (IOException | InterruptedException e2) {
					logger.error("error turning off pump " + pumpId, e2);
				}
			}
		}
	}
}
