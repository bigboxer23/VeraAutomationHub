package com.jones.matt.house.lights.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Overlay object to sit on top of data retrieved from html page
 */
public class LightDataOverlay extends JavaScriptObject
{
	protected LightDataOverlay(){}

	public final native int size() /*-{
		return this.Lights.length / 2;
	}-*/;

	public final String getUrl(int theIndex, String theState)
	{
		return getChannel(theIndex) + "/" + theState;
	}

	public final native String getLabel(int theIndex) /*-{
		return this.Lights[(theIndex * 2) + 1];
	}-*/;

	private native String getChannel(int theIndex) /*-{
		return this.Lights[theIndex * 2];
	}-*/;
}
