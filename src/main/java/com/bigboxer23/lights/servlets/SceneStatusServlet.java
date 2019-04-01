package com.bigboxer23.lights.servlets;

import com.bigboxer23.lights.HubContext;
import com.bigboxer23.lights.controllers.elastic.ElasticAnalyticsController;
import com.bigboxer23.lights.controllers.vera.VeraHouseVO;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Get status from the vera controller for everything in the house
 */

@RestController
@EnableAutoConfiguration
public class SceneStatusServlet extends HubContext
{
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
		fillSmartRooms(aHouseStatus);
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

	private void fillSmartRooms(VeraHouseVO theHouse)
	{
		theHouse.getRooms().forEach(theVeraRoomVO ->
		{
			theVeraRoomVO.setSmart(myOpenHABController.getSmartRooms().contains("Smart" + theVeraRoomVO.getId()));
		});
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

	/**
	 * Send to elastic every 5 min
	 */
	@Scheduled(fixedDelay = 300000)
	private void updateStatus()
	{
		myElasticAnalyticsController.logStatusEvent(getHouseStatus());
	}
}
