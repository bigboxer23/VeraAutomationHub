package com.bigboxer23.lights.controllers.econet;

import com.bigboxer23.eco_net.EcoNetAPI;
import com.bigboxer23.eco_net.data.Equipment;
import com.bigboxer23.eco_net.data.ValueHolder;
import com.bigboxer23.lights.controllers.vera.VeraDeviceVO;
import com.bigboxer23.lights.controllers.vera.VeraHouseVO;
import com.bigboxer23.utils.logging.LoggingContextBuilder;
import com.bigboxer23.utils.logging.WrappingCloseable;
import java.time.LocalDate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

/** */
@Slf4j
@Controller
public class EconetController {
	private EcoNetAPI ecoNetAPI;

	@Value("${econet_email}")
	private String email;

	@Value("${econet_password}")
	private String password;

	private VeraDeviceVO waterHeaterData;

	private EcoNetAPI getEcoNetAPI() {
		if (ecoNetAPI == null) {
			ecoNetAPI = EcoNetAPI.getInstance(email, password);
			waterHeaterData = new VeraDeviceVO("Water Heater", -1);
		}
		return ecoNetAPI;
	}

	@Scheduled(fixedDelay = 300000) // 5min
	private void fetchWaterHeaterStatus() {
		try (WrappingCloseable c = LoggingContextBuilder.create().addTraceId().build()) {
			log.debug("Fetching water heater status...");
			getEcoNetAPI().fetchUserData().ifPresent(data -> {
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
			});
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
