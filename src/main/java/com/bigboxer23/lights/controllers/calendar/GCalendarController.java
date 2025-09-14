package com.bigboxer23.lights.controllers.calendar;

import com.bigboxer23.lights.HubContext;
import com.bigboxer23.lights.controllers.NotificationController;
import com.bigboxer23.lights.controllers.frontdoor.FrontDoorController;
import com.bigboxer23.lights.controllers.garage.GarageController;
import com.bigboxer23.lights.controllers.openHAB.OpenHABController;
import com.bigboxer23.lights.controllers.scene.DaylightController;
import com.bigboxer23.lights.controllers.scene.WeatherController;
import com.bigboxer23.lights.controllers.vera.VeraController;
import com.bigboxer23.utils.logging.LoggingContextBuilder;
import com.bigboxer23.utils.logging.WrappingCloseable;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Check calendar once a day for PTO/Vacation items, set status of this controller to reflect.
 *
 * <p>Based off the quickstart google cal api example. credentials.json file needs to be placed into
 * src/main/resources
 */
@Slf4j
@Component
@EnableAutoConfiguration
public class GCalendarController extends HubContext {
	private static final JsonFactory kJSON_FACTORY = GsonFactory.getDefaultInstance();

	protected static List<String> kVacationKeywords = new ArrayList<>() {
		{
			add("vacation");
			add("paternity");
			add("leave");
			add("camp");
			add("trip");
			add("gone");
		}
	};

	protected static List<String> SafeWords = new ArrayList<>() {
		{
			add("field trip");
		}
	};

	protected static List<String> kPTOKeywords = new ArrayList<>() {
		{
			add("pto");
			add("no work");
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
		if (openHABController != null) {
			fetchCalendarStatus();
		}
	}

	private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
		log.info("Getting gCal creds");
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
		log.info("Starting local server receiver");
		return new AuthorizationCodeInstalledApp(
						aFlow, new LocalServerReceiver.Builder().setPort(8890).build())
				.authorize("user");
	}

	@Scheduled(cron = "0 0 0 ? * *") // Run every day at 12am
	private void fetchCalendarStatus() {
		try (WrappingCloseable c = LoggingContextBuilder.create().addTraceId().build()) {
			log.info("Fetching calendar information");
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
			log.info("Calendar information fetched and parsed");
		} catch (GeneralSecurityException | IOException e) {
			log.error("fetchCalendarStatus:", e);
		}
	}

	boolean findMatchingEvents(String type, Events events, List<String> keywords) {
		return events.getItems().stream().anyMatch(event -> {
			String summary = event.getSummary() != null ? event.getSummary().toLowerCase() : "";
			String description =
					event.getDescription() != null ? event.getDescription().toLowerCase() : "";
			log.debug(event.getSummary());
			return keywords.stream().anyMatch(word -> {
				boolean found = summary.contains(word) || description.contains(word);
 				if (found
						&& SafeWords.stream().anyMatch(safe -> summary.contains(safe) || description.contains(safe))) {
					return false;
				}
				if (found) {
					log.warn(type + " enabled: " + event.getSummary());
				}
				return found;
			});
		});
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
				log.warn("findLateEvents enabled: " + event.getSummary());
			}
			return found;
		});
	}
}
