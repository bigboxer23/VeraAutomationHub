package com.bigboxer23.lights.controllers;

import com.bigboxer23.lights.data.WeatherData;

/**
 * Interface for controller that supports returning weather data
 */
public interface ITemperatureController
{
	public WeatherData getWeatherData();
}
