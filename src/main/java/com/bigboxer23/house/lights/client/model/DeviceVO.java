package com.bigboxer23.house.lights.client.model;

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

	public final native String getFanMode() /*-{ return this.fanmode; }-*/;

	public final native String getMode() /*-{ return this.mode; }-*/;

	public final native String getBatteryLevel() /*-{ return this.batterylevel; }-*/;

	public final native float getSetpoint() /*-{ return this.setpoint; }-*/;

	public final native String getHeat() /*-{ return this.heat; }-*/;

	public final native String getCool() /*-{ return this.cool; }-*/;

	public final native String getHvacState() /*-{ return this.hvacstate; }-*/;

	public final native String getTemperature() /*-{ return this.temperature; }-*/;

	public final native String getHumidity() /*-{ return this.humidity; }-*/;

	public final native boolean getDoor() /*-{ return this.door; }-*/;

	public final native int getAutoClose() /*-{ return this.autoClose; }-*/;

	public final boolean isLight()
	{
		return getCategory() == 2 || getCategory() == 3;
	}
}
