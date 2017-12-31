package com.bigboxer23.house.lights.client;

import com.google.gwt.core.client.EntryPoint;
import com.googlecode.mgwt.ui.client.MGWT;
import com.googlecode.mgwt.ui.client.MGWTSettings;

/**
 * Control X10 house lights via Ajax call to external REST server
 */
public class HouseLights implements EntryPoint
{
	public void onModuleLoad()
	{
		MGWT.applySettings(MGWTSettings.getAppSetting());
		new StatusTimer();
	}

	public static final native int getPollingDelay() /*-{
		return $wnd.PollDelay;
	}-*/;

	public static final native String getBaseUrl() /*-{
		return $wnd.BaseURL;
	}-*/;
}
