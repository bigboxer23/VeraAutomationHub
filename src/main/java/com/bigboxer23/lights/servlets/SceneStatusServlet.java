package com.bigboxer23.lights.servlets;

import com.bigboxer23.lights.HubContext;
import com.bigboxer23.lights.controllers.elastic.ElasticAnalyticsController;
import com.bigboxer23.lights.controllers.vera.VeraHouseVO;
import com.bigboxer23.lights.controllers.vera.VeraSceneVO;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Get status from the vera controller for everything in the house
 */

@RestController
@EnableAutoConfiguration
public class SceneStatusServlet extends HubContext
{
	private static final String kLevelSetSceneName = System.getProperty("level.set.scene.name", "LevelSet");

	private static final Logger myLogger = LoggerFactory.getLogger(SceneStatusServlet.class);

	private long myLastUpdate = -1;

	@Value("${scene.update.interval}")
	private long myUpdateInterval;

	private ElasticAnalyticsController myElasticAnalyticsController;

	@Autowired
	public void setGarageController(ElasticAnalyticsController theElasticAnalyticsController)
	{
		myElasticAnalyticsController = theElasticAnalyticsController;
	}

	/**
	 * Device id, load level
	 */
	private Map<Integer, Integer> mySpecificDimLevels = new HashMap<>();

	@RequestMapping(value = "/SceneStatus", produces = {MediaType.APPLICATION_JSON_VALUE})
	public String getHouseStatusJson()
	{
		return new Gson().toJson(getHouseStatus());
	}

	private VeraHouseVO getHouseStatus()
	{
		VeraHouseVO aHouseStatus = new VeraHouseVO(myOpenHABController.getStatus());
		fillMotionOverrides(aHouseStatus);
		myGarageController.getStatus(aHouseStatus);
		/*Optional.of(aHouseStatus).
				map(VeraHouseVO::getScenes).
				ifPresent(theVeraSceneVOS -> theVeraSceneVOS.
						stream().
						filter(theScene -> theScene.getName().equalsIgnoreCase(kLevelSetSceneName)).
						findAny().
						ifPresent(this::setupLevels));*/
		//fillLevels(aHouseStatus);
		return aHouseStatus;
	}

	/**
	 * Looks through room list fetched from OpenHAB, if finds one named MotionOverrides, will place items from that
	 * room into appropriate rooms based on the device naming schemes.  Necessary to place motion overrides within the
	 * proper places in the room tree without actually attaching them to the room (so they're not turned on if the
	 * entire room is set to on)
	 *
	 * @param theHouse
	 */
	private void fillMotionOverrides(VeraHouseVO theHouse)
	{
		theHouse
				.getRooms()
				.stream()
				.filter(theRoom -> theRoom.getName().equalsIgnoreCase("MotionOverrides"))
				.findAny()
				.ifPresent(theMotionOverride ->
				{
					theHouse.getRooms().remove(theMotionOverride);
					theMotionOverride.getDevices().forEach(theDevice ->
					{
						String aName = theDevice.getName().replace(" Motion Override", "");
						theHouse
								.getRooms()
								.stream()
								.filter(theVeraRoomVO -> theVeraRoomVO.getName().equalsIgnoreCase(aName))
								.findAny()
								.ifPresent(theVeraRoomVO -> theVeraRoomVO.getDevices().add(theDevice));
					});
				});
	}

	private void fillLevels(VeraHouseVO theHouse)
	{
			theHouse.getDevices().stream().
					filter(theDeviceVO -> mySpecificDimLevels.containsKey(theDeviceVO.getId())).
					forEach(theDeviceVO -> theDeviceVO.setDefinedDim(mySpecificDimLevels.get(theDeviceVO.getId())));
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
		if (myLastUpdate < System.currentTimeMillis() - myUpdateInterval)
		{
			myLogger.debug("Fetching new levels---------------------");
			myLastUpdate = System.currentTimeMillis();
			JsonObject anElement = myVeraController.getSceneInformation(theVO.getId());
			JsonArray aDevices = anElement.getAsJsonArray("groups").get(0).getAsJsonObject().getAsJsonArray("actions");
			for (int ai = 0; ai < aDevices.size(); ai++)
			{
				JsonObject aDevice = aDevices.get(ai).getAsJsonObject();
				int aDeviceId = aDevice.get("device").getAsInt();
				int aDimLevel = aDevice.get("arguments").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsInt();
				int aPrevious = mySpecificDimLevels.getOrDefault(aDeviceId, -1);
				if (aPrevious != aDimLevel)
				{
					myLogger.error("device: " + aDeviceId + " set from " + aPrevious + " to " + aDimLevel);
					mySpecificDimLevels.put(aDeviceId, aDimLevel);
				}
			}
		}
	}

	/**
	 * Send to elastic every 5 min
	 */
	@Scheduled(fixedDelay = 300000)
	private void updateStatus()
	{
		myElasticAnalyticsController.logStatusEvent(getHouseStatus());
	}
}
