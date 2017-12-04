package com.bigboxer23.house.lights.client.event;

import com.google.gwt.event.logical.shared.ValueChangeEvent;

/**
 *
 */
public class FireableValueChangeEvent<T> extends ValueChangeEvent<T>
{
	public FireableValueChangeEvent(T theData)
	{
		super(theData);
	}
}
