package com.bigboxer23.lights.controllers.hue;

import com.bigboxer23.lights.controllers.hue.data.HueAPIResponse;
import com.bigboxer23.utils.http.OkHttpUtil;
import com.squareup.moshi.Moshi;
import java.io.IOException;
import java.text.MessageFormat;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** */
@Component
public class HueV2Controller {
	private static final String BASE_URL = "https://{0}/clip/v2/{1}";

	private static final String AUTH_HEADER = "hue-application-key";

	private final Moshi moshi = new Moshi.Builder().build();

	@Value("${hueAPIKey}")
	private String API_KEY;

	/** Hue bridge address */
	@Value("${hueBridgeV2}")
	private String HUE_BRIDGE;

	public void activateScene(String sceneId) throws IOException {
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

	public HueAPIResponse getZones() throws IOException
	{
		try (Response response = OkHttpUtil.getSynchronous(
				MessageFormat.format(BASE_URL, HUE_BRIDGE, "resource/zone"),
				builder -> {
					builder.addHeader(AUTH_HEADER, API_KEY);
					return builder;
				},
				HueCompatibleClient.getClient())) {
			ResponseBody body = response.body();
			return body == null ? null : moshi.adapter(HueAPIResponse.class).fromJson(body.string());
		}
	}

	public HueAPIResponse getScenes() throws IOException {
		try (Response response = OkHttpUtil.getSynchronous(
				MessageFormat.format(BASE_URL, HUE_BRIDGE, "resource/scene"),
				builder -> {
					builder.addHeader(AUTH_HEADER, API_KEY);
					return builder;
				},
				HueCompatibleClient.getClient())) {
			ResponseBody body = response.body();
			return body == null ? null : moshi.adapter(HueAPIResponse.class).fromJson(body.string());
		}
	}
}
