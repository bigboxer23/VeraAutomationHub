package com.jones.matt.house.lights.client.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 *
 */
public class DeviceVO extends JavaScriptObject
{
	protected DeviceVO()
	{
	}

	public final boolean isOn()
	{
		return getStatus() != null && getStatus().equalsIgnoreCase("1");
	}

	public final native String getName() /*-{ return this.name; }-*/;

	public final native int getID() /*-{ return this.id; }-*/;

	public final native int getCategory() /*-{ return this.category; }-*/;

	public final native int getRoom() /*-{ return this.room; }-*/;

	public final native String getStatus() /*-{ return this.status; }-*/;

	public final native String getLevel() /*-{ return this.level; }-*/;

	public final boolean isLight()
	{
		return getCategory() == 2 || getCategory() == 3;
	}
}
