package com.jones.matt.house.lights.client.ui.garage;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Label;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPanel;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPropertyHelper;
import com.jones.matt.house.lights.client.utility.DefaultRequestBuilder;
import com.jones.matt.house.lights.client.HouseLights;
import com.jones.matt.house.lights.client.model.WeatherData;

/**
 * Panel to display temperature.  Changes color based on the temp retrieved from REST URL
 */
public class WeatherLabel extends FlexPanel
{
	private Timer myWeatherTimer;

	public WeatherLabel()
	{
		addStyleName("weather");
		setOrientation(FlexPropertyHelper.Orientation.HORIZONTAL);
		myWeatherTimer = new WeatherTimer();
		myWeatherTimer.run();
	}

	/**
	 * Get our label formatted for display
	 * @param theTemperature
	 * @return
	 */
	private Label getTemperature(double theTemperature)
	{
		Label aLabel = new Label(NumberFormat.getFormat(".##").format(theTemperature) + "°F");
		aLabel.getElement().getStyle().setBackgroundColor(getTemperatureColor(theTemperature));
		if (theTemperature < -10)
		{
			aLabel.getElement().getStyle().setColor("#C5DCFF");
		}
		return aLabel;
	}

	private Label getHumidity(double theHumidity)
	{
		return new Label("Humidity: " + NumberFormat.getFormat(".##").format(theHumidity) + "%");
	}

	/**
	 * Get "heat map" values to use as background color
	 *
	 * @param theTemperature
	 * @return
	 */
	private String getTemperatureColor(double theTemperature)
	{
		if (theTemperature < -10)
		{
			return "#feffff";
		} else if(theTemperature < 0)
		{
			return "#d1c9df";
		} else if(theTemperature < 10)
		{
			return "#a496c0";
		} else if(theTemperature < 20)
		{
			return "#3993CE";
		} else if(theTemperature < 30)
		{
			return "#0772B8";
		} else if(theTemperature < 40)
		{
			return "#03902B";
		} else if(theTemperature < 50)
		{
			return "#2DC558";
		} else if(theTemperature < 60)
		{
			return "#FECF3B";
		} else if(theTemperature < 70)
		{
			return "#EC9800";
		} else if(theTemperature < 80)
		{
			return "#DD531E";
		} else if(theTemperature < 90)
		{
			return "#C53600";
		} else if(theTemperature < 100)
		{
			return "#B10909";
		}
		return "#6F0015";
	}

	/**
	 * Callback sets label text, schedules timer to check for new weather
	 */
	private class WeatherCallback implements RequestCallback
	{
		public void onResponseReceived(Request theRequest, Response theResponse)
		{
			WeatherData aData = JsonUtils.safeEval(theResponse.getText());
			if (aData != null)
			{
				clear();
				add(getTemperature(aData.getTemperature()));
			}
			myWeatherTimer.schedule(HouseLights.getPollingDelay());
		}

		public void onError(Request theRequest, Throwable theException)
		{
			myWeatherTimer.schedule(HouseLights.getPollingDelay());
		}
	}

	/**
	 * Timer makes RPC to check weather, calls our callback
	 */
	private class WeatherTimer extends Timer
	{
		@Override
		public void run()
		{
			new DefaultRequestBuilder(getWeather())
			{
				protected RequestCallback createCallback()
				{
					return new WeatherCallback();
				}
			}.send();
		}
	}

	/**
	 * REST url to retrieve the weather JSON from
	 *
	 * @return
	 */
	private static native String getWeather() /*-{
		return $wnd.BaseURL + $wnd.WeatherUrl;
	}-*/;
}
