package com.bigboxer23.lights.controllers.govee;

import com.bigboxer23.govee.GoveeApi;
import com.bigboxer23.govee.GoveeEventSubscriber;
import com.bigboxer23.govee.data.GoveeDeviceCommandResponse;
import com.bigboxer23.govee.data.GoveeDeviceStatusRequest;
import com.bigboxer23.govee.data.GoveeEvent;
import com.bigboxer23.lights.controllers.aggregate.IHumidityEventHandler;
import com.bigboxer23.utils.mail.MailSender;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

/**
 * Controller listens for empty events from humidifier and triggers a pump to add water to the tank
 * and turn the humidifier back on
 */
@Controller
public class GoveeHumidifierController implements InitializingBean {
	@Value("${govee_api_key}")
	private String API_KEY;

	@Value("${goveeToEmail}")
	private String toEmail;

	@Value("${goveeFromEmail}")
	private String fromEmail;

	@Value("${goveeFromEmailPassword}")
	private String fromPassword;

	@Value("${goveePushEnabled}")
	private boolean isEnabled;

	private static final Logger logger = LoggerFactory.getLogger(GoveeHumidifierController.class);

	private final Map<String, Long> goveeEvents = new HashMap<>();

	private final List<IHumidityEventHandler> handlers = new ArrayList<>();

	public void addHandler(IHumidityEventHandler handler) {
		handlers.add(handler);
	}

	public GoveeDeviceCommandResponse sendDeviceCommand(GoveeDeviceStatusRequest command) throws IOException {
		return GoveeApi.getInstance(API_KEY).sendDeviceCommand(command);
	}

	@Override
	public void afterPropertiesSet() {
		if (!isEnabled) {
			logger.warn("govee event listener not enabled");
			return;
		}
		logger.warn("starting govee event listener");
		GoveeApi.getInstance(API_KEY).subscribeToGoveeEvents(new GoveeEventSubscriber() {
			@Override
			public void messageReceived(GoveeEvent event) {
				if (event.isLackWaterEvent()) {
					logger.warn(
							"no water: " + event.getModel() + " " + event.getDeviceId() + " " + event.getDeviceName());
					if (!isLastEventRecent(event.getDeviceId(), event.getDeviceName())) {
						handlers.forEach(handler ->
								handler.outOfWaterEvent(event.getDeviceId(), event.getDeviceName(), event.getModel()));
					}
				}
			}
		});
	}

	public boolean isLastEventRecent(String deviceId, String deviceName) {
		Long lastEvent = goveeEvents.get(deviceId);
		goveeEvents.put(deviceId, System.currentTimeMillis() + (1000 * 60 * 15)); // 15min
		boolean isRecent = lastEvent != null && System.currentTimeMillis() <= lastEvent;
		if (isRecent) {
			logger.error("govee event recent " + lastEvent + ":" + System.currentTimeMillis());
			MailSender.sendGmail(
					toEmail,
					fromEmail,
					fromPassword,
					deviceName + "  reservoir may be empty",
					"Reservoir for " + deviceName + " may be empty, please check & fill.",
					null);
		}
		return isRecent;
	}
}
