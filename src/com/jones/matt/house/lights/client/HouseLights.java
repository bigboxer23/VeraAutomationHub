package com.jones.matt.house.lights.client;

import com.google.gwt.core.client.EntryPoint;
import com.googlecode.mgwt.ui.client.MGWT;
import com.googlecode.mgwt.ui.client.MGWTSettings;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPanel;
import com.jones.matt.house.lights.client.animation.AnimationStack;
import com.jones.matt.house.lights.client.garage.GarageDoorButton;
import com.jones.matt.house.lights.client.garage.WeatherLabel;
import com.jones.matt.house.lights.client.room.Header;

/**
 * Control X10 house lights via Ajax call to external REST server
 */
public class HouseLights implements EntryPoint
{
	public void onModuleLoad()
	{
		MGWT.applySettings(MGWTSettings.getAppSetting());
		FlexPanel aContent = new FlexPanel();
		FlexPanel aHolder = new FlexPanel();
		aHolder.add(new Header("Lights", false));
		aHolder.add(aContent);
		aHolder.add(new GarageDoorButton());
		aHolder.add(new WeatherLabel());
		AnimationStack.getInstance().forward(aHolder);
		new StatusTimer(aContent);
	}

	/**
	 * Get the data from our html page (set there for easier changing w/o recompile
	 *
	 * @return
	 */
	//private static native LightDataOverlay getLightData() /*-{
	//	return $wnd.LightData;
	//}-*/;

	public static final native int getPollingDelay() /*-{
		return $wnd.PollDelay;
	}-*/;

	public static final native String getBaseUrl() /*-{
		return $wnd.BaseURL;
	}-*/;
}
