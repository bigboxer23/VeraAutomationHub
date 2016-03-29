package com.jones.matt.house.lights.client.model;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * scene object w/name and ID
 */
public class SceneVO extends JavaScriptObject
{
	protected SceneVO()
	{
	}

	public final native String getName() /*-{ return this.name; }-*/;

	public final native int getID() /*-{ return this.id; }-*/;
}
