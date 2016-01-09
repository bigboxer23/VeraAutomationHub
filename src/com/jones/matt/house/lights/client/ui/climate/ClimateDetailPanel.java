package com.jones.matt.house.lights.client.ui.climate;

import com.googlecode.mgwt.ui.client.widget.panel.flex.RootFlexPanel;
import com.jones.matt.house.lights.client.ui.Header;

/**
 * Panel includes details for HVAC system and climate information including inside/outside temp, high low, humidity
 */
public class ClimateDetailPanel extends RootFlexPanel
{
	public ClimateDetailPanel()
	{
		addStyleName("ClimateDetailPanel");
		add(new Header("Climate Control"));
		add(new TemperaturePanel());
		add(new ThermostatModePanel());
		add(new ThermostatChangePanel());
	}
}
