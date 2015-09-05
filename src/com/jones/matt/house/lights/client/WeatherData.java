package com.jones.matt.house.lights.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay for JSON object
 *
 * {"temperature":47.84,"humidity":48.8}
 */
public class WeatherData extends JavaScriptObject
{
	protected WeatherData()
	{
	}

	public final native double getHumidity() /*-{ return this.humidity; }-*/;

	public final native double getTemperature() /*-{ return this.temperature; }-*/;
}
