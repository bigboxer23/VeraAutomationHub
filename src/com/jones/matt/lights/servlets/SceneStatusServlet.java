package com.jones.matt.lights.servlets;

import com.google.gson.Gson;
import com.jones.matt.lights.HubContext;
import com.jones.matt.lights.controllers.garage.GarageController;
import com.jones.matt.lights.controllers.vera.VeraController;
import com.jones.matt.lights.controllers.vera.VeraDeviceVO;
import com.jones.matt.lights.controllers.vera.VeraHouseVO;
import com.jones.matt.lights.controllers.vera.VeraSceneVO;
import org.json.hue.JSONArray;
import org.json.hue.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Get status from the vera controller for everything in the house
 */
public class SceneStatusServlet extends AbstractServlet
{
	private static final String kLevelSetSceneName = System.getProperty("level.set.scene.name", "LevelSet");

	private long myLastUpdate = -1;
	/**
	 * Device id, load level
	 */
	private Map<Integer, Integer> mySpecificDimLevels;

	@Override
	public void process(HttpServletRequest theRequest, HttpServletResponse theResponse) throws ServletException, IOException
	{
		theResponse.setContentType("application/json");
		VeraHouseVO aHouseStatus = HubContext.getInstance().getController(VeraController.kControllerEndpoint, VeraController.class).getStatus();
		HubContext.getInstance().getController(GarageController.kControllerEndpoint, GarageController.class).getStatus(aHouseStatus);
		aHouseStatus.getScenes().stream().filter(theScene -> theScene.getName().equalsIgnoreCase(kLevelSetSceneName)).findAny().ifPresent(this::setupLevels);
		fillLevels(aHouseStatus);
		aHouseStatus.getScenes().clear();
		aHouseStatus.getDevices().clear();
		theResponse.getOutputStream().print(new Gson().toJson(aHouseStatus));
		theResponse.getOutputStream().flush();
		theResponse.getOutputStream().close();
	}

	private void fillLevels(VeraHouseVO theHouse)
	{
		if (mySpecificDimLevels != null)
		{
			theHouse.getDevices().stream().
					filter(theDeviceVO -> mySpecificDimLevels.containsKey(theDeviceVO.getId())).
					forEach(theDeviceVO -> theDeviceVO.setDefinedDim(mySpecificDimLevels.get(theDeviceVO.getId())));
		}
	}

	/**
	 * Every minute query the server for scene information which we can gain custom on/off/dim percentage for
	 * each room's lights.  If a dim is set, or off, then we use that or don't turn that light on with the
	 * containing room (individual light requests still perform as expected
	 *
	 * @param theVO
	 */
	private void setupLevels(VeraSceneVO theVO)
	{
		if (myLastUpdate < System.currentTimeMillis() - 1000 * 60)
		{
			myLastUpdate = System.currentTimeMillis();
			if (mySpecificDimLevels == null)
			{
				mySpecificDimLevels = new HashMap<>();
			}
			mySpecificDimLevels.clear();
			JSONObject anElement = HubContext.getInstance().getController(VeraController.kControllerEndpoint, VeraController.class).getSceneInformation(theVO.getId());
			JSONArray aDevices = anElement.getJSONArray("groups").getJSONObject(0).getJSONArray("actions");
			for (int ai = 0; ai < aDevices.length(); ai++)
			{
				JSONObject aDevice = aDevices.getJSONObject(ai);
				mySpecificDimLevels.put(aDevice.getInt("device"), aDevice.getJSONArray("arguments").getJSONObject(0).getInt("value"));
			}
		}
	}
}
