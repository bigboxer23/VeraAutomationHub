package com.jones.matt.lights.data;

import com.google.gson.annotations.SerializedName;

/**
 * Data object for the data fetched via REST and JSON object from garage sensors
 */
public class WeatherData
{
	@SerializedName("temperature")
	private double myTemperature;

	@SerializedName("humidity")
	private double myHumidity;

	public double getHumidity()
	{
		return myHumidity;
	}

	public void setHumidity(double theHumidity)
	{
		myHumidity = theHumidity;
	}

	public double getTemperature()
	{
		return myTemperature;
	}

	public void setTemperature(double theTemperature)
	{
		myTemperature = theTemperature;
	}
}
