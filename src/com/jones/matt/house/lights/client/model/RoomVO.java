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
			if (aDevice.isOn() && aDevice.isLight())
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Is special "scene" room
	 *
	 * @return
	 */
	public final boolean isScenesList()
	{
		return getName().toLowerCase().equals("scenes");
	}

	public final native String getName() /*-{ return this.name; }-*/;

	public final native int getID() /*-{ return this.id; }-*/;

	public final native int getSection() /*-{ return this.section; }-*/;

	private final native JsArray<DeviceVO> getDevicesNative() /*-{ return this["devices"]; }-*/;

	public final List<DeviceVO> getDevices()
	{
		List<DeviceVO> aList = new ArrayList<>();
		if (getDevicesNative() != null)
		{
			for (int ai = 0; ai < getDevicesNative().length(); ai++)
			{
				aList.add(getDevicesNative().get(ai));
			}
		}
		return aList;
	}

	private final native JsArray<SceneVO> getScenesNative() /*-{ return this["scenes"]; }-*/;

	public final List<SceneVO> getScenes()
	{
		List<SceneVO> aList = new ArrayList<>();
		if (getScenesNative() != null)
		{
			for (int ai = 0; ai < getScenesNative().length(); ai++)
			{
				aList.add(getScenesNative().get(ai));
			}
		}
		return aList;
	}

	public final boolean hasLights()
	{
		for (DeviceVO aDevice : getDevices())
		{
			if (aDevice.isLight())
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * If contains items we want to display a button for.  Return false
	 * if no content matches anything we should do something with
	 *
	 * @return
	 */
	public final boolean shouldDisplay()
	{
		return !"Garage".equals(getName()) && (hasLights() || !getScenes().isEmpty());
	}
}
