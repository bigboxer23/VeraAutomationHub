package com.bigboxer23.lights.controllers.switchbot;

import com.bigboxer23.switch_bot.IDeviceCommands;
import com.bigboxer23.switch_bot.IDeviceTypes;
import com.bigboxer23.switch_bot.SwitchBotApi;
import com.bigboxer23.switch_bot.data.Device;
import com.bigboxer23.utils.command.RetryingCommand;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** */
@Tag(name = "Switchbot Service", description = "Expose APIs needed to interface w/switchbot devices")
@RestController
public class SwitchBotController {
	private static final Logger logger = LoggerFactory.getLogger(SwitchBotController.class);

	@Value("${switchbot_token}")
	private String token;

	@Value("${switchbot_secret}")
	private String secret;

	private String curtainId;

	private SwitchBotApi api;

	private Map<String, String> deviceIdToName;

	public SwitchBotApi getSwitchbotAPI() throws IOException {
		if (api == null) {
			logger.info("initializing switchbot API");
			api = SwitchBotApi.getInstance(token, secret);
		}
		return api;
	}

	private String getCurtainId() throws IOException {
		if (curtainId == null) {
			logger.info("fetching curtain id");
			curtainId = RetryingCommand.execute(
					() -> getSwitchbotAPI().getDeviceApi().getDevices().stream()
							.filter(device -> IDeviceTypes.CURTAIN.equals(device.getDeviceType()))
							.filter(Device::isMaster)
							.findAny()
							.map(Device::getDeviceId)
							.orElse(null),
					"CurtainIdFetch");
		}
		return curtainId;
	}

	@GetMapping(value = "/S/switchbot/openCurtain", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Open curtains", description = "call switchbot api to open curtains")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_BAD_REQUEST + "", description = "Bad request"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public void openCurtains() throws IOException {
		logger.info("open curtain requested");
		RetryingCommand.execute(
				() -> {
					getSwitchbotAPI()
							.getDeviceApi()
							.sendDeviceControlCommands(getCurtainId(), IDeviceCommands.CURTAIN_OPEN);
					return null;
				},
				getCurtainId());
	}

	@GetMapping(value = "/S/switchbot/closeCurtain", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Open curtains", description = "call switchbot api to close curtains")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_BAD_REQUEST + "", description = "Bad request"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public void closeCurtains() throws IOException {
		logger.info("close curtain requested");
		RetryingCommand.execute(
				() -> {
					getSwitchbotAPI()
							.getDeviceApi()
							.sendDeviceControlCommands(getCurtainId(), IDeviceCommands.CURTAIN_CLOSE);
					return null;
				},
				getCurtainId());
	}

	private String getDeviceName(String deviceId) throws IOException {
		if (deviceIdToName == null || !deviceIdToName.containsKey(deviceId)) {
			logger.info("refreshing device id to name map");
			deviceIdToName = getSwitchbotAPI().getDeviceApi().getDevices().stream()
					.collect(Collectors.toMap(Device::getDeviceId, Device::getDeviceName));
		}
		return deviceIdToName.get(deviceId);
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
			throws IOException, InterruptedException {
		logger.info(getDeviceName(deviceId) + ":" + deviceId + ": " + command + " plug-mini requested");
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
				deviceId);
	}
}
