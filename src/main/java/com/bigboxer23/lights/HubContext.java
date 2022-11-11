package com.bigboxer23.lights;

import com.bigboxer23.lights.controllers.NotificationController;
import com.bigboxer23.lights.controllers.frontdoor.FrontDoorController;
import com.bigboxer23.lights.controllers.garage.GarageController;
import com.bigboxer23.lights.controllers.openHAB.OpenHABController;
import com.bigboxer23.lights.controllers.scene.DaylightController;
import com.bigboxer23.lights.controllers.scene.WeatherController;
import com.bigboxer23.lights.controllers.vera.VeraController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

/**
 * Config controlling class.  Reads JSON data from file, initializes scenes from file's contents
 */
public class HubContext
{
	protected GarageController myGarageController;

	protected FrontDoorController myFrontDoorController;

	protected WeatherController myWeatherController;

	protected DaylightController myDaylightController;

	protected NotificationController myNotificationController;

	protected VeraController myVeraController;

	protected OpenHABController myOpenHABController;

	@Autowired
	public void setGarageController(GarageController theGarageController)
	{
		myGarageController = theGarageController;
	}

	@Autowired
	public void setFrontDoorController(FrontDoorController theFrontDoorController)
	{
		myFrontDoorController = theFrontDoorController;
	}

	@Autowired
	public void setWeatherController(WeatherController theWeatherController)
	{
		myWeatherController = theWeatherController;
	}

	@Autowired
	public void setDaylightController(DaylightController theDaylightController)
	{
		myDaylightController = theDaylightController;
	}

	@Autowired
	public void setNotificationController(@Lazy NotificationController theNotificationController)
	{
		myNotificationController = theNotificationController;
	}

	@Autowired
	public void setVeraController(VeraController theVeraController)
	{
		myVeraController = theVeraController;
	}

	@Autowired
	public void setVeraController(OpenHABController theOpenHABController)
	{
		myOpenHABController = theOpenHABController;
	}
}
