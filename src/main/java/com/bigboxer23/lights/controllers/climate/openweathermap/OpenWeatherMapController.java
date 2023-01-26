package com.bigboxer23.lights.controllers.climate.openweathermap;

import com.bigboxer23.lights.controllers.AbstractBaseController;
import com.bigboxer23.utils.http.HttpClientUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Fetch weather date from OpenWeatherMap api, forward to OpenHab Items */
@Component
public class OpenWeatherMapController extends AbstractBaseController {
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
		myLogger.info("Fetching OpenWeatherMap data");
		JsonObject aWeatherData = JsonParser.parseString(HttpClientUtils.execute(new HttpGet(
						MessageFormat.format(kOpenWeatherMapUrl, myLatitude, myLongitude, myOpenWeatherMapAPIKey))))
				.getAsJsonObject();
		aWeatherData = aWeatherData.get("main").getAsJsonObject();
		sendDataToOpenHab(
				"OutsideTemperature",
				"" + kelvinToFahrenheit(aWeatherData.get("temp").getAsFloat()));
		sendDataToOpenHab(
				"LowTemperature",
				"" + kelvinToFahrenheit(aWeatherData.get("temp_min").getAsFloat()));
		sendDataToOpenHab(
				"HighTemperature",
				"" + kelvinToFahrenheit(aWeatherData.get("temp_max").getAsFloat()));
		sendDataToOpenHab("OutsideHumidity", aWeatherData.get("humidity").getAsString());
		myLogger.info("OpenWeatherMap data successfully updated");
	}

	private void sendDataToOpenHab(String theItemName, String theItemValue) {
		HttpPost aHttpPost = new HttpPost(kOpenHABUrl + "/rest/items/" + theItemName);
		try {
			aHttpPost.setEntity(
					new ByteArrayEntity(URLDecoder.decode(theItemValue, StandardCharsets.UTF_8.displayName())
							.getBytes(StandardCharsets.UTF_8)));
		} catch (UnsupportedEncodingException theE) {
			myLogger.warn("OpenWeatherMapController:doAction", theE);
		}
		HttpClientUtils.execute(aHttpPost);
	}

	private float kelvinToFahrenheit(float theTempInKelvin) {
		return (theTempInKelvin - 273.15f) * 9f / 5f + 32f;
	}
}
