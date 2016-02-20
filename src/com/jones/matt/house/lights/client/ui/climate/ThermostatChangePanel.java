package com.jones.matt.house.lights.client.ui.climate;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Label;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPanel;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPropertyHelper;
import com.jones.matt.house.lights.client.event.EventBusInstance;
import com.jones.matt.house.lights.client.model.DeviceVO;
import com.jones.matt.house.lights.client.model.RoomVO;
import com.jones.matt.house.lights.client.ui.TimedSlider;
import com.jones.matt.house.lights.client.ui.garage.WeatherLabel;
import com.jones.matt.house.lights.client.utility.DefaultRequestBuilder;
import com.jones.matt.house.lights.client.utility.VeraUrlUtility;

/**
 * Panel for setting/viewing current thermostat temperature setting.
 */
public class ThermostatChangePanel extends FlexPanel implements ValueChangeHandler<RoomVO>
{
	private TimedSlider myTemperatureSlider;

	private DeviceVO myThermostat;

	public ThermostatChangePanel()
	{
		addStyleName("relative");
		setOrientation(FlexPropertyHelper.Orientation.HORIZONTAL);
		setAlignment(FlexPropertyHelper.Alignment.CENTER);
		EventBusInstance.getInstance().addValueChangeHandler(this);
	}

	@Override
	public void onValueChange(ValueChangeEvent<RoomVO> theEvent)
	{
		if (theEvent.getValue().getName().equals("Climate Control"))
		{
			for (DeviceVO aDevice : theEvent.getValue().getDevices())
			{
				if (aDevice.getName().equals("Thermostat"))
				{
					myThermostat = aDevice;
					if (myTemperatureSlider == null)
					{
						myTemperatureSlider = new TimedSlider();
						myTemperatureSlider.setMax(16);
						myTemperatureSlider.addValueChangeHandler(new ValueChangeHandler<Integer>()
						{
							@Override
							public void onValueChange(ValueChangeEvent<Integer> theEvent)
							{
								clear();
								add(myTemperatureSlider);
								Label aHouseTemperature = WeatherLabel.getTemperature(theEvent.getValue() + 60);
								aHouseTemperature.addStyleName("thermostat");
								add(aHouseTemperature);
							}
						});
						myTemperatureSlider.setValue(Integer.parseInt(aDevice.getSetpoint()) - 60);
						myTemperatureSlider.addChangeHandler(new ChangeHandler()
						{
							@Override
							public void onChange(ChangeEvent theEvent)
							{
								if (myThermostat != null && Integer.parseInt(myThermostat.getSetpoint()) != (myTemperatureSlider.getValue() + 60))
								{
									new DefaultRequestBuilder(VeraUrlUtility.getThermostatSetUrl(myTemperatureSlider.getValue() + 60, myThermostat.getID())).send();
								}
							}
						});
						return;
					}
					myTemperatureSlider.setValue(Integer.parseInt(aDevice.getSetpoint()) - 60);
				}
			}
		}
	}
}
