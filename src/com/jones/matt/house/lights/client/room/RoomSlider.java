package com.jones.matt.house.lights.client.room;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Timer;
import com.googlecode.mgwt.ui.client.widget.input.slider.Slider;
import com.jones.matt.house.lights.client.event.FireableChangeEvent;

import java.util.logging.Logger;

/**
 *
 */
public class RoomSlider extends Slider implements ValueChangeHandler<Integer>, HasChangeHandlers
{
	private Timer myTimer;

	public RoomSlider()
	{
		addStyleName("RoomSlider");
		setMax(101);
		setValue(100);
		addValueChangeHandler(this);
		myTimer = new Timer()
		{
			@Override
			public void run()
			{
				fireEvent(new FireableChangeEvent());
			}
		};
	}

	@Override
	public void onValueChange(ValueChangeEvent<Integer> theEvent)
	{
		myTimer.schedule(500);
	}

	@Override
	public HandlerRegistration addChangeHandler(ChangeHandler handler)
	{
		return addHandler(handler, ChangeEvent.getType());
	}
}
