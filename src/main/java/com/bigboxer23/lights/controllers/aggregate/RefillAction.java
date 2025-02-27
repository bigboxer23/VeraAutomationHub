package com.bigboxer23.lights.controllers.aggregate;

import com.bigboxer23.govee.IHumidifierCommands;
import com.bigboxer23.lights.controllers.govee.GoveeHumidifierController;
import com.bigboxer23.lights.controllers.switchbot.SwitchBotController;
import com.bigboxer23.switch_bot.IDeviceCommands;
import com.bigboxer23.utils.command.RetryingCommand;
import com.bigboxer23.utils.logging.LoggingUtil;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

/** */
@Slf4j
public class RefillAction implements Runnable {
	private final SwitchBotController switchbotController;

	private final GoveeHumidifierController goveeController;

	private final String pumpId;
	private final String humidifierModel;
	private final String humidifierId;

	private final String humidifierOutletId;

	public RefillAction(
			SwitchBotController controller,
			GoveeHumidifierController govee,
			String pumpId,
			String humidifierModel,
			String humidifierId,
			String humidifierOutletId) {
		switchbotController = controller;
		goveeController = govee;
		this.pumpId = pumpId;
		this.humidifierModel = humidifierModel;
		this.humidifierId = humidifierId;
		this.humidifierOutletId = humidifierOutletId;
	}

	@Override
	public void run() {
		try (MDC.MDCCloseable c = LoggingUtil.addMethod("refillAction")) {
			switchbotController.sendDeviceControlCommands(humidifierOutletId, IDeviceCommands.PLUG_MINI_OFF);
			switchbotController.sendDeviceControlCommands(pumpId, IDeviceCommands.PLUG_MINI_ON);
			Thread.sleep(5 * 1000);

			switchbotController.sendDeviceControlCommands(humidifierOutletId, IDeviceCommands.PLUG_MINI_ON);

			Thread.sleep(60 * 1000); // 1 min
			RetryingCommand.builder()
					.identifier("On " + goveeController.getIdentifier(humidifierId))
					.failureCommand(switchbotController.failureCommand(humidifierId))
					.buildAndExecute(() -> {
						goveeController.sendDeviceCommand(IHumidifierCommands.turnOn(humidifierModel, humidifierId));
						return null;
					});
			Thread.sleep(60 * 1000); // 1 min
			switchbotController.sendDeviceControlCommands(pumpId, IDeviceCommands.PLUG_MINI_OFF);
		} catch (IOException | InterruptedException e) {
			log.error("error refilling humidifier, attempting to turn off pump " + pumpId, e);
			try {
				Thread.sleep(5 * 1000); // 5 sec
				switchbotController.sendDeviceControlCommands(pumpId, IDeviceCommands.PLUG_MINI_OFF);
			} catch (IOException | InterruptedException e2) {
				log.error("error turning off pump " + pumpId, e2);
			}
		}
	}
}
