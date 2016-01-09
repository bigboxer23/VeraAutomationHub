package com.jones.matt.house.lights.client.ui.climate;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Label;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPanel;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPropertyHelper;
import com.googlecode.mgwt.ui.client.widget.panel.flex.RootFlexPanel;
import com.jones.matt.house.lights.client.event.EventBusInstance;
import com.jones.matt.house.lights.client.model.DeviceVO;
import com.jones.matt.house.lights.client.model.RoomVO;
import com.jones.matt.house.lights.client.ui.Header;
import com.jones.matt.house.lights.client.ui.TimedSlider;
import com.jones.matt.house.lights.client.ui.garage.WeatherLabel;
import com.jones.matt.house.lights.client.utility.DefaultRequestBuilder;
import com.jones.matt.house.lights.client.utility.VeraUrlUtility;

/**
 * Panel includes details for HVAC system and climate information including inside/outside temp, high low, humidity
 */
public class ClimateDetailPanel extends RootFlexPanel implements ValueChangeHandler<RoomVO>
{
	private TimedSlider myTemperatureSlider;

	private FlexPanel myThermostatControlHolder;

	private int myThermostatId;

	private boolean myInited = false;

	public ClimateDetailPanel()
	{
		addStyleName("ClimateDetailPanel");
		add(new Header("Climate Control"));
		add(new TemperaturePanel());
		myThermostatControlHolder = new FlexPanel();
		myThermostatControlHolder.addStyleName("relative");
		myThermostatControlHolder.setOrientation(FlexPropertyHelper.Orientation.HORIZONTAL);
		myThermostatControlHolder.setAlignment(FlexPropertyHelper.Alignment.CENTER);
		myTemperatureSlider = new TimedSlider();
		myTemperatureSlider.setMax(16);
		myTemperatureSlider.addValueChangeHandler(new ValueChangeHandler<Integer>()
		{
			@Override
			public void onValueChange(ValueChangeEvent<Integer> theEvent)
			{
				myThermostatControlHolder.clear();
				myThermostatControlHolder.add(myTemperatureSlider);
				Label aHouseTemperature = WeatherLabel.getTemperature(theEvent.getValue() + 60);
				aHouseTemperature.addStyleName("thermostat");
				myThermostatControlHolder.add(aHouseTemperature);
			}
		});
		myTemperatureSlider.addChangeHandler(new ChangeHandler()
		{
			@Override
			public void onChange(ChangeEvent theEvent)
			{
				if (!myInited)
				{
					myInited = true;
					return;
				}
				new DefaultRequestBuilder(VeraUrlUtility.getThermostatSetUrl(myTemperatureSlider.getValue() + 60, myThermostatId)).send();
			}
		}); add(myThermostatControlHolder);
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
					myTemperatureSlider.setValue(Integer.parseInt(aDevice.getSetpoint()) - 60);
					myThermostatId = aDevice.getID();
				}
			}
		}
	}
}
