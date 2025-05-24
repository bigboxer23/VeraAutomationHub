package com.bigboxer23.lights.controllers.calendar;

import static com.bigboxer23.lights.controllers.calendar.GCalendarController.kVacationKeywords;
import static org.junit.jupiter.api.Assertions.*;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import java.util.List;
import org.junit.jupiter.api.Test;

class GCalendarControllerTest {
	private final GCalendarController controller = new GCalendarController(null, null, null, null, null, null, null);

	@Test
	void testFindMatchingEvents_found() {
		Event event = new Event().setSummary("Going on Vacation!").setDescription("Family trip to the beach.");
		assertTrue(
				controller.findMatchingEvents("Vacation", new Events().setItems(List.of(event)), kVacationKeywords),
				"Should match on 'vacation' keyword");
	}

	@Test
	void testFindMatchingEvents_notFound() {
		Event event = new Event().setSummary("Regular workday").setDescription("Just another meeting.");
		assertFalse(
				controller.findMatchingEvents("Vacation", new Events().setItems(List.of(event)), kVacationKeywords),
				"Should not match any keywords");
	}

	@Test
	void testFindMatchingEvents_safeWordBlocksWarning() {
		Event event = new Event().setSummary("Field trip with kids").setDescription("School field trip, not vacation.");
		assertFalse(
				controller.findMatchingEvents("Vacation", new Events().setItems(List.of(event)), kVacationKeywords),
				"Should match 'trip' but safe word blocks warning");
	}

	@Test
	void testFindMatchingEvents_nullsAreHandled() {
		Event event = new Event().setSummary(null).setDescription(null);
		assertFalse(
				controller.findMatchingEvents("Vacation", new Events().setItems(List.of(event)), kVacationKeywords),
				"Null summary/description should not throw or match");
	}
}
