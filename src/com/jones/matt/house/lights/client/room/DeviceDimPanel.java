package com.jones.matt.house.lights.client.room;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPropertyHelper;
import com.googlecode.mgwt.ui.client.widget.panel.flex.RootFlexPanel;
import com.jones.matt.house.lights.client.DefaultRequestBuilder;
import com.jones.matt.house.lights.client.HouseLights;
import com.jones.matt.house.lights.client.model.DeviceVO;

/**
 *
 */
public class DeviceDimPanel extends RootFlexPanel
{
	public DeviceDimPanel(final DeviceVO theData)
	{
		setAlignment(FlexPropertyHelper.Alignment.CENTER);
		add(new Header(theData.getName()));
		final RoomSlider aRoomSlider = new RoomSlider();
		aRoomSlider.setValue(Integer.parseInt(theData.getLevel()));
		aRoomSlider.addChangeHandler(new ChangeHandler()
		{
			@Override
			public void onChange(ChangeEvent theEvent)
			{
				new DefaultRequestBuilder(HouseLights.getBaseUrl() + "S/Vera/" + aRoomSlider.getValue() + "/Device/" + theData.getID()).send();
			}
		});
		add(aRoomSlider);
	}
}
