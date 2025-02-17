package com.bigboxer23.lights.controllers.aggregate;

import com.bigboxer23.lights.controllers.EmailController;
import com.bigboxer23.lights.controllers.switchbot.SwitchBotController;
import com.bigboxer23.utils.environment.EnvironmentUtils;
import java.io.IOException;

/** */
public class HeaterController extends AbstractEnvironmentController {

	public HeaterController(SwitchBotController controller, EmailController emailController, String heaterMap) {
		super(controller, emailController, heaterMap);
	}

	@Override
	public float getEnvironmentValue(String identifier, String deviceId, SwitchBotController controller)
			throws IOException {
		return EnvironmentUtils.celciusToFahrenheit(
				controller.getDeviceStatus(deviceId).getTemperature());
	}
}
