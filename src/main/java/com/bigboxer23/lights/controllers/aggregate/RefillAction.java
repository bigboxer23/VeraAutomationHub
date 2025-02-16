package com.bigboxer23.lights.controllers.aggregate;

import com.bigboxer23.govee.IHumidifierCommands;
import com.bigboxer23.lights.controllers.govee.GoveeHumidifierController;
import com.bigboxer23.lights.controllers.switchbot.SwitchBotController;
import com.bigboxer23.switch_bot.IDeviceCommands;
import com.bigboxer23.utils.command.RetryingCommand;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

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
		try {
			RetryingCommand.execute(
					() -> {
						switchbotController
								.getSwitchbotAPI()
								.getDeviceApi()
								.sendDeviceControlCommands(humidifierOutletId, IDeviceCommands.PLUG_MINI_OFF);
						return null;
					},
					"Off " + switchbotController.getIdentifier(humidifierOutletId),
					switchbotController.failureCommand(humidifierOutletId));
			RetryingCommand.execute(
					() -> {
						switchbotController
								.getSwitchbotAPI()
								.getDeviceApi()
								.sendDeviceControlCommands(pumpId, IDeviceCommands.PLUG_MINI_ON);
						return null;
					},
					"On " + switchbotController.getIdentifier(pumpId),
					switchbotController.failureCommand(pumpId));
			Thread.sleep(5 * 1000);

			RetryingCommand.execute(
					() -> {
						switchbotController
								.getSwitchbotAPI()
								.getDeviceApi()
								.sendDeviceControlCommands(humidifierOutletId, IDeviceCommands.PLUG_MINI_ON);
						return null;
					},
					"On " + switchbotController.getIdentifier(humidifierOutletId),
					switchbotController.failureCommand(humidifierOutletId));

			Thread.sleep(60 * 1000); // 1 min
			RetryingCommand.execute(
					() -> {
						goveeController.sendDeviceCommand(IHumidifierCommands.turnOn(humidifierModel, humidifierId));
						return null;
					},
					"On " + goveeController.getIdentifier(humidifierId),
					switchbotController.failureCommand(humidifierId));

			Thread.sleep(60 * 1000); // 1 min
			RetryingCommand.execute(
					() -> {
						switchbotController
								.getSwitchbotAPI()
								.getDeviceApi()
								.sendDeviceControlCommands(pumpId, IDeviceCommands.PLUG_MINI_OFF);
						return null;
					},
					"Off " + switchbotController.getIdentifier(pumpId),
					switchbotController.failureCommand(pumpId));
		} catch (IOException | InterruptedException e) {
			log.error("error refilling humidifier, attempting to turn off pump " + pumpId, e);
			try {
				Thread.sleep(5 * 1000); // 5 sec
				RetryingCommand.execute(
						() -> {
							switchbotController
									.getSwitchbotAPI()
									.getDeviceApi()
									.sendDeviceControlCommands(pumpId, IDeviceCommands.PLUG_MINI_OFF);
							return null;
						},
						"Off " + switchbotController.getIdentifier(pumpId),
						switchbotController.failureCommand(pumpId));
			} catch (IOException | InterruptedException e2) {
				log.error("error turning off pump " + pumpId, e2);
			}
		}
	}
}
