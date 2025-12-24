package com.bigboxer23.lights.controllers.homeassistant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.bigboxer23.lights.data.HomeAssistantEntity;
import com.bigboxer23.utils.http.OkHttpCallback;
import com.bigboxer23.utils.http.OkHttpUtil;
import com.bigboxer23.utils.http.RequestBuilderCallback;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import okhttp3.*;
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
	void testSetStateTrue() {
		try (MockedStatic<OkHttpUtil> mockedStatic = mockStatic(OkHttpUtil.class)) {
			controller.setState(TEST_ENTITY_ID, true);

			mockedStatic.verify(() -> OkHttpUtil.post(
					eq(TEST_URL + "/api/services/input_boolean/turn_on"),
					any(OkHttpCallback.class),
					any(RequestBuilderCallback.class)));
		}
	}

	@Test
	void testSetStateFalse() {
		try (MockedStatic<OkHttpUtil> mockedStatic = mockStatic(OkHttpUtil.class)) {
			controller.setState(TEST_ENTITY_ID, false);

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

	@Test
	void testGetAllStatesSuccess() throws IOException {
		String jsonResponse = "[{\"entity_id\":\"sensor.temperature\",\"state\":\"23.5\"},"
				+ "{\"entity_id\":\"binary_sensor.door\",\"state\":\"on\"}]";

		Response mockResponse = createMockResponse(200, jsonResponse);

		try (MockedStatic<OkHttpUtil> mockedStatic = mockStatic(OkHttpUtil.class)) {
			mockedStatic
					.when(() -> OkHttpUtil.getSynchronous(anyString(), any(RequestBuilderCallback.class)))
					.thenReturn(mockResponse);

			List<HomeAssistantEntity> entities = controller.getAllStates();

			assertNotNull(entities);
			assertEquals(2, entities.size());
			assertEquals("sensor.temperature", entities.get(0).getEntityId());
			assertEquals("23.5", entities.get(0).getState());
			assertEquals("binary_sensor.door", entities.get(1).getEntityId());
			assertEquals("on", entities.get(1).getState());
		}
	}

	@Test
	void testGetAllStatesFailedHttpResponse() throws IOException {
		Response mockResponse = createMockResponse(500, "Internal Server Error");

		try (MockedStatic<OkHttpUtil> mockedStatic = mockStatic(OkHttpUtil.class)) {
			mockedStatic
					.when(() -> OkHttpUtil.getSynchronous(anyString(), any(RequestBuilderCallback.class)))
					.thenReturn(mockResponse);

			List<HomeAssistantEntity> entities = controller.getAllStates();

			assertNotNull(entities);
			assertTrue(entities.isEmpty());
		}
	}

	@Test
	void testGetAllStatesIOException() throws IOException {
		try (MockedStatic<OkHttpUtil> mockedStatic = mockStatic(OkHttpUtil.class)) {
			mockedStatic
					.when(() -> OkHttpUtil.getSynchronous(anyString(), any(RequestBuilderCallback.class)))
					.thenThrow(new IOException("Network error"));

			List<HomeAssistantEntity> entities = controller.getAllStates();

			assertNotNull(entities);
			assertTrue(entities.isEmpty());
		}
	}

	@Test
	void testGetAllStatesMalformedJson() throws IOException {
		Response mockResponse = createMockResponse(200, "{malformed json");

		try (MockedStatic<OkHttpUtil> mockedStatic = mockStatic(OkHttpUtil.class)) {
			mockedStatic
					.when(() -> OkHttpUtil.getSynchronous(anyString(), any(RequestBuilderCallback.class)))
					.thenReturn(mockResponse);

			List<HomeAssistantEntity> entities = controller.getAllStates();

			assertNotNull(entities);
			assertTrue(entities.isEmpty());
		}
	}

	@Test
	void testGetAllStatesEmptyList() throws IOException {
		Response mockResponse = createMockResponse(200, "[]");

		try (MockedStatic<OkHttpUtil> mockedStatic = mockStatic(OkHttpUtil.class)) {
			mockedStatic
					.when(() -> OkHttpUtil.getSynchronous(anyString(), any(RequestBuilderCallback.class)))
					.thenReturn(mockResponse);

			List<HomeAssistantEntity> entities = controller.getAllStates();

			assertNotNull(entities);
			assertTrue(entities.isEmpty());
		}
	}

	@Test
	void testGetEntityStateFound() throws IOException {
		String jsonResponse = "[{\"entity_id\":\"sensor.temperature\",\"state\":\"23.5\"},"
				+ "{\"entity_id\":\"binary_sensor.door\",\"state\":\"on\"}]";

		Response mockResponse = createMockResponse(200, jsonResponse);

		try (MockedStatic<OkHttpUtil> mockedStatic = mockStatic(OkHttpUtil.class)) {
			mockedStatic
					.when(() -> OkHttpUtil.getSynchronous(anyString(), any(RequestBuilderCallback.class)))
					.thenReturn(mockResponse);

			Optional<HomeAssistantEntity> entity = controller.getEntityState("sensor.temperature");

			assertTrue(entity.isPresent());
			assertEquals("sensor.temperature", entity.get().getEntityId());
			assertEquals("23.5", entity.get().getState());
		}
	}

	@Test
	void testGetEntityStateNotFound() throws IOException {
		String jsonResponse = "[{\"entity_id\":\"sensor.temperature\",\"state\":\"23.5\"}]";

		Response mockResponse = createMockResponse(200, jsonResponse);

		try (MockedStatic<OkHttpUtil> mockedStatic = mockStatic(OkHttpUtil.class)) {
			mockedStatic
					.when(() -> OkHttpUtil.getSynchronous(anyString(), any(RequestBuilderCallback.class)))
					.thenReturn(mockResponse);

			Optional<HomeAssistantEntity> entity = controller.getEntityState("sensor.nonexistent");

			assertFalse(entity.isPresent());
		}
	}

	@Test
	void testGetEntityStateWhenGetAllStatesFails() throws IOException {
		Response mockResponse = createMockResponse(500, "Error");

		try (MockedStatic<OkHttpUtil> mockedStatic = mockStatic(OkHttpUtil.class)) {
			mockedStatic
					.when(() -> OkHttpUtil.getSynchronous(anyString(), any(RequestBuilderCallback.class)))
					.thenReturn(mockResponse);

			Optional<HomeAssistantEntity> entity = controller.getEntityState("sensor.temperature");

			assertFalse(entity.isPresent());
		}
	}

	private Response createMockResponse(int code, String body) throws IOException {
		ResponseBody responseBody = ResponseBody.create(body, MediaType.parse("application/json"));
		return new Response.Builder()
				.request(new Request.Builder().url(TEST_URL).build())
				.protocol(Protocol.HTTP_1_1)
				.code(code)
				.message("Response")
				.body(responseBody)
				.build();
	}
}
