package com.bigboxer23.lights.controllers;

import com.bigboxer23.lights.HubContext;
import com.bigboxer23.lights.controllers.frontdoor.FrontDoorController;
import com.bigboxer23.lights.controllers.garage.GarageController;
import com.bigboxer23.lights.controllers.openHAB.OpenHABController;
import com.bigboxer23.lights.controllers.openHAB.OpenHABItem;
import com.bigboxer23.lights.controllers.scene.DaylightController;
import com.bigboxer23.lights.controllers.scene.WeatherController;
import com.bigboxer23.lights.controllers.vera.VeraController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * controller for receiving a notification from some source and triggering an alert to scenes that
 * care about it
 */
@Tag(name = "Notification Controller", description = "Service to fire pulsing notifications to devices")
@RestController
public class NotificationController extends HubContext {
	protected static final Logger myLogger = LoggerFactory.getLogger(NotificationController.class);

	@Value("${veraUrl}")
	private String kVeraHubUrl;

	private static final int kZWaveTiming = 2500;

	private static final int kHueTiming = 400;

	@Value("${notificationGap}")
	private long myNotificationGap;

	@Value("${notificationTag}")
	private String kNotificationTag;

	private long myLastNotification = -1;

	private ThreadPoolExecutor myExecutor;

	protected NotificationController(
			GarageController garageController,
			FrontDoorController frontDoorController,
			WeatherController weatherController,
			DaylightController daylightController,
			VeraController veraController,
			OpenHABController openHABController) {
		super(
				garageController,
				frontDoorController,
				weatherController,
				daylightController,
				veraController,
				openHABController);
	}

	private ThreadPoolExecutor getExecutors() {
		if (myExecutor == null) {
			myExecutor = new ThreadPoolExecutor(10, 10, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(5));
		}
		return myExecutor;
	}

	@GetMapping(
			value = {"/S/Notification", "/S/Notification/{deviceId}"},
			produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			summary = "Trigger a notification",
			description = "Fire notification tagged devices with a pulse. Can provide a specific device"
					+ " to notify if using the defined notification device(s) isn't waht is"
					+ " wanted")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_UNAUTHORIZED + "", description = "unauthorized"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public String doAction(
			@Parameter(description = "optional device id to do a notification to")
					@PathVariable(value = "deviceId", required = false)
					String deviceId) {
		if ((System.currentTimeMillis() - myNotificationGap * 1000 * 60) < myLastNotification) {
			myLogger.info("Not triggering notification, not enough gap yet.");
			return null;
		}
		myLogger.info("Notification received.");
		List<OpenHABItem> anItems = getItems(deviceId);
		if (anItems == null || anItems.isEmpty()) {
			myLogger.info("No items to notify.");
			return null;
		}
		myLogger.info("Doing Notification.");
		doPulseNotification(anItems);
		return null;
	}

	private List<OpenHABItem> getItems(String deviceId) {
		return myOpenHABController.getItemsByTag(deviceId == null || deviceId.isEmpty() ? kNotificationTag : deviceId);
	}

	/**
	 * Display a "pulse" notification which pulses twice
	 *
	 * @param theItems
	 */
	private void doPulseNotification(List<OpenHABItem> theItems) {
		if (theItems.isEmpty()) {
			return;
		}
		theItems.stream()
				.filter(theVeraDeviceVO -> theVeraDeviceVO.getIntLevel() > 0)
				.forEach(theDevice -> {
					myLastNotification = System.currentTimeMillis();
					myLogger.info(theDevice.getName() + " " + theDevice.getLevel());
					getExecutors().execute(() -> {
						try {
							myOpenHABController.setLevel(theDevice.getName(), theDevice.getIntLevel() / 2);
							Thread.sleep(kZWaveTiming);
							myOpenHABController.setLevel(theDevice.getName(), theDevice.getIntLevel());
							Thread.sleep(kZWaveTiming * 8);
							myOpenHABController.setLevel(theDevice.getName(), theDevice.getIntLevel());
						} catch (InterruptedException theE) {
							myLogger.warn("doPulseNotification", theE);
							theE.printStackTrace();
						}
					});
				});
	}
}
