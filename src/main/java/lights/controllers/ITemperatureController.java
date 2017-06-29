package lights.controllers;

import lights.data.WeatherData;

/**
 * Interface for controller that supports returning weather data
 */
public interface ITemperatureController
{
	public WeatherData getWeatherData();
}
