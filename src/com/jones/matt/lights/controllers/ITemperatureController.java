package com.jones.matt.lights.controllers;

import com.jones.matt.lights.data.WeatherData;

/**
 * Interface for controller that supports returning weather data
 */
public interface ITemperatureController
{
	public WeatherData getWeatherData();
}
