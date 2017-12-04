package com.bigboxer23.house.lights.client.model;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

/**
 *
 */
public class HouseStatus extends JavaScriptObject
{
	protected HouseStatus()
	{
	}

	public final native JsArray<RoomVO> getRooms() /*-{ return this["rooms"]; }-*/;

}
