package com.jones.matt.house.lights.client.ui.climate;

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
import com.jones.matt.house.lights.client.utility.VeraUrlUtility;

/**
 * Panel to set thermostat mode between off/heat/cool
 */
public class ThermostatModePanel extends FlexPanel implements ValueChangeHandler<RoomVO>, TapHandler
{
	private Button myOffButton;

	private Button myHeatButton;

	private Button myCoolButton;

	private DeviceVO myThermostat;

	public ThermostatModePanel()
	{
		addStyleName("ThermostatModePanel");
		setOrientation(FlexPropertyHelper.Orientation.HORIZONTAL);
		myOffButton = generateButton("Off");
		myHeatButton = generateButton("Heat");
		myCoolButton = generateButton("Cool");
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
					setButtonStatus(aDevice.getMode());
				}
			}
		}
	}

	private Button generateButton(String theText)
	{
		Button aButton = new Button(theText);
		aButton.addStyleName(theText);
		aButton.addTapHandler(this);
		add(aButton);
		return aButton;
	}

	private void setButtonStatus(String theText)
	{
		myOffButton.setImportant(theText.equals("Off"));
		myHeatButton.setImportant(theText.startsWith("Heat"));
		myCoolButton.setImportant(theText.startsWith("Cool"));
	}

	@Override
	public void onTap(TapEvent theEvent)
	{
		setButtonStatus(((Button)theEvent.getSource()).getText());
		new DefaultRequestBuilder(VeraUrlUtility.getThermostatModeUrl(myHeatButton.isImportant() ? "HeatOn" : myCoolButton.isImportant() ? "CoolOn" : "Off", myThermostat.getID())).send();
	}
}
