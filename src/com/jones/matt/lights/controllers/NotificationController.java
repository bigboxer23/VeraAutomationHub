package com.jones.matt.lights.controllers;

import com.jones.matt.lights.HubContext;
import com.jones.matt.lights.data.SceneVO;

import java.util.ArrayList;
import java.util.List;

/**
 * controller for receiving a notification from some source and triggering an alert to scenes that care about it
 */
public class NotificationController implements ISystemController
{
	public static final String kControllerEndpoint = "Notification";

	@Override
	public String doAction(List<String> theCommands)
	{
		for (SceneVO aScene : HubContext.getInstance().getScenes())
		{
			if (aScene.getNotificationEvents() != null && aScene.getNotificationEvents().contains(theCommands.get(0)))
			{
				theCommands = new ArrayList<>();
				theCommands.add("alert");
				HubContext.getInstance().getControllers().get(aScene.getSceneUrl()).doAction(theCommands);
			}
		}
		return null;
	}
}
