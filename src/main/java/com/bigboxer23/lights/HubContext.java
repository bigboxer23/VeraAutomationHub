package com.bigboxer23.lights;

import com.bigboxer23.lights.controllers.NotificationController;
import com.bigboxer23.lights.controllers.frontdoor.FrontDoorController;
import com.bigboxer23.lights.controllers.garage.GarageController;
import com.bigboxer23.lights.controllers.homeassistant.HomeAssistantController;
import com.bigboxer23.lights.controllers.openHAB.OpenHABController;
import com.bigboxer23.lights.controllers.scene.DaylightController;
import com.bigboxer23.lights.controllers.scene.WeatherController;
import com.bigboxer23.lights.controllers.vera.VeraController;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

/** Config controlling class. Reads JSON data from file, initializes scenes from file's contents */
@Data
public class HubContext {
	private GarageController garageController;

	private FrontDoorController frontDoorController;

	private WeatherController weatherController;

	private DaylightController daylightController;

	private NotificationController notificationController;

	private VeraController veraController;

	private OpenHABController openHABController;

	private HomeAssistantController homeAssistantController;

	protected HubContext(
			GarageController garageController,
			FrontDoorController frontDoorController,
			WeatherController weatherController,
			DaylightController daylightController,
			VeraController veraController,
			OpenHABController openHABController,
			HomeAssistantController homeAssistantController) {
		setGarageController(garageController);
		setFrontDoorController(frontDoorController);
		setWeatherController(weatherController);
		setDaylightController(daylightController);
		setVeraController(veraController);
		setOpenHABController(openHABController);
		setHomeAssistantController(homeAssistantController);
	}

	@Autowired
	public void setNotificationController(@Lazy NotificationController notificationController) {
		this.notificationController = notificationController;
	}
}
