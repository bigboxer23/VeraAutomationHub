package com.bigboxer23.lights.controllers.aggregate;

import com.bigboxer23.lights.controllers.switchbot.SwitchBotController;
import com.bigboxer23.switch_bot.IDeviceCommands;
import com.bigboxer23.utils.command.RetryingCommand;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RestController;

/** */
@Slf4j
@RestController
public class TempLifterController {
	private final String fanSwitchId = "DCDA0CAB2BBE"; // S5 Lifter Pump

	private final SwitchBotController switchbotController;

	public TempLifterController(SwitchBotController switchbotController) {
		this.switchbotController = switchbotController;
	}

	@Scheduled(cron = "0 0,5,10 */2 * * *")
	public void runFans() throws IOException, InterruptedException {
		RetryingCommand.execute(
				() -> {
					switchbotController
							.getSwitchbotAPI()
							.getDeviceApi()
							.sendDeviceControlCommands(fanSwitchId, IDeviceCommands.PLUG_MINI_ON);
					return null;
				},
				"On " + switchbotController.getIdentifier(fanSwitchId));

		log.debug("sleeping lifter system controller");
		Thread.sleep(30000L);
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
}
