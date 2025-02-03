package com.bigboxer23.lights.controllers.room;

import com.bigboxer23.lights.controllers.aggregate.HeaterController;
import com.bigboxer23.lights.controllers.aggregate.HumidityController;
import com.bigboxer23.lights.controllers.switchbot.SwitchBotController;
import com.bigboxer23.switch_bot.IDeviceCommands;
import com.bigboxer23.switch_bot.data.IApiResponse;
import com.bigboxer23.utils.command.RetryingCommand;
import com.bigboxer23.utils.file.FilePersistedBoolean;
import com.bigboxer23.utils.time.ITimeConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.io.IOException;
import java.net.HttpURLConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/** */
@Slf4j
@RestController
public class BMController {
	@Value("${bm.fanSwitchId}")
	private String fanSwitchId;

	@Value("${bm.lightswitchId}")
	private String lightSwitchId;

	/** Minutes to run fan */
	@Value("${fanDuration}")
	private long fanDuration;

	@Value("${bm.incubator_map}")
	private String incubatorMap;

	@Value("${bm.humidity_map}")
	private String humidifierMap;

	private final FilePersistedBoolean faeDisabled = new FilePersistedBoolean("BMController.FanService");

	private final FilePersistedBoolean lightsDisabled = new FilePersistedBoolean("BMController.Lights");

	private final SwitchBotController switchbotController;

	private HeaterController incubatorController;

	private HumidityController humidityController;

	public BMController(SwitchBotController switchbotController) {
		this.switchbotController = switchbotController;
		log.info("BMController.FanSystem initialized and enabled: " + !faeDisabled.get());
		log.info("BMController.Lights initialized and enabled: " + !lightsDisabled.get());
	}

	@Scheduled(cron = "0 */5 * * * *")
	public void humidityControl() throws IOException {
		if (humidityController == null) {
			humidityController = new HumidityController(switchbotController, humidifierMap);
		}
		humidityController.run();
	}

	@Scheduled(cron = "0 */5 * * * *")
	public void incubateControl() throws IOException {
		if (incubatorController == null) {
			incubatorController = new HeaterController(switchbotController, incubatorMap);
		}
		incubatorController.run();
	}

	@Scheduled(cron = "0 0 8 * * ?")
	public void lightsOn() throws IOException {
		if (lightsDisabled.get()) {
			return;
		}
		RetryingCommand.execute(
				() -> switchbotController
						.getSwitchbotAPI()
						.getDeviceApi()
						.sendDeviceControlCommands(lightSwitchId, IDeviceCommands.PLUG_MINI_ON),
				"On " + switchbotController.getIdentifier(lightSwitchId));
	}

	@Scheduled(cron = "0 0 20 * * ?")
	public void lightsOff() throws IOException {
		if (lightsDisabled.get()) {
			return;
		}
		RetryingCommand.execute(
				() -> switchbotController
						.getSwitchbotAPI()
						.getDeviceApi()
						.sendDeviceControlCommands(lightSwitchId, IDeviceCommands.PLUG_MINI_OFF),
				"Off " + switchbotController.getIdentifier(lightSwitchId));
	}

	@Scheduled(cron = "0 */15 * * * *") // every 15 min
	public void runFans() throws IOException, InterruptedException {
		if (faeDisabled.get()) {
			return;
		}
		RetryingCommand.execute(
				() -> {
					IApiResponse response = switchbotController
							.getSwitchbotAPI()
							.getDeviceApi()
							.sendDeviceControlCommands(fanSwitchId, IDeviceCommands.PLUG_MINI_ON);
					return null;
				},
				"On " + switchbotController.getIdentifier(fanSwitchId));

		log.debug("sleeping fan system controller");
		Thread.sleep(fanDuration * ITimeConstants.MINUTE);
		RetryingCommand.execute(
				() -> {
					switchbotController
							.getSwitchbotAPI()
							.getDeviceApi()
							.sendDeviceControlCommands(fanSwitchId, IDeviceCommands.PLUG_MINI_OFF);
					return null;
				},
				"Off " + switchbotController.getIdentifier(fanSwitchId));
	}

	@PostMapping(value = "/S/BMController/FanService/{enable}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "enable or disable the BM Fan service")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_BAD_REQUEST + "", description = "Bad request"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public void enableFans(
			@Parameter(description = "Enable or disable the fan controller from running")
					@PathVariable(value = "enable")
					boolean enable) {
		log.info(enable + " fan system requested via web api");
		faeDisabled.set(!enable);
	}

	@PostMapping(value = "/S/BMController/Lights/{enable}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "enable or disable the BM lights")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_BAD_REQUEST + "", description = "Bad request"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public void enableLights(
			@Parameter(description = "Enable or disable the bm lights from running") @PathVariable(value = "enable")
					boolean enable) {
		log.info(enable + " lights system requested via web api");
		lightsDisabled.set(!enable);
	}

	@PostMapping(value = "/S/BMController/IncubationHeater/{enable}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "enable or disable the BM incubation heater")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_BAD_REQUEST + "", description = "Bad request"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public void enableIncubationHeater(
			@Parameter(description = "Enable or disable the bm incubation heater from running")
					@PathVariable(value = "enable")
					boolean enable) {
		log.info(enable + " incubation heater system requested via web api");
		incubatorController.disable(!enable);
	}

	@PostMapping(value = "/S/BMController/Humidity/{enable}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "enable or disable the BM humidity")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_BAD_REQUEST + "", description = "Bad request"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public void enableHumidityController(
			@Parameter(description = "Enable or disable the bm humidity from running") @PathVariable(value = "enable")
					boolean enable) {
		log.info(enable + " humidity system requested via web api");
		humidityController.disable(!enable);
	}
}
