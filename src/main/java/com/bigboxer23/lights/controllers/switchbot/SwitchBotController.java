package com.bigboxer23.lights.controllers.switchbot;

import com.bigboxer23.lights.controllers.EmailController;
import com.bigboxer23.lights.util.IErrorConstants;
import com.bigboxer23.switch_bot.IDeviceCommands;
import com.bigboxer23.switch_bot.IDeviceTypes;
import com.bigboxer23.switch_bot.SwitchBotApi;
import com.bigboxer23.switch_bot.data.Device;
import com.bigboxer23.utils.command.Command;
import com.bigboxer23.utils.command.RetryingCommand;
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
			curtains = RetryingCommand.execute(
					() -> getSwitchbotAPI().getDeviceApi().getDevices().stream()
							.filter(device -> IDeviceTypes.CURTAIN.equals(device.getDeviceType()))
							/*.filter(Device::isMaster)
							.findAny()*/
							.map(Device::getDeviceId)
							.toList(),
					"Fetching CurtainIdFetch");
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
			RetryingCommand.execute(
					() -> {
						getSwitchbotAPI()
								.getDeviceApi()
								.sendDeviceControlCommands(curtainId, IDeviceCommands.CURTAIN_OPEN);
						return null;
					},
					"Open " + getIdentifier(curtainId),
					failureCommand(curtainId));
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
			RetryingCommand.execute(
					() -> {
						getSwitchbotAPI()
								.getDeviceApi()
								.sendDeviceControlCommands(curtainId, IDeviceCommands.CURTAIN_CLOSE);
						return null;
					},
					"Close " + getIdentifier(curtainId),
					failureCommand(curtainId));
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
		RetryingCommand.execute(
				() -> {
					getSwitchbotAPI()
							.getDeviceApi()
							.sendDeviceControlCommands(
									deviceId,
									"on".equalsIgnoreCase(command)
											? IDeviceCommands.PLUG_MINI_ON
											: IDeviceCommands.PLUG_MINI_OFF);

					return null;
				},
				command + " " + getIdentifier(deviceId),
				failureCommand(deviceId));
	}

	public Command<Void> failureCommand(String deviceId) {
		return () -> {
			emailController.sendMessageThrottled(
					deviceId, getIdentifier(deviceId), IErrorConstants.emailSubject, IErrorConstants.emailBody);
			return null;
		};
	}
}
