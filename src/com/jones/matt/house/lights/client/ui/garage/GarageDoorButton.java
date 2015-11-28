package com.jones.matt.house.lights.client.ui.garage;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.googlecode.mgwt.dom.client.event.tap.TapEvent;
import com.googlecode.mgwt.dom.client.event.tap.TapHandler;
import com.googlecode.mgwt.ui.client.widget.button.Button;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPanel;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPropertyHelper;
import com.jones.matt.house.lights.client.event.EventBusInstance;
import com.jones.matt.house.lights.client.model.DeviceVO;
import com.jones.matt.house.lights.client.model.RoomVO;
import com.jones.matt.house.lights.client.utility.DefaultRequestBuilder;

/**
 * Button for garage door, displays Open or Closed based on status.  Queries server
 * for status every 5 seconds to update button text if open or closed.
 */
public class GarageDoorButton extends FlexPanel implements TapHandler, ValueChangeHandler<RoomVO>
{
	private Button myGarageButton;

	private WeatherLabel myWeatherLabel;

	public GarageDoorButton()
	{
		addStyleName("relative");
		setOrientation(FlexPropertyHelper.Orientation.HORIZONTAL);
		myGarageButton = new Button("Open Garage");
		myGarageButton.addStyleName("garage-button");
		myGarageButton.addTapHandler(this);
		add(myGarageButton);
		myWeatherLabel = new WeatherLabel();
		add(myWeatherLabel);
		EventBusInstance.getInstance().addValueChangeHandler(this);
	}

	public void onTap(TapEvent theEvent)
	{
		String aUrl = myGarageButton.getText().equals("Close Garage") ? getClose() : getOpen();
		new DefaultRequestBuilder(aUrl).send();
	}

	@Override
	public void onValueChange(ValueChangeEvent<RoomVO> theEvent)
	{
		if (theEvent.getValue().getName().equals("Garage"))
		{
			for (DeviceVO aDevice : theEvent.getValue().getDevices())
			{
				if (aDevice.getName().equals("Garage Opener"))
				{
					myGarageButton.setText(!aDevice.getDoor() ? "Open Garage" : "Close Garage");
					myWeatherLabel.update(aDevice);
				}
			}
		}
	}

	private static native String getClose() /*-{
		return $wnd.BaseURL + $wnd.CloseUrl;
	}-*/;

	private static native String getOpen() /*-{
		return $wnd.BaseURL + $wnd.OpenUrl;
	}-*/;
}
