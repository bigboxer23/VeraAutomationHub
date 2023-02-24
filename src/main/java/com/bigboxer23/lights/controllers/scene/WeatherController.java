package com.bigboxer23.lights.controllers.scene;

import com.bigboxer23.lights.controllers.garage.GarageController;
import com.bigboxer23.lights.controllers.hue.HueController;
import com.bigboxer23.lights.data.WeatherData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to make lights reactive to temperature outside...
 *
 * <p>We use the GarageController object which is a RPi located in the garage which has a restful
 * service to return a JSON object with temperature and humidity returned
 */
@Tag(
		name = "Weather Controller",
		description = "Controller to make lights reactive to temperature outside. The GarageController"
				+ " object is used, which is a RPi located in the garage which has a restful"
				+ " service to return a JSON object with temperature and humidity returned.")
@RestController
public class WeatherController {
	private static final String kLightModel = "LCT001";

	public static final int kMaxTemp = 90;

	public static final int kMinTemp = -10;

	private HueController myHueController;

	private GarageController myGarageController;

	public WeatherController(GarageController garageController, HueController hueController) {
		myHueController = hueController;
		myGarageController = garageController;
	}

	/**
	 * Get weather data from garage pi, convert to rgb, convert to philips xy, send to light to
	 * display
	 *
	 * @param theCommands
	 * @param theResponse
	 * @return
	 */
	@GetMapping(value = "/S/Weather/{deviceId}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			summary = "Fetches weather data from remote source, updates lights defined by the command"
					+ " with weather appropriate color",
			description = "Fetches weather data from remote source, updates lights defined by the command"
					+ " with weather appropriate color. The defined device should be a philips"
					+ " hue light")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_UNAUTHORIZED + "", description = "unauthorized"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public String doAction(
			@Parameter(description = "device id for which to apply the weather color") @PathVariable(value = "deviceId")
					String deviceId) {
		WeatherData aData = myGarageController.getWeatherData();
		if (aData == null) {
			return "Cannot get weather data.";
		}
		List<String> aCommands = new ArrayList<>();
		aCommands.add(deviceId);
		aCommands.add("xy");
		int[] aColor = getColor(aData.getTemperature(), kMinTemp, kMaxTemp);
		float[] aXY =
				new float[] {0f, 0f}; // TODO: this is totally non-functional PHUtilities.calculateXYFromRGB(aColor[0],
		// aColor[1], aColor[2], kLightModel);
		aCommands.add("" + aXY[0]);
		aCommands.add("" + aXY[1]);
		return myHueController.doAction(aCommands);
	}

	/**
	 * Using method adapted from:
	 * http://stackoverflow.com/questions/4615029/rgb-range-for-cold-and-warm-colors
	 *
	 * @param theTemp the temp
	 * @param theMin min possible temp
	 * @param theMax max possible temp
	 * @return rgb triplet for color
	 */
	private int[] getColor(double theTemp, double theMin, double theMax) {
		double[] aColor = {1, 1, 1}; // white
		double aDifference;
		if (theTemp < theMin) {
			theTemp = theMin;
		}
		if (theTemp > theMax) {
			theTemp = theMax;
		}
		aDifference = theMax - theMin;

		if (theTemp < (theMin + 0.25 * aDifference)) {
			aColor[0] = 0;
			aColor[1] = 4 * (theTemp - theMin) / aDifference;
		} else if (theTemp < (theMin + 0.5 * aDifference)) {
			aColor[0] = 0;
			aColor[2] = 1 + 4 * (theMin + 0.25 * aDifference - theTemp) / aDifference;
		} else if (theTemp < (theMin + 0.75 * aDifference)) {
			aColor[0] = 4 * (theTemp - theMin - 0.5 * aDifference) / aDifference;
			aColor[2] = 0;
		} else {
			aColor[1] = 1 + 4 * (theMin + 0.75 * aDifference - theTemp) / aDifference;
			aColor[2] = 0;
		}
		return new int[] {
			(int) Math.round(aColor[0] * 255), (int) Math.round(aColor[1] * 255), (int) Math.round(aColor[2] * 255)
		};
	}
}
