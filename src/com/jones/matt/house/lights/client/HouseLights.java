package com.jones.matt.house.lights.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.googlecode.mgwt.dom.client.event.tap.TapEvent;
import com.googlecode.mgwt.dom.client.event.tap.TapHandler;
import com.googlecode.mgwt.dom.client.recognizer.longtap.LongTapEvent;
import com.googlecode.mgwt.dom.client.recognizer.longtap.LongTapHandler;
import com.googlecode.mgwt.ui.client.MGWT;
import com.googlecode.mgwt.ui.client.MGWTSettings;
import com.googlecode.mgwt.ui.client.widget.animation.AnimationWidget;
import com.googlecode.mgwt.ui.client.widget.animation.Animations;
import com.googlecode.mgwt.ui.client.widget.button.Button;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPanel;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPropertyHelper;
import com.googlecode.mgwt.ui.client.widget.panel.flex.RootFlexPanel;
import com.jones.matt.house.lights.client.animation.AnimationStack;
import com.jones.matt.house.lights.client.room.Header;

import java.util.List;

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
