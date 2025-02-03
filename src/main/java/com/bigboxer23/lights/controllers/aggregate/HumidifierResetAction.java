package com.bigboxer23.lights.controllers.aggregate;

import com.bigboxer23.govee.IHumidifierCommands;
import com.bigboxer23.lights.controllers.govee.GoveeHumidifierController;
import com.bigboxer23.utils.command.RetryingCommand;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

/**
 * Govee humidifier sometimes "sticks" on when in auto mode. This action resets the humidity control
 * if detected its stuck open
 */
@Slf4j
public class HumidifierResetAction implements Runnable {
	private final GoveeHumidifierController goveeController;

	private final String humidifierModel;
	private final String humidifierId;

	public HumidifierResetAction(GoveeHumidifierController govee, String humidifierModel, String humidifierId) {
		goveeController = govee;
		this.humidifierModel = humidifierModel;
		this.humidifierId = humidifierId;
	}

	@Override
	public void run() {
		try {
			RetryingCommand.execute(
					() -> {
						goveeController.sendDeviceCommand(
								IHumidifierCommands.setAutoHumidityTargetPercent(humidifierModel, humidifierId, 50));
						return null;
					},
					"Set 50% " + goveeController.getIdentifier(humidifierId));
			Thread.sleep(5 * 1000);
			RetryingCommand.execute(
					() -> {
						goveeController.sendDeviceCommand(
								IHumidifierCommands.setAutoHumidityTargetPercent(humidifierModel, humidifierId, 70));
						return null;
					},
					"Set 70% " + goveeController.getIdentifier(humidifierId));
		} catch (InterruptedException | IOException e) {
			log.error("HumidifierResetAction", e);
		}
	}
}
