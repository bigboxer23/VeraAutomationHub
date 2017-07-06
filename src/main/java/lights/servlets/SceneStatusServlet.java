package lights.servlets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lights.controllers.garage.GarageController;
import lights.controllers.vera.VeraController;
import lights.controllers.vera.VeraHouseVO;
import lights.controllers.vera.VeraSceneVO;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Get status from the vera controller for everything in the house
 */
@RestController
@EnableAutoConfiguration
public class SceneStatusServlet extends AbstractControllerServlet
{
	private static final String kLevelSetSceneName = System.getProperty("level.set.scene.name", "LevelSet");

	private long myLastUpdate = -1;
	/**
	 * Device id, load level
	 */
	private Map<Integer, Integer> mySpecificDimLevels;

	@RequestMapping("/SceneStatus")
	public VeraHouseVO getStatus(HttpServletResponse theResponse) throws ServletException, IOException
	{
		VeraHouseVO aHouseStatus = getController(VeraController.kControllerEndpoint, VeraController.class).getStatus();
		getController(GarageController.kControllerEndpoint, GarageController.class).getStatus(aHouseStatus);
		aHouseStatus.getScenes().stream().filter(theScene -> theScene.getName().equalsIgnoreCase(kLevelSetSceneName)).findAny().ifPresent(this::setupLevels);
		fillLevels(aHouseStatus);
		aHouseStatus.getScenes().clear();
		aHouseStatus.getDevices().clear();
		return aHouseStatus;
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
			JsonObject anElement = getController(VeraController.kControllerEndpoint, VeraController.class).getSceneInformation(theVO.getId());
			JsonArray aDevices = anElement.get("groups").getAsJsonArray().get(0).getAsJsonObject().get("actions").getAsJsonArray();
			aDevices.forEach(theDevice ->
			{
				JsonObject aDevice = theDevice.getAsJsonObject();
				mySpecificDimLevels.put(aDevice.get("device").getAsInt(), aDevice.getAsJsonArray("arguments").get(0).getAsJsonObject().get("value").getAsInt());
			});
		}
	}
}
