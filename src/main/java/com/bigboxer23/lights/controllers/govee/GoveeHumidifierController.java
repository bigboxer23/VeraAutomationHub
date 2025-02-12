package com.bigboxer23.lights.controllers.govee;

import com.bigboxer23.govee.GoveeApi;
import com.bigboxer23.govee.GoveeEventSubscriber;
import com.bigboxer23.govee.data.GoveeDeviceCommandResponse;
import com.bigboxer23.govee.data.GoveeDeviceStatusRequest;
import com.bigboxer23.govee.data.GoveeEvent;
import com.bigboxer23.lights.controllers.EmailController;
import com.bigboxer23.lights.controllers.aggregate.IHumidityEventHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

/**
 * Controller listens for empty events from humidifier and triggers a pump to add water to the tank
 * and turn the humidifier back on
 */
@Slf4j
@Controller
public class GoveeHumidifierController implements InitializingBean {
	@Value("${govee_api_key}")
	private String API_KEY;

	@Value("${goveePushEnabled}")
	private boolean isEnabled;

	private EmailController emailController;

	private final List<IHumidityEventHandler> handlers = new ArrayList<>();

	public GoveeHumidifierController(EmailController controller) {
		emailController = controller;
	}

	public void addHandler(IHumidityEventHandler handler) {
		handlers.add(handler);
	}

	public GoveeDeviceCommandResponse sendDeviceCommand(GoveeDeviceStatusRequest command) throws IOException {
		return GoveeApi.getInstance(API_KEY).sendDeviceCommand(command);
	}

	@Override
	public void afterPropertiesSet() {
		if (!isEnabled) {
			log.warn("govee event listener not enabled");
			return;
		}
		log.warn("starting govee event listener");
		GoveeApi.getInstance(API_KEY).subscribeToGoveeEvents(new GoveeEventSubscriber() {
			@Override
			public void messageReceived(GoveeEvent event) {
				if (event.isLackWaterEvent()) {
					log.warn("no water: " + event.getModel() + " " + event.getDeviceId() + " " + event.getDeviceName());
					if (!emailController.sendMessageThrottled(event.getDeviceId(), event.getDeviceName())) {
						handlers.forEach(handler ->
								handler.outOfWaterEvent(event.getDeviceId(), event.getDeviceName(), event.getModel()));
					}
				}
			}
		});
	}

	public String getDeviceNameFromId(String deviceId) {
		return GoveeApi.getInstance(API_KEY).getDeviceNameFromId(deviceId);
	}

	public String getIdentifier(String deviceId) {
		return deviceId + ":" + getDeviceNameFromId(deviceId);
	}
}
