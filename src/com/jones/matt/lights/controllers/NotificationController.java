package com.jones.matt.lights.controllers;

import com.jones.matt.lights.HubContext;
import com.jones.matt.lights.controllers.vera.VeraController;
import com.jones.matt.lights.controllers.vera.VeraDeviceVO;
import com.jones.matt.lights.controllers.vera.VeraSceneVO;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.hue.JSONArray;
import org.json.hue.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * controller for receiving a notification from some source and triggering an alert to scenes that care about it
 */
public class NotificationController implements ISystemController
{
	public static final String kControllerEndpoint = "Notification";

	private static final String kNotificationSceneName = System.getProperty("notification.scene", "Notify");

	private static final int kZWaveTiming = 1500;

	private static final int kHueTiming = 400;

	private VeraSceneVO myNotificationScene;

	private List<Integer> myNotificationDeviceIds = new ArrayList<>();

	@Override
	public String doAction(List<String> theCommands)
	{
		if (HubContext.getInstance().getController(VeraController.kControllerEndpoint, VeraController.class).getStatus() == null)
		{
			return null;
		}
		if (!updateNotificationSceneId())
		{
			return null;
		}
		doPulseNotification(getDeviceInfo());
		updateNotificationSceneContents();
		return null;
	}

	/**
	 * Display a "pulse" notification which pulses twice
	 *
	 * @param theDevices
	 */
	private void doPulseNotification(List<VeraDeviceVO> theDevices)
	{
		new Thread(() -> {
			try
			{
				doNotification(theDevices, 3);
				Thread.sleep(kZWaveTiming);
				doNotification(theDevices, 1);
				Thread.sleep(kZWaveTiming);
				doNotification(theDevices, 3);
				Thread.sleep(kZWaveTiming);
				doNotification(theDevices, 1);
			}
			catch (InterruptedException theE)
			{
				//theE.printStackTrace();
			}
		}).start();
	}

	/**
	 * Set a specific level for devices.
	 *
	 * @param theDevices
	 * @param theLevelDivisor  Takes device current level and divides by this value to set a new level
	 */
	private void doNotification(List<VeraDeviceVO> theDevices, int theLevelDivisor)
	{
		theDevices.
				stream().
				filter(theVeraDeviceVO -> theVeraDeviceVO.getLevel() > 0).
				forEach(theDevice ->
				{
					String aGetUrl = VeraController.kVeraHubUrl + VeraController.kVeraRequest + theDevice.getId() + VeraController.kVeraServiceUrn + VeraController.kDimmingCommand;
					doRequest(aGetUrl, theDevice.getLevel() / theLevelDivisor);
				});
	}

	private List<VeraDeviceVO> getDeviceInfo()
	{
		return HubContext.getInstance().getController(VeraController.kControllerEndpoint, VeraController.class).
				getStatus().
				getDevices().stream().filter(theDevice ->
					{
						return myNotificationDeviceIds.contains(theDevice.getId());
					}).
				collect(Collectors.toList());
	}

	private void updateNotificationSceneContents()
	{
		JSONObject anElement = HubContext.getInstance().getController(VeraController.kControllerEndpoint, VeraController.class).getSceneInformation(myNotificationScene.getId());
		JSONArray aDevices = anElement.getJSONArray("groups").getJSONObject(0).getJSONArray("actions");
		for (int ai = 0; ai < aDevices.length(); ai++)
		{
			myNotificationDeviceIds.add(aDevices.getJSONObject(ai).getInt("device"));
		}
	}

	/**
	 * Find the updated scene... return true if we've found or previously found one
	 *
	 * @return true means we have found a valid notification scene
	 */
	private boolean updateNotificationSceneId()
	{
		HubContext.getInstance().getController(VeraController.kControllerEndpoint, VeraController.class)
				.getStatus().
				getScenes().
				stream().
				filter(theVeraSceneVO -> theVeraSceneVO.getName().equalsIgnoreCase(kNotificationSceneName)).
				findAny().
				ifPresent(theScene -> myNotificationScene = theScene);

		return myNotificationScene != null;
	}

	private static void doRequest(String theUrl, int theLevel)
	{
		DefaultHttpClient aHttpClient = new DefaultHttpClient();
		try
		{
			aHttpClient.execute(new HttpGet(theUrl + theLevel));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
