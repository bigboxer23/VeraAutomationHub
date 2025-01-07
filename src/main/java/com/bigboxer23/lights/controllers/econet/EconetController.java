package com.bigboxer23.lights.controllers.econet;

import com.bigboxer23.eco_net.EcoNetAPI;
import com.bigboxer23.eco_net.data.Equipment;
import com.bigboxer23.lights.controllers.vera.VeraDeviceVO;
import com.bigboxer23.lights.controllers.vera.VeraHouseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

/** */
@Controller
public class EconetController {
	private static final Logger logger = LoggerFactory.getLogger(EconetController.class);

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

	@Scheduled(fixedDelay = 30000) // 5min
	private void fetchWaterHeaterStatus() {
		try {
			logger.debug("Fetching water heater status...");
			getEcoNetAPI().fetchUserData().ifPresent(data -> {
				Equipment equipment =
						data.getResults().getLocations().get(0).getEquipments().get(0);
				waterHeaterData.setHumidity(equipment.getTankStatus());
				waterHeaterData.setLevel(equipment.getCompressorStatus());
				waterHeaterData.setTemperature(equipment.getSetpoint().getValue() + "");
			});
			logger.debug("Fetched water heater status...");
		} catch (Exception e) {
			logger.error("fetchWaterHeaterStatus", e);
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
