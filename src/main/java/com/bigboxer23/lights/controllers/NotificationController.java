package com.bigboxer23.lights.controllers;

import com.bigboxer23.lights.controllers.vera.VeraSceneVO;
import com.bigboxer23.lights.HubContext;
import com.bigboxer23.lights.controllers.vera.VeraController;
import com.bigboxer23.lights.controllers.vera.VeraDeviceVO;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

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

	private static final int kZWaveTiming = 2000;

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
		theDevices.
			stream().
			filter(theVeraDeviceVO -> theVeraDeviceVO.getLevel() > 0).
			forEach(theDevice ->
			{
				new Thread(() -> {
					String aGetUrl = VeraController.kVeraHubUrl + VeraController.kVeraRequest + theDevice.getId() + VeraController.kVeraServiceUrn + VeraController.kDimmingCommand;
					try
					{
						doRequest(aGetUrl, theDevice.getLevel() / 3);
						Thread.sleep(kZWaveTiming);
						doRequest(aGetUrl, theDevice.getLevel());
						Thread.sleep(kZWaveTiming);
						doRequest(aGetUrl, theDevice.getLevel() / 3);
						Thread.sleep(kZWaveTiming);
						doRequest(aGetUrl, theDevice.getLevel());
					}
					catch (InterruptedException theE)
					{
						theE.printStackTrace();
					}
				}).start();
			});
	}

	private List<VeraDeviceVO> getDeviceInfo()
	{
		return HubContext.getInstance().getController(VeraController.kControllerEndpoint, VeraController.class).
				getStatus().
				getDevices().
				stream().
				filter(theDevice -> myNotificationDeviceIds.contains(theDevice.getId())).
				collect(Collectors.toList());
	}

	private void updateNotificationSceneContents()
	{
		JsonObject anElement = HubContext.getInstance().getController(VeraController.kControllerEndpoint, VeraController.class).getSceneInformation(myNotificationScene.getId());
		JsonArray aDevices = anElement.getAsJsonArray("groups").get(0).getAsJsonObject().getAsJsonArray("actions");
		myNotificationDeviceIds.clear();
		for (int ai = 0; ai < aDevices.size(); ai++)
		{
			myNotificationDeviceIds.add(aDevices.get(ai).getAsJsonObject().get("device").getAsInt());
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
