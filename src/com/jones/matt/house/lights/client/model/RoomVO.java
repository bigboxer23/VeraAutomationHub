package com.jones.matt.house.lights.client.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class RoomVO extends JavaScriptObject
{
	protected RoomVO()
	{
	}

	public final boolean isOn()
	{
		for(DeviceVO aDevice : getDevices())
		{
			if (aDevice.isOn())
			{
				return true;
			}
		}
		return false;
	}

	public final native String getName() /*-{ return this.name; }-*/;

	public final native int getID() /*-{ return this.id; }-*/;

	public final native int getSection() /*-{ return this.section; }-*/;

	private final native JsArray<DeviceVO> getDevicesNative() /*-{ return this["devices"]; }-*/;

	public final List<DeviceVO> getDevices()
	{
		List<DeviceVO> aList = new ArrayList<>();
		for (int ai = 0; ai < getDevicesNative().length(); ai++)
		{
			aList.add(getDevicesNative().get(ai));
		}
		return aList;
	}
}
