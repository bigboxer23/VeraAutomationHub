package com.bigboxer23.lights.data;

import lombok.Data;

/**
 * Data object for the data fetched via REST and JSON object from garage sensors
 */
@Data
public class WeatherData
{
	private double temperature;

	private double humidity;
}
