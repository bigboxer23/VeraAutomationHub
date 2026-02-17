package com.bigboxer23.lights.controllers.econet;

import static org.mockito.Mockito.*;

import com.bigboxer23.lights.controllers.homeassistant.HomeAssistantController;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WaterHeaterControllerTest {

	@Mock
	private HomeAssistantController homeAssistantController;

	private WaterHeaterController controller;

	@BeforeEach
	void setUp() {
		controller = new WaterHeaterController(homeAssistantController);
	}

	@Test
	void fetchExecutesWhenLastFetchIsOlderThan5Minutes() {
		when(homeAssistantController.getAllStates()).thenReturn(Collections.emptyList());

		ReflectionTestUtils.invokeMethod(controller, "fetchWaterHeaterStatus");

		verify(homeAssistantController).getAllStates();
	}

	@Test
	void fetchSkipsWhenUiIdleAndRecentFetch() {
		ReflectionTestUtils.setField(controller, "lastFetchTime", System.currentTimeMillis());

		ReflectionTestUtils.invokeMethod(controller, "fetchWaterHeaterStatus");

		verify(homeAssistantController, never()).getAllStates();
	}

	@Test
	void fetchExecutesWhenUiIsActive() {
		ReflectionTestUtils.setField(controller, "lastFetchTime", System.currentTimeMillis());
		controller.notifyActive();

		when(homeAssistantController.getAllStates()).thenReturn(Collections.emptyList());

		ReflectionTestUtils.invokeMethod(controller, "fetchWaterHeaterStatus");

		verify(homeAssistantController).getAllStates();
	}

	@Test
	void fetchExecutesWhenUiActivityExpiredButLastFetchIsStale() {
		ReflectionTestUtils.setField(controller, "lastActiveTime", System.currentTimeMillis() - 180000);
		ReflectionTestUtils.setField(controller, "lastFetchTime", System.currentTimeMillis() - 360000);

		when(homeAssistantController.getAllStates()).thenReturn(Collections.emptyList());

		ReflectionTestUtils.invokeMethod(controller, "fetchWaterHeaterStatus");

		verify(homeAssistantController).getAllStates();
	}

	@Test
	void notifyActiveMakesSubsequentFetchExecute() {
		ReflectionTestUtils.setField(controller, "lastFetchTime", System.currentTimeMillis());

		ReflectionTestUtils.invokeMethod(controller, "fetchWaterHeaterStatus");
		verify(homeAssistantController, never()).getAllStates();

		controller.notifyActive();
		when(homeAssistantController.getAllStates()).thenReturn(Collections.emptyList());

		ReflectionTestUtils.invokeMethod(controller, "fetchWaterHeaterStatus");
		verify(homeAssistantController).getAllStates();
	}
}
