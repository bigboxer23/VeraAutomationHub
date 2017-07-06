package lights.servlets;

import lights.controllers.ISystemController;
import lights.controllers.garage.GarageController;
import lights.controllers.scene.WeatherController;
import lights.controllers.vera.VeraController;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * Config controlling class.  Reads JSON data from file, initializes scenes from file's contents
 */
public abstract class AbstractControllerServlet
{
	private Map<String, ISystemController> myControllers = new HashMap<>();

	@Autowired
	public void setVeraController(VeraController theVeraController)
	{
		myControllers.put(VeraController.kControllerEndpoint, theVeraController);
	}

	@Autowired
	public void setGarageController(GarageController theGarageController)
	{
		myControllers.put(GarageController.kControllerEndpoint, theGarageController);
	}

	@Autowired
	public void setWeatherController(WeatherController theWeatherController)
	{
		myControllers.put(WeatherController.kControllerEndpoint, theWeatherController);
	}

	public <D> D getController(String theControllerKey, Class<D> theControllerClass)
	{
		ISystemController aSystemController = myControllers.get(theControllerKey);
		if (theControllerClass != null && theControllerClass.isInstance(aSystemController))
		{
			return (D) aSystemController;
		}
		return null;
	}
}
