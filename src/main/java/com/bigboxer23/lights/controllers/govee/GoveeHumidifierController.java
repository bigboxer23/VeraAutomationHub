package com.bigboxer23.lights.controllers.govee;

import com.bigboxer23.govee.GoveeApi;
import com.bigboxer23.govee.GoveeEventSubscriber;
import com.bigboxer23.govee.IHumidifierCommands;
import com.bigboxer23.govee.data.GoveeDeviceCommandResponse;
import com.bigboxer23.govee.data.GoveeEvent;
import com.bigboxer23.lights.controllers.switchbot.SwitchBotController;
import com.bigboxer23.switch_bot.IDeviceCommands;
import com.google.gson.*;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

/**
 * Controller listens for empty events from humidifier and triggers a pump to add water to the tank and turn the humidifier back on
 */
@Controller
public class GoveeHumidifierController implements InitializingBean {
	@Value("${govee_api_key}")
	private String API_KEY;

	@Value("${humidfier_to_pump_map}")
	private String MAP_KEY;

	private JsonObject humidifierToPumpMap;

	private static final Logger logger = LoggerFactory.getLogger(GoveeHumidifierController.class);

	private final SwitchBotController switchbotController;

	public GoveeHumidifierController(SwitchBotController switchbotController) {
		this.switchbotController = switchbotController;
	}

	private void setupListeners() {
		logger.warn("starting govee event listener");
		humidifierToPumpMap = JsonParser.parseString(MAP_KEY).getAsJsonObject();
		GoveeApi.getInstance(API_KEY).subscribeToGoveeEvents(new GoveeEventSubscriber() {
			@Override
			public void messageReceived(GoveeEvent event) {
				if (event.isLackWaterEvent()) {
					logger.warn(
							"no water: " + event.getModel() + " " + event.getDeviceId() + " " + event.getDeviceName());
					JsonPrimitive pump = humidifierToPumpMap.getAsJsonPrimitive(event.getDeviceId());
					if (pump == null) {
						logger.warn("No pump for " + event.getDeviceId());
						return;
					}
					new Thread(new RefillAction(pump.getAsString(), event.getModel(), event.getDeviceId())).start();
				}
			}
		});
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		setupListeners();
	}

	private class RefillAction implements Runnable {
		private final String pumpId;
		private final String humidifierModel;
		private final String humidifierId;

		public RefillAction(String pumpId, String humidifierModel, String humidifierId) {
			this.pumpId = pumpId;
			this.humidifierModel = humidifierModel;
			this.humidifierId = humidifierId;
		}

		@Override
		public void run() {
			try {
				logger.info("starting pump " + pumpId);
				switchbotController
						.getSwitchbotAPI()
						.getDeviceApi()
						.sendDeviceControlCommands(pumpId, IDeviceCommands.PLUG_MINI_ON);
				Thread.sleep(5 * 1000);

				logger.info("starting humidifier " + humidifierId);
				GoveeDeviceCommandResponse response = GoveeApi.getInstance(API_KEY)
						.sendDeviceCommand(IHumidifierCommands.turnOn(humidifierModel, humidifierId));
				Thread.sleep(2 * 60 * 1000); // 2 min

				logger.info("stopping pump " + pumpId);
				switchbotController
						.getSwitchbotAPI()
						.getDeviceApi()
						.sendDeviceControlCommands(pumpId, IDeviceCommands.PLUG_MINI_OFF);
			} catch (IOException | InterruptedException e) {
				logger.error("error refilling humidifier, attempting to turn off pump " + pumpId, e);
				try {
					Thread.sleep(5 * 1000); // 5 sec
					switchbotController
							.getSwitchbotAPI()
							.getDeviceApi()
							.sendDeviceControlCommands(pumpId, IDeviceCommands.PLUG_MINI_OFF);
				} catch (IOException | InterruptedException e2) {
					logger.error("error turning off pump " + pumpId, e2);
				}
			}
		}
	}
}
