package com.jones.matt.lights.data;

import com.google.gson.annotations.SerializedName;

/**
 * overlay for information from a hue light
 */
public class HueLightVO
{
	@SerializedName("state")
	private HueLightStateVO myState;

	@SerializedName("type")
	private String myType;

	@SerializedName("name")
	private String myName;

	@SerializedName("modelid")
	private String myModelId;

	@SerializedName("uniqueid")
	private String myUniqueId;

	@SerializedName("swversion")
	private String mySWVersion;

	/*@SerializedName("pointsymbol")
	private String myPointSymbol;*/

	public HueLightStateVO getState()
	{
		return myState;
	}

	public String getType()
	{
		return myType;
	}

	public String getName()
	{
		return myName;
	}

	public String getModelId()
	{
		return myModelId;
	}

	public String getUniqueId()
	{
		return myUniqueId;
	}

	public String getSWVersion()
	{
		return mySWVersion;
	}

	/*public String getPointSymbol()
	{
		return myPointSymbol;
	}*/
}
