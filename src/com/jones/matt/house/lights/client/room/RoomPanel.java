package com.jones.matt.house.lights.client.room;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPropertyHelper;
import com.googlecode.mgwt.ui.client.widget.panel.flex.RootFlexPanel;
import com.jones.matt.house.lights.client.DefaultRequestBuilder;
import com.jones.matt.house.lights.client.event.EventBusInstance;
import com.jones.matt.house.lights.client.HouseLights;
import com.jones.matt.house.lights.client.model.DeviceVO;
import com.jones.matt.house.lights.client.model.RoomVO;

/**
 *
 */
public class RoomPanel extends RootFlexPanel implements ValueChangeHandler<RoomVO>
{
	private RoomVO myData;

	public RoomPanel(RoomVO theData)
	{
		myData = theData;
		EventBusInstance.getInstance().addValueChangeHandler(this);
		setAlignment(FlexPropertyHelper.Alignment.CENTER);
		add(new Header(theData.getName()));
		final RoomSlider aRoomSlider = new RoomSlider();
		aRoomSlider.addChangeHandler(new ChangeHandler()
		{
			@Override
			public void onChange(ChangeEvent theEvent)
			{
				new DefaultRequestBuilder(HouseLights.getBaseUrl() + "S/Vera/" + aRoomSlider.getValue() + "/Room/" + myData.getID()).send();
			}
		});
		add(aRoomSlider);
		for(DeviceVO aDevice : myData.getDevices())
		{
			add(new DevicePanel(myData, aDevice));
		}
	}

	@Override
	public void onValueChange(ValueChangeEvent<RoomVO> theEvent)
	{
		if (theEvent.getValue().getID() == myData.getID())
		{
			myData = theEvent.getValue();
		}
	}
}
