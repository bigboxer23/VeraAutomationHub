package com.bigboxer23.lights.controllers.climate;

import com.google.gson.annotations.SerializedName;

/**
 *
 */
public class ClimateData
{
	@SerializedName("temperature")
	private float myTemperature;

	@SerializedName("pressure")
	private float myPressure;

	@SerializedName("humidity")
	private float myHumidity;

	@SerializedName("quality")
	private float myQuality;

	public float getQuality()
	{
		return myQuality;
	}

	public float getHumidity()
	{
		return myHumidity;
	}

	public float getPressure()
	{
		return myPressure;
	}

	public float getTemperature()
	{
		return (myTemperature * 9/5) + 32;
	}
}
