package com.bigboxer23.lights.controllers.aggregate;

import com.bigboxer23.lights.controllers.EmailController;
import com.bigboxer23.lights.controllers.switchbot.SwitchBotController;
import java.io.IOException;

/** */
public class HumidityController extends AbstractEnvironmentController {
	public HumidityController(SwitchBotController controller, EmailController emailController, String heaterMap) {
		super(controller, emailController, heaterMap);
	}

	@Override
	public float getEnvironmentValue(SwitchBotController controller, String deviceId) throws IOException {
		return controller
				.getSwitchbotAPI()
				.getDeviceApi()
				.getDeviceStatus(deviceId)
				.getHumidity();
	}
}
