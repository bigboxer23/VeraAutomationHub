package com.bigboxer23.lights.controllers.aggregate;

import com.bigboxer23.lights.controllers.EmailController;
import com.bigboxer23.lights.controllers.govee.GoveeHumidifierController;
import com.bigboxer23.lights.controllers.govee.HumidifierCluster;
import com.bigboxer23.lights.controllers.switchbot.SwitchBotController;
import com.bigboxer23.utils.logging.LoggingUtil;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
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
@Slf4j
@RestController
public class HumiditySystemController implements InitializingBean, IHumidityEventHandler {

	private final SwitchBotController switchbotController;

	private final GoveeHumidifierController goveeController;

	private final EmailController emailController;

	private final Map<String, HumidifierCluster> humidifierMap = new HashMap<>();

	@Value("${humidfier_to_pump_map}")
	private String MAP_KEY;

	private int HUMIDIFIER_RUNNING_WATTAGE = 10;

	public HumiditySystemController(
			SwitchBotController switchbotController,
			GoveeHumidifierController goveeController,
			EmailController emailController) {
		this.switchbotController = switchbotController;
		this.goveeController = goveeController;
		this.goveeController.addHandler(this);
		this.emailController = emailController;
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
		log.info(deviceId + " requested via web api");
		outOfWaterEvent(deviceId, deviceName, deviceModel);
	}

	@Scheduled(fixedDelay = 600000) // 10min
	public void checkAfterInterval() {
		log.info("checking cluster humidity");
		humidifierMap.values().stream()
				.filter(cluster -> !StringUtils.isEmpty(cluster.getHumiditySensor()))
				.forEach(cluster -> {
					try (MDC.MDCCloseable c = LoggingUtil.addMethod("checkAfterInterval")){
						int humidity = switchbotController
								.getDeviceStatus(cluster.getHumiditySensor())
								.getHumidity();
						if (humidity < cluster.getLowHumidityPoint()) {
							float watts = switchbotController
									.getDeviceStatus(cluster.getOutlet())
									.getWatts();
							if (watts > HUMIDIFIER_RUNNING_WATTAGE) {
								return;
							}
							outOfWaterEvent(
									cluster.getHumidifier(),
									goveeController.getDeviceNameFromId(cluster.getHumidifier()),
									cluster.getHumidifierModel());
						} else if (humidity > 73) { // Govee seems to have a bug where the humidifier
							// doesn't turn off, so we manually cycle again
							float watts = switchbotController
									.getDeviceStatus(cluster.getOutlet())
									.getWatts();
							if (watts > HUMIDIFIER_RUNNING_WATTAGE) {
								log.info("humidifier should not be running, humidify is too"
										+ " high "
										+ watts
										+ " "
										+ humidity);
								new Thread(new HumidifierResetAction(
												goveeController, cluster.getHumidifierModel(), cluster.getHumidifier()))
										.start();
							}
						}
					} catch (IOException e) {
						log.error("manualCheck: ", e);
					}
				});
	}

	@Override
	public void outOfWaterEvent(String deviceId, String deviceName, String deviceModel) {
		HumidifierCluster cluster = humidifierMap.get(deviceId);
		if (cluster == null) {
			log.warn("No cluster for " + deviceId);
			return;
		}
		log.info("Out of water event triggered " + deviceName + " : " + deviceId);
		if (!emailController.sendMessageThrottled(deviceId, deviceName)) {
			new Thread(new RefillAction(
							switchbotController,
							goveeController,
							cluster.getPump(),
							deviceModel,
							deviceId,
							cluster.getOutlet()))
					.start();
		}
	}
}
