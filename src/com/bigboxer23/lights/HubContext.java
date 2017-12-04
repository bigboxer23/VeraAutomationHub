package com.bigboxer23.lights;

import com.bigboxer23.lights.controllers.NotificationController;
import com.bigboxer23.lights.controllers.hue.HueController;
import com.bigboxer23.lights.controllers.ISystemController;
import com.bigboxer23.lights.controllers.garage.GarageController;
import com.bigboxer23.lights.controllers.scene.DaylightController;
import com.bigboxer23.lights.controllers.scene.WeatherController;
import com.bigboxer23.lights.controllers.vera.VeraController;
import com.bigboxer23.lights.data.SceneVO;

import java.util.HashMap;
import java.util.Map;

/**
 * Config controlling class.  Reads JSON data from file, initializes scenes from file's contents
 */
public class HubContext
{
	private static HubContext myInstance;

	private Map<String, ISystemController> myControllers;

	private HubContext()
	{

	}

	public static HubContext getInstance()
	{
		if (myInstance == null)
		{
			myInstance = new HubContext();
		}
		return myInstance;
	}

	/**
	 * Get our mapping of URL's to controllers
	 * If not initialized, trigger that here
	 *
	 * @return
	 */
	public Map<String, ISystemController> getControllers()
	{
		if (myControllers == null)
		{
			myControllers = new HashMap<>();
			SceneVO aGarage = new SceneVO(GarageController.kControllerEndpoint);
			GarageController aGarageController = new GarageController();
			myControllers.put(aGarage.getSceneUrl(), aGarageController);
			myControllers.put(WeatherController.kControllerEndpoint, new WeatherController(new HueController(), aGarageController));
			myControllers.put(DaylightController.kControllerEndpoint, new DaylightController());
			myControllers.put(NotificationController.kControllerEndpoint, new NotificationController());
			myControllers.put(VeraController.kControllerEndpoint, new VeraController());
		}
		return myControllers;
	}

	public <D> D getController(String theControllerKey, Class<D> theControllerClass)
	{
		ISystemController aSystemController = getControllers().get(theControllerKey);
		if (theControllerClass != null && theControllerClass.isInstance(aSystemController))
		{
			return (D) aSystemController;
		}
		return null;
	}

	/**
	 * Make us re-initialize
	 */
	public void reset()
	{
		myControllers = null;
	}

}
