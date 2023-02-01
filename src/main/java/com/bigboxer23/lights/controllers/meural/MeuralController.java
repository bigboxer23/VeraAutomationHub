package com.bigboxer23.lights.controllers.meural;

import com.bigboxer23.lights.controllers.vera.VeraDeviceVO;
import com.bigboxer23.lights.controllers.vera.VeraHouseVO;
import com.squareup.moshi.Moshi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/** */
@Tag(name = "Meural Service", description = "Expose APIs needed to change source, art, etc on our Meural device.")
@RestController
public class MeuralController {
	private static final Logger logger = LoggerFactory.getLogger(MeuralController.class);

	@Value("${meuralServer}")
	private String meuralServer;

	private VeraDeviceVO meuralStatus;

	private final Moshi moshi = new Moshi.Builder().build();

	private final OkHttpClient client = new OkHttpClient.Builder()
			.connectTimeout(1, TimeUnit.MINUTES)
			.writeTimeout(1, TimeUnit.MINUTES)
			.readTimeout(1, TimeUnit.MINUTES)
			.callTimeout(1, TimeUnit.MINUTES)
			.build();

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
			logger.warn("updateOpenAIPrompt", e);
		}
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
				example = "0=Google Photos, 1=OpenAI Dall-e",
				schema =
						@Schema(
								type = "string",
								defaultValue = "0",
								allowableValues = {"0", "1"}))
	})
	public void changeSource(int source) {
		callMeural("/changeSource?source=" + source);
 		Optional.ofNullable(meuralStatus).ifPresent(meuralStatus -> meuralStatus.setStatus("" + source));
	}

	@GetMapping(value = "/S/meural/getSource")
	public int getSource() throws IOException {
		Response response = client.newCall(new Request.Builder()
						.url(meuralServer + "/getCurrentSource")
						.get()
						.build())
				.execute();
		return moshi.adapter(MeuralIntegerResponse.class)
				.fromJson(response.body().string())
				.getResponse();
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
		Response response = client.newCall(new Request.Builder()
						.url(meuralServer + "/isAsleep")
						.get()
						.build())
				.execute();
		return moshi.adapter(MeuralResponse.class)
				.fromJson(response.body().string())
				.getResponse();
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
		logger.info("meural requested: " + url);
		client.newCall(new Request.Builder()
						.url(meuralServer + url)
						.post(RequestBody.create(new byte[0]))
						.build())
				.enqueue(new MeuralCallback());
	}

	@Scheduled(fixedDelay = 5000)
	private void fetchMeuralStatus() {
		try {
			logger.debug("Fetching meural data");
			boolean isAwake = isAwake();
			int source = getSource();
			meuralStatus = new VeraDeviceVO("Meural", isAwake ? 0 : 1);
			meuralStatus.setStatus("" + source);
			logger.debug("Fetched meural data");
		} catch (Exception e) {
			logger.error("fetchMeuralStatus", e);
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
