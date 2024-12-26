package com.bigboxer23.lights.controllers.aggregate;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/** */
@RestController
public class FanSystemController {
	private static final Logger logger = LoggerFactory.getLogger(FanSystemController.class);

	/** id of the switchbot switch device */
	@Value("${fanSwitchId}")
	private String fanSwitchId;

	private final FilePersistedBoolean disabled = new FilePersistedBoolean("FanService");

	/** Minutes to run fan */
	@Value("${fanDuration}")
	private long fanDuration;

	private final SwitchBotController switchbotController;

	public FanSystemController(SwitchBotController switchbotController) {
		this.switchbotController = switchbotController;
		logger.info("FanSystemController initialized and enabled: " + !disabled.get());
	}

	@Scheduled(cron = "0 */15 * * * *") // every hour
	public void runFans() throws IOException, InterruptedException {
		if (disabled.get()) {
			return;
		}
		logger.info("Turning on fan system");
		RetryingCommand.execute(
				() -> {
					IApiResponse response = switchbotController
							.getSwitchbotAPI()
							.getDeviceApi()
							.sendDeviceControlCommands(fanSwitchId, IDeviceCommands.PLUG_MINI_ON);
					return null;
				},
				switchbotController.getIdentifier(fanSwitchId));

		logger.debug("sleeping fan system controller");
		Thread.sleep(fanDuration * ITimeConstants.MINUTE);
		logger.info("Turning off fan system");
		RetryingCommand.execute(
				() -> {
					switchbotController
							.getSwitchbotAPI()
							.getDeviceApi()
							.sendDeviceControlCommands(fanSwitchId, IDeviceCommands.PLUG_MINI_OFF);
					return null;
				},
				switchbotController.getIdentifier(fanSwitchId));
	}

	@PostMapping(value = "/S/FanSystem/{enable}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "enable or disable the fan service")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_BAD_REQUEST + "", description = "Bad request"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public void triggerHumidifierRefillAndRestart(
			@Parameter(description = "Enable or disable the fan controller from running")
					@PathVariable(value = "enable")
					boolean enable) {
		logger.info(enable + " fan system requested via web api");
		disabled.set(!enable);
	}
}
