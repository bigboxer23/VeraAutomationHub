package com.bigboxer23.lights.controllers.hue;

import com.bigboxer23.lights.controllers.hue.data.HueAPIResponse;
import com.bigboxer23.lights.controllers.hue.data.HueResource;
import com.bigboxer23.lights.controllers.vera.VeraDeviceVO;
import com.bigboxer23.lights.controllers.vera.VeraHouseVO;
import com.bigboxer23.utils.http.OkHttpUtil;
import com.squareup.moshi.Moshi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** */
@Tag(
		name = "Hue V2 Controller",
		description = "Controller to control hue scenes or other constructs without going through the" + " native app.")
@RestController
public class HueV2Controller {

	private static final Logger logger = LoggerFactory.getLogger(HueV2Controller.class);

	private static final String BASE_URL = "https://{0}/clip/v2/{1}";

	private static final String AUTH_HEADER = "hue-application-key";

	private final Moshi moshi = new Moshi.Builder().build();

	@Value("${hueAPIKey}")
	private String API_KEY;

	/** Hue bridge address */
	@Value("${hueBridgeV2}")
	private String HUE_BRIDGE;

	@GetMapping(
			value = "/S/hue/scene/{sceneId}/{command}",
			produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "control a scene")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_UNAUTHORIZED + "", description = "missing valid token"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public void controlScene(
			@Parameter(description = "sceneId") @PathVariable(value = "sceneId") String sceneId,
			@Parameter(description = "command to run.  Possible values [0-100, ON, OFF]")
					@PathVariable(value = "command")
					String command)
			throws IOException {
		try (Response response = OkHttpUtil.putSynchronous(
				MessageFormat.format(BASE_URL, HUE_BRIDGE, "resource/scene/" + sceneId),
				RequestBody.create(
						"{\"recall\": {\"action\": \"dynamic_palette\"}}", MediaType.parse("application/json")),
				builder -> {
					builder.addHeader(AUTH_HEADER, API_KEY);
					return builder;
				},
				HueCompatibleClient.getClient())) {}
	}

	public void getSceneData(VeraHouseVO house) {
		List<HueResource> zones = getZones();
		List<HueResource> scenes = getScenes();
		house.getRooms().forEach(room -> zones.stream()
				.filter(zone -> zone.getMetadata().getName().equalsIgnoreCase(room.getName()))
				.forEach(zone -> {
					scenes.stream()
							.filter(scene -> scene.getGroup().getRid().equalsIgnoreCase(zone.getId()))
							.forEach(scene -> {
								VeraDeviceVO sceneDevice =
										new VeraDeviceVO(scene.getMetadata().getName(), -1);
								sceneDevice.setId(scene.getId());
								sceneDevice.setCategory("hc");
								room.addDevice(sceneDevice);
							});
				}));
	}

	public List<HueResource> getZones() {
		return getResource("resource/zone");
	}

	public List<HueResource> getScenes() {
		return getResource("resource/scene");
	}

	private List<HueResource> getResource(String url) {
		try (Response response = OkHttpUtil.getSynchronous(
				MessageFormat.format(BASE_URL, HUE_BRIDGE, url),
				builder -> {
					builder.addHeader(AUTH_HEADER, API_KEY);
					return builder;
				},
				HueCompatibleClient.getClient())) {
			ResponseBody body = response.body();
			return body == null
					? Collections.emptyList()
					: Optional.ofNullable(moshi.adapter(HueAPIResponse.class).fromJson(body.string()))
							.map(HueAPIResponse::getData)
							.map(Arrays::asList)
							.orElse(Collections.emptyList());
		} catch (IOException e) {
			logger.error("getResource " + url, e);
			return Collections.emptyList();
		}
	}
}
