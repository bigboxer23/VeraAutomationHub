package com.bigboxer23.lights.controllers.scene;

import com.bigboxer23.lights.controllers.hue.HueController;
import com.bigboxer23.utils.http.HttpClientUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.HttpURLConnection;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Get value from hue bridge */
@Tag(name = "Daylight Controller", description = "returns if it is daylight or not")
@RestController
public class DaylightController extends HueController {
	private static final Logger myLogger = LoggerFactory.getLogger(DaylightController.class);

	public static final String kControllerEndpoint = "Daylight";

	@GetMapping(value = "/S/Daylight", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			summary = "returns if it is daylight or not",
			description = "Check with our sensors to see if daylight is active or not")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_UNAUTHORIZED + "", description = "unauthorized"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public String doAction() {
		return "" + isDaylight();
	}

	/**
	 * Get daylight value from hue bridge
	 *
	 * @return
	 */
	public boolean isDaylight() {
		String aDaylightString = HttpClientUtils.execute(new HttpGet(getBaseUrl() + "/sensors/1"));
		myLogger.debug(aDaylightString);
		JsonElement anElement = JsonParser.parseString(aDaylightString);
		return anElement
				.getAsJsonObject()
				.get("state")
				.getAsJsonObject()
				.get("daylight")
				.getAsBoolean();
	}
}
