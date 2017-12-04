package com.bigboxer23.house.lights.client.ui.climate;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.googlecode.mgwt.dom.client.event.tap.TapEvent;
import com.googlecode.mgwt.dom.client.event.tap.TapHandler;
import com.googlecode.mgwt.ui.client.widget.button.Button;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPanel;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPropertyHelper;
import com.bigboxer23.house.lights.client.event.EventBusInstance;
import com.bigboxer23.house.lights.client.model.DeviceVO;
import com.bigboxer23.house.lights.client.model.RoomVO;
import com.bigboxer23.house.lights.client.utility.DefaultRequestBuilder;
import com.bigboxer23.house.lights.client.utility.VeraUrlUtility;

/**
 *
 */
public class FanChangePanel extends FlexPanel implements ValueChangeHandler<RoomVO>, TapHandler
{
	private Button myOffButton;

	private Button myAutoButton;

	private DeviceVO myThermostat;

	public FanChangePanel()
	{
		addStyleName("ThermostatModePanel");
		setOrientation(FlexPropertyHelper.Orientation.HORIZONTAL);
		myOffButton = generateButton("On");
		myAutoButton = generateButton("Auto");
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
					setButtonStatus(aDevice.getFanMode());
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
		myOffButton.setImportant(theText.contains("On"));
		myAutoButton.setImportant(theText.startsWith("Auto"));
	}

	@Override
	public void onTap(TapEvent theEvent)
	{
		setButtonStatus(((Button) theEvent.getSource()).getText());
		new DefaultRequestBuilder(VeraUrlUtility.getFanModeUrl(myAutoButton.isImportant() ? "Auto" : "ContinuousOn", myThermostat.getID())).send();
	}
}
