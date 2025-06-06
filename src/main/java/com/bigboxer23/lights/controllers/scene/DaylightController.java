package com.bigboxer23.lights.controllers.scene;

import com.bigboxer23.lights.controllers.hue.HueController;
import com.bigboxer23.utils.http.OkHttpUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.net.HttpURLConnection;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Get value from hue bridge */
@Slf4j
@Tag(name = "Daylight Controller", description = "returns if it is daylight or not")
@RestController
public class DaylightController extends HueController {
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
		try (Response response = OkHttpUtil.getSynchronous(getBaseUrl() + "/sensors/1", null)) {
			String body = response.body().string();
			if (!response.isSuccessful()) {
				throw new IOException("call to " + getBaseUrl() + "/sensors/1 failed. " + body);
			}
			log.debug(body);
			JsonElement anElement = JsonParser.parseString(body);
			return anElement
					.getAsJsonObject()
					.get("state")
					.getAsJsonObject()
					.get("daylight")
					.getAsBoolean();
		} catch (IOException e) {
			log.warn("isDaylight: ", e);
		}
		return false;
	}
}
