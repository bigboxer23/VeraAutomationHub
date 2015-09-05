package com.jones.matt.lights.controllers.scene;

import com.jones.matt.lights.controllers.ISystemController;
import com.jones.matt.lights.controllers.garage.GarageController;
import com.jones.matt.lights.data.WeatherData;
import com.jones.matt.lights.controllers.hue.HueController;
import com.philips.lighting.hue.sdk.utilities.PHUtilities;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller to make lights reactive to temperature outside...
 *
 * We use the GarageController object which is a RPi located in the garage which has a restful
 * service to return a JSON object with temperature and humidity returned
 */
public class WeatherController implements ISystemController
{
	private static final String kLightModel = "LCT001";

	public static final int kMaxTemp = 90;

	public static final int kMinTemp = -10;

	private HueController myHueController;

	private GarageController myGarageController;

	public static final String kControllerEndpoint = "Weather";

	public WeatherController(HueController theHueController, GarageController theGarageController)
	{
		myHueController = theHueController;
		myGarageController = theGarageController;
	}

	/**
	 * Get weather data from garage pi, convert to rgb, convert to philips xy, send to light to display
	 *
	 * @param theCommands
	 * @param theResponse
	 * @return
	 */
	@Override
	public String doAction(List<String> theCommands)
	{
		WeatherData aData = myGarageController.getWeatherData();
		if (aData == null)
		{
			return "Cannot get weather data.";
		}
		List<String> aCommands = new ArrayList<>();
		aCommands.add(theCommands.get(0));
		aCommands.add("xy");
		int[] aColor = getColor(aData.getTemperature(), kMinTemp, kMaxTemp);
		float[] aXY = PHUtilities.calculateXYFromRGB(aColor[0], aColor[1], aColor[2], kLightModel);
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
	private int[] getColor(double theTemp, double theMin, double theMax)
	{
		double[] aColor = {1, 1, 1}; // white
		double aDifference;
		if (theTemp < theMin)
		{
			theTemp = theMin;
		}
		if (theTemp > theMax)
		{
			theTemp = theMax;
		}
		aDifference = theMax - theMin;

		if (theTemp < (theMin + 0.25 * aDifference))
		{
			aColor[0] = 0;
			aColor[1] = 4 * (theTemp - theMin) / aDifference;
		} else if (theTemp < (theMin + 0.5 * aDifference))
		{
			aColor[0] = 0;
			aColor[2] = 1 + 4 * (theMin + 0.25 * aDifference - theTemp) / aDifference;
		} else if (theTemp < (theMin + 0.75 * aDifference))
		{
			aColor[0] = 4 * (theTemp - theMin - 0.5 * aDifference) / aDifference;
			aColor[2] = 0;
		} else
		{
			aColor[1] = 1 + 4 * (theMin + 0.75 * aDifference - theTemp) / aDifference;
			aColor[2] = 0;
		}
		return new int[]{(int)Math.round(aColor[0] * 255), (int)Math.round(aColor[1] * 255), (int)Math.round(aColor[2] * 255)};
	}
}
