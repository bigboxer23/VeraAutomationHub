package com.bigboxer23.lights.servlets;

import com.bigboxer23.lights.HubContext;
import com.bigboxer23.lights.controllers.climate.ClimateController;
import com.bigboxer23.lights.controllers.econet.WaterHeaterController;
import com.bigboxer23.lights.controllers.elastic.ElasticAnalyticsController;
import com.bigboxer23.lights.controllers.frontdoor.FrontDoorController;
import com.bigboxer23.lights.controllers.garage.GarageController;
import com.bigboxer23.lights.controllers.homeassistant.HomeAssistantController;
import com.bigboxer23.lights.controllers.hue.HueV2Controller;
import com.bigboxer23.lights.controllers.meural.MeuralController;
import com.bigboxer23.lights.controllers.openHAB.OpenHABController;
import com.bigboxer23.lights.controllers.scene.DaylightController;
import com.bigboxer23.lights.controllers.scene.WeatherController;
import com.bigboxer23.lights.controllers.vera.VeraController;
import com.bigboxer23.lights.controllers.vera.VeraHouseVO;
import com.bigboxer23.utils.logging.LoggingContextBuilder;
import com.bigboxer23.utils.logging.WrappingCloseable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.HttpURLConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Get status from the vera controller for everything in the house */
@RestController
@EnableAutoConfiguration
@Tag(name = "Scene Status", description = "Return a JSON object that describes the state of all the house's items")
public class SceneStatusServlet extends HubContext {
	private long myLastUpdate = -1;

	@Value("${scene.update.interval}")
	private long myUpdateInterval;

	private ElasticAnalyticsController myElasticAnalyticsController;

	private ClimateController myClimateController;

	private MeuralController meuralController;

	private HueV2Controller hueController;

	private WaterHeaterController waterHeaterController;

	protected SceneStatusServlet(
			GarageController garageController,
			FrontDoorController frontDoorController,
			WeatherController weatherController,
			DaylightController daylightController,
			VeraController veraController,
			OpenHABController openHABController,
			MeuralController meuralController,
			ElasticAnalyticsController elasticAnalyticsController,
			ClimateController climateController,
			HueV2Controller hueController,
			WaterHeaterController waterHeaterController,
			HomeAssistantController homeAssistantController) {
		super(
				garageController,
				frontDoorController,
				weatherController,
				daylightController,
				veraController,
				openHABController,
				homeAssistantController);
		this.meuralController = meuralController;
		myElasticAnalyticsController = elasticAnalyticsController;
		myClimateController = climateController;
		this.hueController = hueController;
		this.waterHeaterController = waterHeaterController;
	}

	@GetMapping(value = "/SceneStatus", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			summary = "Get object representing house's state",
			description = "Returns object that shows all devices and various state associated with them.")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_UNAUTHORIZED + "", description = "unauthorized"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public VeraHouseVO getHouseStatusJson() {
		try (WrappingCloseable c = LoggingContextBuilder.create().addTraceId().build()) {
			return getHouseStatus();
		}
	}

	private VeraHouseVO getHouseStatus() {
		VeraHouseVO aHouseStatus = new VeraHouseVO(getOpenHABController().getStatus());
		fillMotionOverrides(aHouseStatus);
		getGarageController().getStatus(aHouseStatus);
		myClimateController.getClimateData(aHouseStatus);
		getFrontDoorController().getStatus(aHouseStatus);
		meuralController.getStatus(aHouseStatus);
		hueController.getSceneData(aHouseStatus);
		waterHeaterController.notifyActive();
		waterHeaterController.getStatus(aHouseStatus);
		fillSmartRooms(aHouseStatus);
		return aHouseStatus;
	}

	private void fillSmartRooms(VeraHouseVO theHouse) {
		theHouse.getRooms().forEach(theVeraRoomVO -> {
			theVeraRoomVO.setSmart(getOpenHABController().getSmartRooms().contains("Smart" + theVeraRoomVO.getId()));
		});
	}

	/**
	 * Looks through room list fetched from OpenHAB, if finds one named MotionOverrides, will place
	 * items from that room into appropriate rooms based on the device naming schemes. Necessary to
	 * place motion overrides within the proper places in the room tree without actually attaching
	 * them to the room (so they're not turned on if the entire room is set to on)
	 *
	 * @param theHouse
	 */
	private void fillMotionOverrides(VeraHouseVO theHouse) {
		theHouse.getRooms().stream()
				.filter(theRoom -> theRoom.getName().equalsIgnoreCase("MotionOverrides"))
				.findAny()
				.ifPresent(theMotionOverride -> {
					theHouse.getRooms().remove(theMotionOverride);
					theMotionOverride.getDevices().forEach(theDevice -> {
						String aName = theDevice.getName().replace(" Motion Override", "");
						theHouse.getRooms().stream()
								.filter(theVeraRoomVO -> theVeraRoomVO.getName().equalsIgnoreCase(aName))
								.findAny()
								.ifPresent(theVeraRoomVO ->
										theVeraRoomVO.getDevices().add(theDevice));
					});
				});
	}

	/** Send to elastic every 5 min */
	@Scheduled(fixedDelay = 300000)
	private void updateStatus() {
		try (WrappingCloseable c = LoggingContextBuilder.create().addTraceId().build()) {
			myElasticAnalyticsController.logStatusEvent(getHouseStatus());
		}
	}
}
