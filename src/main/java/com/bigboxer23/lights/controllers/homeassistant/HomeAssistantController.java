package com.bigboxer23.lights.controllers.homeassistant;

import com.bigboxer23.utils.http.OkHttpCallback;
import com.bigboxer23.utils.http.OkHttpUtil;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Controller to interact with Home Assistant REST API */
@Slf4j
@Component
public class HomeAssistantController {

	@Value("${homeAssistantUrl}")
	private String homeAssistantUrl;

	@Value("${homeAssistantToken}")
	private String homeAssistantToken;

	private static final String SERVICES_ENDPOINT = "/api/services/";
	private static final String INPUT_BOOLEAN_DOMAIN = "input_boolean";
	private static final String TURN_ON_SERVICE = "turn_on";
	private static final String TURN_OFF_SERVICE = "turn_off";

	/**
	 * Turn on a boolean input in Home Assistant
	 *
	 * @param entityId the entity ID of the input_boolean (e.g., "my_boolean_input")
	 */
	public void turnOn(String entityId) {
		callService(TURN_ON_SERVICE, entityId);
	}

	/**
	 * Turn off a boolean input in Home Assistant
	 *
	 * @param entityId the entity ID of the input_boolean (e.g., "my_boolean_input")
	 */
	public void turnOff(String entityId) {
		callService(TURN_OFF_SERVICE, entityId);
	}

	/**
	 * Toggle a boolean input based on the provided state
	 *
	 * @param entityId the entity ID of the input_boolean
	 * @param turnOn true to turn on, false to turn off
	 */
	public void setState(String entityId, boolean turnOn) {
		if (turnOn) {
			turnOn(entityId);
		} else {
			turnOff(entityId);
		}
	}

	private void callService(String service, String entityId) {
		String url = homeAssistantUrl + SERVICES_ENDPOINT + INPUT_BOOLEAN_DOMAIN + "/" + service;
		String payload = String.format("{\"entity_id\": \"input_boolean.%s\"}", entityId);

		log.info("Calling Home Assistant service: {} for entity: {}", service, entityId);

		OkHttpUtil.post(url, new OkHttpCallback(), builder -> {
			builder.post(RequestBody.create(payload.getBytes(StandardCharsets.UTF_8)))
					.addHeader("Authorization", "Bearer " + homeAssistantToken)
					.addHeader("Content-Type", "application/json");
			return builder;
		});
	}
}
