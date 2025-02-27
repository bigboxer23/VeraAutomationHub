package com.bigboxer23.lights.controllers.switchbot;

import com.bigboxer23.lights.controllers.EmailController;
import com.bigboxer23.lights.util.IErrorConstants;
import com.bigboxer23.switch_bot.IDeviceCommands;
import com.bigboxer23.switch_bot.IDeviceTypes;
import com.bigboxer23.switch_bot.SwitchBotApi;
import com.bigboxer23.switch_bot.data.Device;
import com.bigboxer23.switch_bot.data.DeviceCommand;
import com.bigboxer23.switch_bot.data.IApiResponse;
import com.bigboxer23.utils.command.Command;
import com.bigboxer23.utils.command.RetryingCommand;
import com.bigboxer23.utils.environment.EnvironmentUtils;
import com.bigboxer23.utils.logging.LoggingUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** */
@Slf4j
@Tag(name = "Switchbot Service", description = "Expose APIs needed to interface w/switchbot devices")
@RestController
public class SwitchBotController {
	@Value("${switchbot_token}")
	private String token;

	@Value("${switchbot_secret}")
	private String secret;

	private List<String> curtains;

	private SwitchBotApi api;

	private EmailController emailController;

	public SwitchBotController(EmailController emailController) {
		this.emailController = emailController;
	}

	public SwitchBotApi getSwitchbotAPI() throws IOException {
		if (api == null) {
			log.info("initializing switchbot API");
			api = SwitchBotApi.getInstance(token, secret);
		}
		return api;
	}

	public String getIdentifier(String deviceId) {
		try {
			deviceId = deviceId + ":" + getSwitchbotAPI().getDeviceApi().getDeviceNameFromId(deviceId);
		} catch (IOException e) {
			log.error("getIdentifier", e);
		}
		return deviceId;
	}

	private List<String> getCurtains() throws IOException {
		if (curtains == null) {
			curtains = RetryingCommand.builder()
					.identifier("Fetching CurtainIdFetch")
					.buildAndExecute(() -> getSwitchbotAPI().getDeviceApi().getDevices().stream()
							.filter(device -> IDeviceTypes.CURTAIN.equals(device.getDeviceType()))
							/*.filter(Device::isMaster)
							.findAny()*/
							.map(Device::getDeviceId)
							.toList());
		}
		return curtains;
	}

	@GetMapping(value = "/S/switchbot/openCurtain", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Open curtains", description = "call switchbot api to open curtains")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_BAD_REQUEST + "", description = "Bad request"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public void openCurtains() throws IOException {
		for (String curtainId : getCurtains()) {
			RetryingCommand.builder()
					.identifier("Open " + getIdentifier(curtainId))
					.failureCommand(failureCommand(curtainId))
					.buildAndExecute(() -> {
						getSwitchbotAPI()
								.getDeviceApi()
								.sendDeviceControlCommands(curtainId, IDeviceCommands.CURTAIN_OPEN);
						return null;
					});
		}
	}

	@GetMapping(value = "/S/switchbot/closeCurtain", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Open curtains", description = "call switchbot api to close curtains")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_BAD_REQUEST + "", description = "Bad request"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public void closeCurtains() throws IOException {
		for (String curtainId : getCurtains()) {
			RetryingCommand.builder()
					.identifier("Close " + getIdentifier(curtainId))
					.failureCommand(failureCommand(curtainId))
					.buildAndExecute(() -> {
						getSwitchbotAPI()
								.getDeviceApi()
								.sendDeviceControlCommands(curtainId, IDeviceCommands.CURTAIN_CLOSE);
						return null;
					});
		}
	}

	@GetMapping(value = "/S/switchbot/plugmini/{deviceId}/{command}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "control plug mini", description = "call switchbot api to control plug mini")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_BAD_REQUEST + "", description = "Bad request"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public void plugMini(
			@Parameter(description = "deviceId to run command on") @PathVariable(value = "deviceId") String deviceId,
			@Parameter(description = "command to run.  Possible values [0-100, ON, OFF]")
					@PathVariable(value = "command")
					String command)
			throws IOException {
		sendDeviceControlCommands(
				deviceId,
				"on".equalsIgnoreCase(command) ? IDeviceCommands.PLUG_MINI_ON : IDeviceCommands.PLUG_MINI_OFF);
	}

	public Command<Void> failureCommand(String deviceId) {
		return () -> {
			emailController.sendMessageThrottled(
					deviceId, getIdentifier(deviceId), IErrorConstants.emailSubject, IErrorConstants.emailBody);
			return null;
		};
	}

	public Device getDeviceStatus(String deviceId) throws IOException {
		Device device = RetryingCommand.builder()
				.identifier("Device Status " + getIdentifier(deviceId))
				.failureCommand(failureCommand(deviceId))
				.buildAndExecute(() -> getSwitchbotAPI().getDeviceApi().getDeviceStatus(deviceId));
		try (MDC.MDCCloseable i = LoggingUtil.addDeviceId(device.getDeviceId());
				MDC.MDCCloseable t = LoggingUtil.addTemperature(
						device.getTemperature() != 0
								? EnvironmentUtils.celciusToFahrenheit(device.getTemperature())
								: device.getTemperature());
				MDC.MDCCloseable w = LoggingUtil.addWatts(device.getWatts());
				MDC.MDCCloseable h = LoggingUtil.addHumidity(device.getHumidity());
				MDC.MDCCloseable co2 = LoggingUtil.addCO2(device.getCo2())) {
			log.info("Device Status: {}", getIdentifier(deviceId));
			return device;
		}
	}

	public IApiResponse sendDeviceControlCommands(String deviceId, DeviceCommand command) throws IOException {
		try (MDC.MDCCloseable i = LoggingUtil.addDeviceId(deviceId);
				MDC.MDCCloseable t = LoggingUtil.addCommand(command.getCommand())) {
			log.info("sendDeviceControlCommands: {}", getIdentifier(deviceId));
			return RetryingCommand.builder()
					.identifier(command.getCommand() + getIdentifier(deviceId))
					.failureCommand(failureCommand(deviceId))
					.buildAndExecute(
							() -> getSwitchbotAPI().getDeviceApi().sendDeviceControlCommands(deviceId, command));
		}
	}
}
