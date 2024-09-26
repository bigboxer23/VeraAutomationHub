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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
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
	private static final Logger logger = LoggerFactory.getLogger(GCalendarController.class);

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
		fetchCalendarStatus();
	}

	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		logger.info("Getting gCal creds");
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
		logger.info("Starting local server receiver");
		return new AuthorizationCodeInstalledApp(
						aFlow, new LocalServerReceiver.Builder().setPort(8890).build())
				.authorize("user");
	}

	@Scheduled(cron = "0 0 0 ? * *") // Run every day at 12am
	private void fetchCalendarStatus() {
		logger.info("Fetching calendar information");
		try {
			NetHttpTransport aTransport = GoogleNetHttpTransport.newTrustedTransport();
			Calendar aCalendar = new Calendar.Builder(aTransport, kJSON_FACTORY, getCredentials(aTransport))
					.setApplicationName("Calendar Fetch")
					.build();
			Date dayStart = Date.from(LocalDateTime.now()
					.truncatedTo(ChronoUnit.MINUTES)
					.withMinute(0)
					.withHour(0)
					.atZone(ZoneId.systemDefault())
					.toInstant());

			Events events = aCalendar
					.events()
					.list("primary")
					.setMaxResults(25)
					.setTimeMin(new DateTime(dayStart.getTime()))
					.setTimeMax(new DateTime(dayStart.getTime() + 86400000)) // +1 day
					.setOrderBy("startTime")
					.setSingleEvents(true)
					.execute();
			myOpenHABController.setVacationMode(findMatchingEvents("Vacation", events, kVacationKeywords));
			myOpenHABController.setPTOMode(findMatchingEvents("PTO", events, kPTOKeywords));
			myOpenHABController.setExtendedEveningMode(findLateEvents(events));
			logger.info("Calendar information fetched and parsed");
		} catch (GeneralSecurityException | IOException e) {
			logger.error("fetchCalendarStatus:", e);
		}
	}

	private boolean findMatchingEvents(String theType, Events theEvents, List<String> theKeywords) {
		return theEvents.getItems().stream()
				.anyMatch(theEvent -> theKeywords.stream().anyMatch(theWord -> {
					logger.debug(theEvent.getSummary());
					boolean aFound = (theEvent.getSummary() != null
									&& theEvent.getSummary().toLowerCase().contains(theWord))
							|| (theEvent.getDescription() != null
									&& theEvent.getDescription().toLowerCase().contains(theWord));
					if (aFound) {
						logger.warn(theType + " enabled: " + theEvent.getSummary());
					}
					return aFound;
				}));
	}

	private boolean findLateEvents(Events events) {
		return events.getItems().stream().anyMatch(event -> {
			Date tenThirty = Date.from(LocalDateTime.now()
					.truncatedTo(ChronoUnit.MINUTES)
					.withMinute(30)
					.withHour(22)
					.atZone(ZoneId.of(Optional.ofNullable(event.getEnd().getTimeZone())
							.orElseGet(() -> ZoneId.systemDefault().getId())))
					.toInstant());
			boolean found = event.getEnd() != null
					&& event.getEnd().getDateTime() != null
					&& event.getEnd().getDateTime().getValue()
							>= tenThirty.toInstant().toEpochMilli();
			if (found) {
				logger.warn("findLateEvents enabled: " + event.getSummary());
			}
			return found;
		});
	}
}
