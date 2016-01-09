package com.jones.matt.house.lights.client.ui.room;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPropertyHelper;
import com.googlecode.mgwt.ui.client.widget.panel.flex.RootFlexPanel;
import com.jones.matt.house.lights.client.utility.DefaultRequestBuilder;
import com.jones.matt.house.lights.client.event.EventBusInstance;
import com.jones.matt.house.lights.client.model.DeviceVO;
import com.jones.matt.house.lights.client.model.RoomVO;
import com.jones.matt.house.lights.client.ui.Header;
import com.jones.matt.house.lights.client.ui.TimedSlider;
import com.jones.matt.house.lights.client.utility.VeraUrlUtility;

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
		final TimedSlider aTimedSlider = new TimedSlider();
		aTimedSlider.addChangeHandler(new ChangeHandler()
		{
			@Override
			public void onChange(ChangeEvent theEvent)
			{
				new DefaultRequestBuilder(VeraUrlUtility.getRoomDimUrl(aTimedSlider.getValue(), myData.getID())).send();
			}
		});
		add(aTimedSlider);
		for(DeviceVO aDevice : myData.getDevices())
		{
			if(aDevice.isLight())
			{
				add(new DevicePanel(myData, aDevice));
			}
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
