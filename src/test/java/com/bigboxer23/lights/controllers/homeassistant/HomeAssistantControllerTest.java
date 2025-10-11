package com.bigboxer23.lights.controllers.homeassistant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.bigboxer23.utils.http.OkHttpCallback;
import com.bigboxer23.utils.http.OkHttpUtil;
import com.bigboxer23.utils.http.RequestBuilderCallback;
import okhttp3.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class HomeAssistantControllerTest {

	private HomeAssistantController controller;
	private static final String TEST_URL = "http://localhost:8123";
	private static final String TEST_TOKEN = "test-token-123";
	private static final String TEST_ENTITY_ID = "my_boolean_input";

	@BeforeEach
	void setUp() {
		controller = new HomeAssistantController();
		ReflectionTestUtils.setField(controller, "homeAssistantUrl", TEST_URL);
		ReflectionTestUtils.setField(controller, "homeAssistantToken", TEST_TOKEN);
	}

	@Test
	void testTurnOn() {
		try (MockedStatic<OkHttpUtil> mockedStatic = mockStatic(OkHttpUtil.class)) {
			controller.turnOn(TEST_ENTITY_ID);

			mockedStatic.verify(() -> OkHttpUtil.post(
					eq(TEST_URL + "/api/services/input_boolean/turn_on"),
					any(OkHttpCallback.class),
					any(RequestBuilderCallback.class)));
		}
	}

	@Test
	void testTurnOff() {
		try (MockedStatic<OkHttpUtil> mockedStatic = mockStatic(OkHttpUtil.class)) {
			controller.turnOff(TEST_ENTITY_ID);

			mockedStatic.verify(() -> OkHttpUtil.post(
					eq(TEST_URL + "/api/services/input_boolean/turn_off"),
					any(OkHttpCallback.class),
					any(RequestBuilderCallback.class)));
		}
	}

	@Test
	void testCallServiceWithCorrectHeaders() {
		try (MockedStatic<OkHttpUtil> mockedStatic = mockStatic(OkHttpUtil.class)) {
			mockedStatic
					.when(() ->
							OkHttpUtil.post(anyString(), any(OkHttpCallback.class), any(RequestBuilderCallback.class)))
					.thenAnswer(invocation -> {
						RequestBuilderCallback builderFunction = invocation.getArgument(2);
						Request.Builder builder = new Request.Builder().url(TEST_URL);
						Request.Builder result = builderFunction.modifyBuilder(builder);
						Request request = result.build();

						assert request.header("Authorization").equals("Bearer " + TEST_TOKEN);
						assert request.header("Content-Type").equals("application/json");
						return null;
					});

			controller.turnOn(TEST_ENTITY_ID);
		}
	}
}
