package com.bigboxer23.lights.controllers.econet;

import com.bigboxer23.eco_net.EcoNetAPI;
import com.bigboxer23.lights.controllers.homeassistant.HomeAssistantController;
import com.bigboxer23.lights.controllers.vera.VeraDeviceVO;
import com.bigboxer23.lights.controllers.vera.VeraHouseVO;
import com.bigboxer23.lights.data.HomeAssistantEntity;
import com.bigboxer23.utils.logging.LoggingContextBuilder;
import com.bigboxer23.utils.logging.WrappingCloseable;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** */
@Slf4j
@Component
public class WaterHeaterController {
	private EcoNetAPI ecoNetAPI;

	@Value("${econet_email:}")
	private String email;

	@Value("${econet_password:}")
	private String password;

	private VeraDeviceVO waterHeaterData;

	private final HomeAssistantController homeAssistantController;

	public WaterHeaterController(HomeAssistantController homeAssistantController) {
		this.homeAssistantController = homeAssistantController;
		waterHeaterData = new VeraDeviceVO("Water Heater", -1);
		log.debug("WaterHeaterController initialized");
	}

	private EcoNetAPI getEcoNetAPI() {
		if (ecoNetAPI == null) {
			ecoNetAPI = EcoNetAPI.getInstance(email, password);
		}
		return ecoNetAPI;
	}

	private void setEntityState(
			Map<String, HomeAssistantEntity> entities, String entityId, java.util.function.Consumer<String> setter) {

		HomeAssistantEntity entity = entities.get(entityId);
		if (entity != null) {
			String state = entity.getState();
			if (state != null) {
				setter.accept(state);
			}
		}
	}

	private void setEntityAttribute(
			Map<String, HomeAssistantEntity> entities,
			String entityId,
			String attributeName,
			java.util.function.Consumer<String> setter) {

		HomeAssistantEntity entity = entities.get(entityId);
		if (entity != null) {
			String value = entity.getAttributeAsString(attributeName);
			if (value != null) {
				setter.accept(value);
			}
		}
	}

	private void setEntityFloatState(
			Map<String, HomeAssistantEntity> entities,
			String entityId,
			java.util.function.Consumer<Float> setter,
			float divisor) {

		HomeAssistantEntity entity = entities.get(entityId);
		if (entity != null) {
			String state = entity.getState();
			if (state != null && !state.isEmpty()) {
				try {
					setter.accept(Float.parseFloat(state) / divisor);
				} catch (NumberFormatException e) {
					log.warn("Invalid numeric value for {}: {}", entityId, state, e);
				}
			}
		}
	}

	@Scheduled(fixedDelay = 300000) // 5min
	private void fetchWaterHeaterStatus() {
		try (WrappingCloseable c = LoggingContextBuilder.create().addTraceId().build()) {
			log.debug("Fetching water heater status...");
			Map<String, HomeAssistantEntity> waterHeaterEntities = homeAssistantController.getAllStates().stream()
					.filter(e -> e.getEntityId().startsWith("sensor.econet_hpwh")
							|| e.getEntityId().startsWith("binary_sensor.econet_hpwh")
							|| e.getEntityId().startsWith("climate.econet_hpwh")
							|| e.getEntityId().startsWith("sensor.hot_water_utility"))
					.collect(Collectors.toMap(HomeAssistantEntity::getEntityId, e -> e));

			setEntityFloatState(
					waterHeaterEntities, "sensor.econet_hpwh_hot_water", waterHeaterData::setHumidity, 100f);
			setEntityState(waterHeaterEntities, "binary_sensor.econet_hpwh_compressor", waterHeaterData::setStatus);
			setEntityAttribute(
					waterHeaterEntities,
					"climate.econet_hpwh_water_heater",
					"temperature",
					waterHeaterData::setTemperature);
			setEntityState(waterHeaterEntities, "sensor.hot_water_utility", waterHeaterData::setLevel);
			setEntityState(
					waterHeaterEntities, "sensor.econet_hpwh_upper_tank_temperature", waterHeaterData::setCategory);

			/*getEcoNetAPI().fetchUserData().ifPresent(data -> {
				Equipment equipment =
						data.getResults().getLocations().get(0).getEquipments().get(0);
				waterHeaterData.setHumidity(equipment.getTankStatus());
				waterHeaterData.setStatus(equipment.getCompressorStatus());
				waterHeaterData.setTemperature(equipment.getSetpoint().getValue() + "");
				LocalDate today = LocalDate.now();
				getEcoNetAPI()
						.fetchEnergyUsage(
								equipment.getDeviceName(),
								equipment.getSerialNumber(),
								today.getDayOfMonth(),
								today.getMonthValue(),
								today.getYear())
						.ifPresent(energyUsage -> {
							float kwh = energyUsage.getResults().getEnergyUsage().getData().stream()
									.map(ValueHolder::getValue)
									.reduce(0.0f, Float::sum);
							waterHeaterData.setLevel(kwh + "");
						});
			});*/
			log.debug("Fetched water heater status...");
		} catch (Exception e) {
			log.error("fetchWaterHeaterStatus", e);
		}
	}

	public void getStatus(VeraHouseVO house) {
		if (waterHeaterData != null && house != null && house.getRooms() != null) {
			house.getRooms().stream()
					.filter(room -> room.getName().equals("Climate"))
					.findAny()
					.ifPresent(room -> room.addDevice(waterHeaterData));
		}
	}
}
