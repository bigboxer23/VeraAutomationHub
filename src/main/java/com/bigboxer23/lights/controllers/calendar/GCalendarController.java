package com.bigboxer23.lights.controllers.calendar;

import com.bigboxer23.lights.HubContext;
import com.bigboxer23.lights.controllers.NotificationController;
import com.bigboxer23.lights.controllers.frontdoor.FrontDoorController;
import com.bigboxer23.lights.controllers.garage.GarageController;
import com.bigboxer23.lights.controllers.openHAB.OpenHABController;
import com.bigboxer23.lights.controllers.scene.DaylightController;
import com.bigboxer23.lights.controllers.scene.WeatherController;
import com.bigboxer23.lights.controllers.vera.VeraController;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Events;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Check calendar once a day for PTO/Vacation items, set status of this controller to reflect.
 *
 * <p>Based off the quickstart google cal api example. credentials.json file needs to be placed into
 * src/main/resources
 */
@Component
@EnableAutoConfiguration
public class GCalendarController extends HubContext {
	private static final Logger myLogger = LoggerFactory.getLogger(GCalendarController.class);

	private static final JsonFactory kJSON_FACTORY = GsonFactory.getDefaultInstance();

	private static List<String> kVacationKeywords = new ArrayList<String>() {
		{
			add("vacation");
			add("paternity");
			add("leave");
			add("camp");
			add("trip");
			add("gone");
		}
	};

	private static List<String> kPTOKeywords = new ArrayList<String>() {
		{
			add("pto");
		}
	};

	protected GCalendarController(
			GarageController garageController,
			FrontDoorController frontDoorController,
			WeatherController weatherController,
			DaylightController daylightController,
			NotificationController notificationController,
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

	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		myLogger.info("Getting gCal creds");
		// Load client secrets.
		InputStream aCredStream = GCalendarController.class.getResourceAsStream("/credentials.json");
		GoogleClientSecrets aClientSecrets =
				GoogleClientSecrets.load(kJSON_FACTORY, new InputStreamReader(aCredStream));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow aFlow = new GoogleAuthorizationCodeFlow.Builder(
						HTTP_TRANSPORT,
						kJSON_FACTORY,
						aClientSecrets,
						Collections.singletonList(CalendarScopes.CALENDAR_READONLY))
				.setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
				.setAccessType("offline")
				.build();
		myLogger.info("Starting local server receiver");
		return new AuthorizationCodeInstalledApp(
						aFlow, new LocalServerReceiver.Builder().setPort(8890).build())
				.authorize("user");
	}

	@Scheduled(cron = "0 0 0 ? * *") // Run every day at 12am
	private void fetchCalendarStatus() {
		myLogger.info("Fetching calendar information");
		try {
			NetHttpTransport aTransport = GoogleNetHttpTransport.newTrustedTransport();
			Calendar aCalendar = new Calendar.Builder(aTransport, kJSON_FACTORY, getCredentials(aTransport))
					.setApplicationName("Calendar Fetch")
					.build();
			Events anEvents = aCalendar
					.events()
					.list("primary")
					.setMaxResults(25)
					.setTimeMin(new DateTime(System.currentTimeMillis()))
					.setTimeMax(new DateTime(System.currentTimeMillis() + 86400000)) // +1 day
					.setOrderBy("startTime")
					.setSingleEvents(true)
					.execute();
			anEvents.getItems().forEach(theEvent -> System.out.println(theEvent.getSummary()));
			myOpenHABController.setVacationMode(findMatchingEvents("Vacation", anEvents, kVacationKeywords));
			myOpenHABController.setPTOMode(findMatchingEvents("PTO", anEvents, kPTOKeywords));
			myLogger.info("Calendar information fetched and parsed");
		} catch (GeneralSecurityException | IOException theE) {
			theE.printStackTrace();
		}
	}

	private boolean findMatchingEvents(String theType, Events theEvents, List<String> theKeywords) {
		return theEvents.getItems().stream()
				.anyMatch(theEvent -> theKeywords.stream().anyMatch(theWord -> {
					myLogger.debug(theEvent.getSummary());
					boolean aFound = (theEvent.getSummary() != null
									&& theEvent.getSummary().toLowerCase().contains(theWord))
							|| (theEvent.getDescription() != null
									&& theEvent.getDescription().toLowerCase().contains(theWord));
					if (aFound) {
						myLogger.warn(theType + " enabled: " + theEvent.getSummary());
					}
					return aFound;
				}));
	}
}
