package com.jones.matt.house.lights.client;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.*;
import com.jones.matt.house.lights.client.model.RoomVO;

/**
 *
 */
public class EventBusInstance implements HasValueChangeHandlers<RoomVO>
{
	private static EventBusInstance myInstance;

	private HandlerManager myHandlerManager;

	private EventBusInstance()
	{
		myHandlerManager = new HandlerManager(this);
	}

	public static EventBusInstance getInstance()
	{
		if (myInstance == null)
		{
			myInstance = new EventBusInstance();
		}
		return myInstance;
	}

	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<RoomVO> theHandler)
	{
		return myHandlerManager.addHandler(ValueChangeEvent.<RoomVO>getType(), theHandler);
	}

	@Override
	public void fireEvent(GwtEvent<?> theEvent)
	{
		myHandlerManager.fireEvent(theEvent);
	}
}
