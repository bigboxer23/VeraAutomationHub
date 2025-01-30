package com.bigboxer23.lights.controllers.climate.openweathermap;

import com.bigboxer23.utils.http.OkHttpCallback;
import com.bigboxer23.utils.http.OkHttpUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Fetch weather date from OpenWeatherMap api, forward to OpenHab Items */
@Slf4j
@Component
public class OpenWeatherMapController {
	private static final String kOpenWeatherMapUrl =
			"https://api.openweathermap.org/data/2.5/weather?lat={0}&lon={1}&APPID={2}";

	@Value("${openweathermap.api}")
	private String myOpenWeatherMapAPIKey;

	@Value("${lat}")
	private String myLatitude;

	@Value("${long}")
	private String myLongitude;

	@Value("${openHABUrl}")
	private String kOpenHABUrl;

	@Scheduled(fixedDelay = 900000) // 15min
	private void fetchClimateData() {
		log.debug("Fetching OpenWeatherMap data");
		OkHttpUtil.get(
				MessageFormat.format(kOpenWeatherMapUrl, myLatitude, myLongitude, myOpenWeatherMapAPIKey),
				new OkHttpCallback() {
					@Override
					public void onResponseBody(Call call, ResponseBody responseBody) {
						try {
							JsonObject weatherData = JsonParser.parseString(responseBody.string())
									.getAsJsonObject();
							weatherData = weatherData.get("main").getAsJsonObject();
							sendDataToOpenHab(
									"OutsideTemperature",
									kelvinToFahrenheit(weatherData.get("temp").getAsFloat()));
							sendDataToOpenHab(
									"LowTemperature",
									kelvinToFahrenheit(
											weatherData.get("temp_min").getAsFloat()));
							sendDataToOpenHab(
									"HighTemperature",
									kelvinToFahrenheit(
											weatherData.get("temp_max").getAsFloat()));
							sendDataToOpenHab(
									"OutsideHumidity",
									weatherData.get("humidity").getAsFloat());
							log.debug("OpenWeatherMap data successfully updated");
						} catch (IOException e) {
							log.warn("fetchClimateData:", e);
						}
					}
				});
	}

	private void sendDataToOpenHab(String theItemName, float theItemValue) {
		OkHttpUtil.post(
				kOpenHABUrl + "/rest/items/" + theItemName,
				new OkHttpCallback(),
				theBuilder ->
						theBuilder.post(RequestBody.create(("" + theItemValue).getBytes(StandardCharsets.UTF_8))));
	}

	private float kelvinToFahrenheit(float theTempInKelvin) {
		return (theTempInKelvin - 273.15f) * 9f / 5f + 32f;
	}
}
