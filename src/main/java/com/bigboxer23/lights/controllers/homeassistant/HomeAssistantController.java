package com.bigboxer23.lights.controllers.homeassistant;

import com.bigboxer23.lights.data.HomeAssistantEntity;
import com.bigboxer23.utils.http.OkHttpCallback;
import com.bigboxer23.utils.http.OkHttpUtil;
import com.bigboxer23.utils.logging.LoggingContextBuilder;
import com.bigboxer23.utils.logging.WrappingCloseable;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import okhttp3.RequestBody;
import okhttp3.Response;
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
	private static final String STATES_ENDPOINT = "/api/states";
	private static final String INPUT_BOOLEAN_DOMAIN = "input_boolean";
	private static final String TURN_ON_SERVICE = "turn_on";
	private static final String TURN_OFF_SERVICE = "turn_off";

	private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

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

	/**
	 * Get all entity states from Home Assistant
	 *
	 * @return List of all Home Assistant entities, or empty list if request fails
	 */
	public List<HomeAssistantEntity> getAllStates() {
		String url = homeAssistantUrl + STATES_ENDPOINT;
		log.debug("Fetching all states from Home Assistant: {}", url);

		try (WrappingCloseable c = LoggingContextBuilder.create().addTraceId().build()) {
			Response response = OkHttpUtil.getSynchronous(url, builder -> {
				builder.addHeader("Authorization", "Bearer " + homeAssistantToken)
						.addHeader("Content-Type", "application/json");
				return builder;
			});

			if (!response.isSuccessful()) {
				log.warn("Failed to fetch states from Home Assistant. Response: {}", response.code());
				return Collections.emptyList();
			}
			String responseBody = response.body().string();
			List<HomeAssistantEntity> entities = objectMapper.readValue(responseBody, new TypeReference<>() {});
			log.debug("Successfully retrieved {} entities from Home Assistant", entities.size());
			return entities;
		} catch (IOException e) {
			log.error("Error fetching states from Home Assistant", e);
			return Collections.emptyList();
		}
	}

	/**
	 * Get a specific entity state by entity ID
	 *
	 * @param entityId the full entity ID (e.g., "sensor.temperature")
	 * @return Optional containing the entity if found
	 */
	public Optional<HomeAssistantEntity> getEntityState(String entityId) {
		return getAllStates().stream()
				.filter(entity -> entityId.equals(entity.getEntityId()))
				.findFirst();
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
