package com.bigboxer23.lights.controllers.meural;

import com.bigboxer23.lights.controllers.vera.VeraDeviceVO;
import com.bigboxer23.lights.controllers.vera.VeraHouseVO;
import com.bigboxer23.utils.http.OkHttpCallback;
import com.bigboxer23.utils.http.OkHttpUtil;
import com.bigboxer23.utils.logging.LoggingContextBuilder;
import com.bigboxer23.utils.logging.WrappingCloseable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/** */
@Slf4j
@Tag(name = "Meural Service", description = "Expose APIs needed to change source, art, etc on our Meural device.")
@RestController
public class MeuralController {
	@Value("${meuralServer}")
	private String meuralServer;

	private VeraDeviceVO meuralStatus;

	@PostMapping(value = "/S/meural/nextPicture", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			summary = "Go to next piece of artwork",
			description = "Whatever source is defined in the scheduler, go to the next item from the" + " source")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_UNAUTHORIZED + "", description = "missing valid token"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public void nextPicture() {
		callMeural("/nextPicture");
	}

	@PostMapping(value = "/S/meural/prevPicture", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			summary = "Go to previous piece of artwork",
			description = "Whatever source is defined in the scheduler, go to the previous item from the" + " source")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_UNAUTHORIZED + "", description = "missing valid token"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public void prevPicture() {
		callMeural("/prevPicture");
	}

	@PostMapping(value = "/S/meural/updateOpenAIPrompt", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			summary = "Updates the prompt used to generate the images from OpenAI component",
			description = "This prompt is sent to OpenAI's generator and an AI creates an image based on" + " this")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_UNAUTHORIZED + "", description = "missing valid token"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	@Parameters({
		@Parameter(
				name = "prompt",
				description = "Text prompt for OpenAI to generate an image from",
				required = true,
				example = "a beaver that's using a chainsaw on top of his dam")
	})
	public void updateOpenAIPrompt(String prompt) {
		try {
			callMeural("/updateOpenAIPrompt?prompt=" + URLEncoder.encode(prompt, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			log.warn("updateOpenAIPrompt", e);
		}
	}

	@GetMapping(value = "/S/meural/getOpenAIInformation", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			summary = "Gets the information used to generate the images from OpenAI component",
			description = "This information was last sent to OpenAI's Dall-e for image creation.")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_BAD_REQUEST + "", description = "Bad request"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public String getOpenAIPrompt() throws IOException {
		try (WrappingCloseable c = LoggingContextBuilder.create().addTraceId().build();
				Response response = OkHttpUtil.getSynchronous(meuralServer + "/getOpenAIInformation", null)) {
			return response.body().string();
		}
	}

	@PostMapping(value = "/S/meural/updateOpenAIQuality", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			summary = "Updates the quality used to generate the images from OpenAI component",
			description = "This quality is sent to OpenAI's generator and an AI creates an image based on" + " this")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_UNAUTHORIZED + "", description = "missing valid token"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	@Parameters({
		@Parameter(
				name = "quality",
				description = "quality for OpenAI to generate an image with",
				required = true,
				example = "standard | hd")
	})
	public void updateOpenAIQuality(String quality) {
		callMeural("/changeOpenAIQuality?quality=" + URLEncoder.encode(quality, StandardCharsets.UTF_8));
	}

	@PostMapping(value = "/S/meural/updateOpenAIStyle", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			summary = "Updates the style used to generate the images from OpenAI component",
			description = "This style is sent to OpenAI's generator and an AI creates an image based on" + " this")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_UNAUTHORIZED + "", description = "missing valid token"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	@Parameters({
		@Parameter(
				name = "style",
				description = "style for OpenAI to generate an image with",
				required = true,
				example = "natural | vivid")
	})
	public void updateOpenAIStyle(String style) {
		callMeural("/changeOpenAIStyle?style=" + URLEncoder.encode(style, StandardCharsets.UTF_8));
	}

	@PostMapping(value = "/S/meural/showInfo", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			summary = "Display info about what's currently displayed",
			description = "Display info about what's currently displayed on the Meural's display")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_BAD_REQUEST + "", description = "Bad request"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public void showInfo() {
		callMeural("/showInfo");
	}

	@PostMapping(value = "/S/meural/hideInfo", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			summary = "If displaying info no the Meural display, hide it",
			description = "If displaying info no the Meural display, hide it")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_BAD_REQUEST + "", description = "Bad request"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public void hideInfo() {
		callMeural("/hideInfo");
	}

	@PostMapping(value = "/S/meural/changeSource")
	@Operation(
			summary = "Changes the source where new images are fetched from",
			description = "Currently supported sources are from google photos, and from OpenAI Dall-e")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_UNAUTHORIZED + "", description = "missing valid token"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	@Parameters({
		@Parameter(
				name = "source",
				description = "ordinal to change backing sources.",
				required = true,
				example = "0=Google Photos Album, 1=OpenAI TextCompletion Dall-e, 2=OpenAI ChatGPT-3"
						+ " Dall-e, 3=OpenAI ChatGPT-4 Dall-e, 4=James Webb Space Telescope",
				schema =
						@Schema(
								type = "string",
								defaultValue = "0",
								allowableValues = {"0", "1", "2", "3", "4"}))
	})
	public void changeSource(int source) {
		callMeural("/changeSource?source=" + source);
		Optional.ofNullable(meuralStatus).ifPresent(meuralStatus -> meuralStatus.setStatus("" + source));
	}

	@GetMapping(value = "/S/meural/getSource")
	@Operation(
			summary = "Get the current source for content",
			description = "Content source, 0 is google photos, 1 is openAI TextCompletion Dall-e, 2 is"
					+ " OpenAI ChatGPT-3 Dall-e, 3 is OpenAI ChatGPT-4 Dall-e, 4 is James Webb"
					+ " Space Telescope content.")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_UNAUTHORIZED + "", description = "missing valid token"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public int getSource() throws IOException {
		try (WrappingCloseable c = LoggingContextBuilder.create().addTraceId().build();
				Response response = OkHttpUtil.getSynchronous(meuralServer + "/getCurrentSource", null)) {
			return OkHttpUtil.getBody(response, MeuralIntegerResponse.class)
					.map(MeuralIntegerResponse::getResponse)
					.orElse(-1);
		}
	}

	@GetMapping(value = "/S/meural/isAwake")
	@Operation(
			summary = "Check if the Meural is asleep.",
			description = "If the Meural is asleep return true, otherwise false.  \"Response\" field"
					+ " within return value denotes state")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_UNAUTHORIZED + "", description = "missing valid token"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public boolean isAwake() throws IOException {
		try (WrappingCloseable c = LoggingContextBuilder.create().addTraceId().build();
				Response response = OkHttpUtil.getSynchronous(meuralServer + "/isAsleep", null)) {
			return OkHttpUtil.getBody(response, MeuralResponse.class)
					.map(MeuralResponse::getResponse)
					.orElse(false);
		}
	}

	@PostMapping(value = "/S/meural/sleep", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			summary = "Put the meural to sleep if it's awake",
			description = "If the Meural is awake, will put it to sleep.")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_BAD_REQUEST + "", description = "Bad request"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public void sleep() {
		callMeural("/sleep");
		Optional.ofNullable(meuralStatus).ifPresent(meuralStatus -> meuralStatus.setLevel("0"));
	}

	@PostMapping(value = "/S/meural/wakeup", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			summary = "Wake the Meural if it's asleep",
			description = "If the Meural is sleeping, this will turn it on.")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_BAD_REQUEST + "", description = "Bad request"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public void wakeup() {
		callMeural("/wakeup");
		Optional.ofNullable(meuralStatus).ifPresent(meuralStatus -> meuralStatus.setLevel("1"));
	}

	private void callMeural(String url) {
		try (WrappingCloseable c = LoggingContextBuilder.create().addTraceId().build()) {
			log.info("meural requested: " + url);
			OkHttpUtil.post(meuralServer + url, new OkHttpCallback());
		}
	}

	@Scheduled(fixedDelay = 5000)
	private void fetchMeuralStatus() {
		try (WrappingCloseable c = LoggingContextBuilder.create().addTraceId().build()) {
			log.debug("Fetching meural data");
			boolean isAwake = isAwake();
			int source = getSource();
			meuralStatus = new VeraDeviceVO("Meural", isAwake ? 0 : 1);
			meuralStatus.setStatus("" + source);
			meuralStatus.setTemperature(getOpenAIPrompt());
			log.debug("Fetched meural data");
		} catch (Exception e) {
			log.error("fetchMeuralStatus", e);
		}
	}

	public void getStatus(VeraHouseVO houseStatus) {
		if (meuralStatus != null && houseStatus != null && houseStatus.getRooms() != null) {
			houseStatus.getRooms().stream()
					.filter(room -> "Meural".equalsIgnoreCase(room.getName()))
					.forEach(room -> room.addDevice(meuralStatus));
		}
	}
}
