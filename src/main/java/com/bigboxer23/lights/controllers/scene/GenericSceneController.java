package com.bigboxer23.lights.controllers.scene;

import com.bigboxer23.lights.controllers.IStatusController;
import com.bigboxer23.lights.controllers.ISystemController;
import com.bigboxer23.lights.controllers.hue.HueController;
import com.bigboxer23.lights.data.LightVO;
import com.bigboxer23.lights.data.SceneVO;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Controller for a scene (or room) consisting of multi lights, potentially
 * from multiple home automation ecosystems
 */
public class GenericSceneController implements ISystemController, IStatusController
{
	/**
	 * Controller for HUE or zigbee
	 */
	private HueController myHueController;

	private SceneVO myScene;

	private static Logger myLogger = Logger.getLogger("com.jones");

	public GenericSceneController(HueController theHueController, SceneVO theSceneVO)
	{
		myHueController = theHueController;
		myScene = theSceneVO;
	}

	@Override
	public String doAction(List<String> theCommands)
	{
		String aCommand = theCommands.get(0);
		if (aCommand.equalsIgnoreCase("off"))
		{
			doCommand("off");
		} else if (aCommand.equalsIgnoreCase("on"))
		{
			doCommand("on");
		} else if (aCommand.equalsIgnoreCase("movie"))
		{
			doCommand("movie");
		} else if (aCommand.equalsIgnoreCase("pulse"))
		{
			doCommand("pulse");
		}
		return null;
	}

	/**
	 * Iterate our list of lights.  If one has a controller that supports status
	 * fetch status from that light.  Otherwise just return true
	 *
	 * @param theLightId
	 * @return
	 */
	@Override
	public boolean getStatus(int theLightId)
	{
		for (LightVO aLight : myScene.getLights())
		{
			if (getController(aLight.getType()) instanceof IStatusController)
			{
				return ((IStatusController) getController(aLight.getType())).getStatus(aLight.getId());
			}
		}
		return true;
	}

	private void doCommand(String theCommand)
	{
		for (LightVO aLight : myScene.getLights())
		{
			if ("movie".equalsIgnoreCase(theCommand))
			{
				theCommand = aLight.getMovieModeAction();
			}
			if (theCommand != null && !theCommand.equalsIgnoreCase(""))
			{
				List<String> aCommands = new ArrayList<>();
				aCommands.add("" + aLight.getId());
				aCommands.add(theCommand);
				if (aLight.getBrightness() > 0)
				{
					aCommands.add("" + aLight.getBrightness());
				}
				getController(aLight.getType()).doAction(aCommands);
			}
		}
	}

	/**
	 * Get the appropriate controller based on the type
	 *
	 * @param theType
	 * @return
	 */
	private ISystemController getController(String theType)
	{
		if ("h".equalsIgnoreCase(theType))
		{
			return myHueController;
		}
		return null;
	}
}
