package com.jones.matt.house.lights.client.ui.climate;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Label;
import com.googlecode.mgwt.ui.client.widget.button.ButtonAppearance;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPanel;
import com.jones.matt.house.lights.client.event.EventBusInstance;
import com.jones.matt.house.lights.client.model.DeviceVO;
import com.jones.matt.house.lights.client.model.RoomVO;
import com.jones.matt.house.lights.client.ui.garage.WeatherLabel;

/**
 * Displays colored temperature outside and inside
 */
public class TemperaturePanel extends FlexPanel implements ValueChangeHandler<RoomVO>
{
	public TemperaturePanel()
	{
		addStyleName("current " + GWT.<ButtonAppearance>create(ButtonAppearance.class).css().button());
		EventBusInstance.getInstance().addValueChangeHandler(this);
	}

	@Override
	public void onValueChange(ValueChangeEvent<RoomVO> theEvent)
	{
		if (theEvent.getValue().getName().equals("Climate Control"))
		{
			clear();
			for (DeviceVO aDevice : theEvent.getValue().getDevices())
			{
				if (aDevice.getName().equals("Temperature"))
				{
					add(WeatherLabel.getTemperature(Double.parseDouble(aDevice.getTemperature())));
				}
				if (aDevice.getName().equals("Thermostat"))
				{
					Label aHouseTemperature = WeatherLabel.getTemperature(Double.parseDouble(aDevice.getTemperature()));
					aHouseTemperature.addStyleName("thermostat");
					add(aHouseTemperature);
				}
			}
		}
	}
}
