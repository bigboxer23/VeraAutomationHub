package com.bigboxer23.lights.controllers.room;

import static com.bigboxer23.utils.logging.LoggingUtil.runTraced;

import com.bigboxer23.lights.controllers.EmailController;
import com.bigboxer23.lights.controllers.switchbot.SwitchBotController;
import com.bigboxer23.switch_bot.IDeviceCommands;
import com.bigboxer23.utils.file.FilePersistedBoolean;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

/** */
@RestController
public class VeggieTentController {
	private final SwitchBotController switchbotController;

	private final EmailController emailController;

	private final FilePersistedBoolean lightsDisabled = new FilePersistedBoolean("VeggieTentController.Lights");

	private final FilePersistedBoolean lifterPumpDisabled = new FilePersistedBoolean("VeggieTentController.lifterPump");

	private final FilePersistedBoolean circulationPumpDisabled =
			new FilePersistedBoolean("VeggieTentController.circulationPump");

	private final FilePersistedBoolean humidifierDisabled = new FilePersistedBoolean("VeggieTentController.humidifier");

	private final FilePersistedBoolean airPumpDisabled = new FilePersistedBoolean("VeggieTentController.airPump");

	@Value("${veggieTent.lightswitchId}")
	private String lightSwitchId;

	@Value("${veggieTent.lifterPump}")
	private String lifterPumpSwitchId;

	@Value("${veggieTent.circulationPump}")
	private String circulationPumpSwitchId;

	@Value("${veggieTent.airPump}")
	private String airPumpSwitchId;

	public VeggieTentController(SwitchBotController switchbotController, EmailController emailController) {
		this.switchbotController = switchbotController;
		this.emailController = emailController;
	}

	@Scheduled(cron = "0 0 8 * * ?")
	public void lightsOn() throws IOException, InterruptedException {
		if (lightsDisabled.get()) {
			return;
		}
		runTraced(() -> switchbotController.sendDeviceControlCommands(lightSwitchId, IDeviceCommands.PLUG_MINI_ON));
	}

	@Scheduled(cron = "0 0 21 * * ?")
	public void lightsOff() throws IOException, InterruptedException {
		if (lightsDisabled.get()) {
			return;
		}
		runTraced(() -> switchbotController.sendDeviceControlCommands(lightSwitchId, IDeviceCommands.PLUG_MINI_OFF));
	}

	@Scheduled(cron = "0 0,15 * * * *")
	public void lifterPumpOn() throws IOException, InterruptedException {
		if (lifterPumpDisabled.get()) {
			return;
		}
		runTraced(
				() -> switchbotController.sendDeviceControlCommands(lifterPumpSwitchId, IDeviceCommands.PLUG_MINI_ON));
	}

	@Scheduled(cron = "30 0,15 * * * *")
	public void lifterPumpOff() throws IOException, InterruptedException {
		if (lifterPumpDisabled.get()) {
			return;
		}
		runTraced(
				() -> switchbotController.sendDeviceControlCommands(lifterPumpSwitchId, IDeviceCommands.PLUG_MINI_OFF));
	}

	@Scheduled(cron = "0 0 */2 * * ?")
	public void circulationPumpOn() throws IOException, InterruptedException {
		if (circulationPumpDisabled.get()) {
			return;
		}
		runTraced(() ->
				switchbotController.sendDeviceControlCommands(circulationPumpSwitchId, IDeviceCommands.PLUG_MINI_ON));
	}

	@Scheduled(cron = "0 15 */2 * * ?")
	public void circulationPumpOff() throws IOException, InterruptedException {
		if (circulationPumpDisabled.get()) {
			return;
		}
		runTraced(() ->
				switchbotController.sendDeviceControlCommands(circulationPumpSwitchId, IDeviceCommands.PLUG_MINI_OFF));
	}

	@Scheduled(cron = "0 30 23 * * ?")
	@Scheduled(cron = "0 0 9 * * ?")
	public void airPumpOn() throws IOException, InterruptedException {
		if (airPumpDisabled.get()) {
			return;
		}
		runTraced(() -> switchbotController.sendDeviceControlCommands(airPumpSwitchId, IDeviceCommands.PLUG_MINI_ON));
	}

	@Scheduled(cron = "0 0 7 * * ?")
	@Scheduled(cron = "0 0 13 * * ?")
	public void airPumpOff() throws IOException, InterruptedException {
		if (airPumpDisabled.get()) {
			return;
		}
		runTraced(() -> switchbotController.sendDeviceControlCommands(airPumpSwitchId, IDeviceCommands.PLUG_MINI_OFF));
	}
}
