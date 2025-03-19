package com.bigboxer23.lights.controllers.switchbot;

import com.bigboxer23.lights.controllers.EmailController;
import com.bigboxer23.lights.util.IErrorConstants;
import com.bigboxer23.switch_bot.IDeviceCommands;
import com.bigboxer23.switch_bot.IDeviceTypes;
import com.bigboxer23.switch_bot.SwitchBotApi;
import com.bigboxer23.switch_bot.data.Device;
import com.bigboxer23.switch_bot.data.DeviceCommand;
import com.bigboxer23.switch_bot.data.IApiResponse;
import com.bigboxer23.utils.command.RetryingCommand;
import com.bigboxer23.utils.command.VoidCommand;
import com.bigboxer23.utils.environment.EnvironmentUtils;
import com.bigboxer23.utils.logging.LoggingContextBuilder;
import com.bigboxer23.utils.logging.WrappingCloseable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
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
		try (WrappingCloseable c = LoggingContextBuilder.create().addTraceId().build()) {
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
	}

	@GetMapping(value = "/S/switchbot/closeCurtain", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Open curtains", description = "call switchbot api to close curtains")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_BAD_REQUEST + "", description = "Bad request"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public void closeCurtains() throws IOException {
		try (WrappingCloseable c = LoggingContextBuilder.create().addTraceId().build()) {
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

	public VoidCommand failureCommand(String deviceId) {
		return () -> emailController.sendMessageThrottled(
				deviceId, getIdentifier(deviceId), IErrorConstants.emailSubject, IErrorConstants.emailBody);
	}

	public Device getDeviceStatus(String deviceId) throws IOException {
		Device device = RetryingCommand.builder()
				.identifier("Device Status " + getIdentifier(deviceId))
				.failureCommand(failureCommand(deviceId))
				.buildAndExecute(() -> getSwitchbotAPI().getDeviceApi().getDeviceStatus(deviceId));
		try (WrappingCloseable i = LoggingContextBuilder.create()
				.addDeviceId(device.getDeviceId())
				.addTemperature(
						device.getTemperature() != 0
								? EnvironmentUtils.celciusToFahrenheit(device.getTemperature())
								: device.getTemperature())
				.addWatts(device.getWatts())
				.addHumidity(device.getHumidity())
				.addCO2(device.getCo2())
				.addCommand("status")
				.build()) {
			log.info("Device Status: {}", getSwitchbotAPI().getDeviceApi().getDeviceNameFromId(deviceId));
			return device;
		}
	}

	public IApiResponse sendDeviceControlCommands(String deviceId, DeviceCommand command) throws IOException {
		try (WrappingCloseable i = LoggingContextBuilder.create()
				.addDeviceId(deviceId)
				.addCommand(command.getCommand())
				.addTraceId()
				.build()) {
			log.info(
					"sendDeviceControlCommands: {}",
					getSwitchbotAPI().getDeviceApi().getDeviceNameFromId(deviceId));
			return RetryingCommand.builder()
					.identifier(command.getCommand() + getIdentifier(deviceId))
					.failureCommand(failureCommand(deviceId))
					.buildAndExecute(
							() -> getSwitchbotAPI().getDeviceApi().sendDeviceControlCommands(deviceId, command));
		}
	}
}
