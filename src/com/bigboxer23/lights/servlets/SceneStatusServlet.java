package com.bigboxer23.lights.servlets;

import com.google.gson.Gson;
import com.bigboxer23.lights.HubContext;
import com.bigboxer23.lights.controllers.garage.GarageController;
import com.bigboxer23.lights.controllers.vera.VeraController;
import com.bigboxer23.lights.controllers.vera.VeraHouseVO;
import com.bigboxer23.lights.controllers.vera.VeraSceneVO;
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
		Map<Integer, Integer> aLevels = mySpecificDimLevels;
			theHouse.getDevices().stream().
					filter(theDeviceVO -> aLevels.containsKey(theDeviceVO.getId())).
					forEach(theDeviceVO -> theDeviceVO.setDefinedDim(aLevels.get(theDeviceVO.getId())));
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
			Map<Integer, Integer> aLevels = new HashMap<>();
			JSONObject anElement = HubContext.getInstance().getController(VeraController.kControllerEndpoint, VeraController.class).getSceneInformation(theVO.getId());
			JSONArray aDevices = anElement.getJSONArray("groups").getJSONObject(0).getJSONArray("actions");
			for (int ai = 0; ai < aDevices.length(); ai++)
			{
				JSONObject aDevice = aDevices.getJSONObject(ai);
				aLevels.put(aDevice.getInt("device"), aDevice.getJSONArray("arguments").getJSONObject(0).getInt("value"));
			}
			mySpecificDimLevels = aLevels;
		}
	}
}
