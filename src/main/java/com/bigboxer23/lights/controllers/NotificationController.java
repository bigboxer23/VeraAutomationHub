package com.bigboxer23.lights.controllers;

import com.bigboxer23.lights.HubContext;
import com.bigboxer23.lights.controllers.openHAB.OpenHABHouse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * controller for receiving a notification from some source and triggering an alert to scenes that care about it
 */
@Component
public class NotificationController extends HubContext implements ISystemController
{
	protected static final Logger myLogger = LoggerFactory.getLogger(NotificationController.class);

	@Value("${veraUrl}")
	private String kVeraHubUrl;

	public static final String kControllerEndpoint = "Notification";

	private static final int kZWaveTiming = 2500;

	private static final int kHueTiming = 400;

	@Value("${notificationGap}")
	private long myNotificationGap;

	@Value("${notificationTag}")
	private String kNotificationTag;

	private long myLastNotification = -1;

	private ThreadPoolExecutor myExecutor;

	private ThreadPoolExecutor getExecutors()
	{
		if (myExecutor == null)
		{
			myExecutor = new ThreadPoolExecutor(10, 10, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(5));
		}
		return myExecutor;
	}

	@Override
	public String doAction(List<String> theCommands)
	{
		if ((System.currentTimeMillis() - myNotificationGap * 1000 * 60) < myLastNotification)
		{
			myLogger.info("Not triggering notification, not enough gap yet.");
			return null;
		}
		myLogger.info("Notification received.");
		OpenHABHouse anItems = myOpenHABController.getItemsByTag(kNotificationTag);
		if (anItems == null || anItems.isEmpty())
		{
			myLogger.info("No items to notify.");
			return null;
		}
		myLogger.info("Doing Notification.");
		doPulseNotification(anItems);
		return null;
	}

	/**
	 * Display a "pulse" notification which pulses twice
	 *
	 * @param theItems
	 */
	private void doPulseNotification(OpenHABHouse theItems)
	{
		if (theItems.isEmpty())
		{
			return;
		}
		theItems.stream()
			.filter(theVeraDeviceVO -> theVeraDeviceVO.getIntLevel() > 0)
			.forEach(theDevice ->
			{
				myLastNotification = System.currentTimeMillis();
				myLogger.info(theDevice.getName() + " " + theDevice.getLevel());
				getExecutors().execute(() -> {
					try
					{
						myOpenHABController.setLevel(theDevice.getName(), theDevice.getIntLevel() / 3);
						Thread.sleep(kZWaveTiming);
						myOpenHABController.setLevel(theDevice.getName(), theDevice.getIntLevel());
						Thread.sleep(kZWaveTiming * 8);
						myOpenHABController.setLevel(theDevice.getName(), theDevice.getIntLevel());
					}
					catch (InterruptedException theE)
					{
						myLogger.warn("doPulseNotification", theE);
						theE.printStackTrace();
					}
				});
			});
	}
}
