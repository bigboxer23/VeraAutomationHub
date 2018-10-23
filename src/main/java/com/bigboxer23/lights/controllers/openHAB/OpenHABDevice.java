package com.bigboxer23.lights.controllers.openHAB;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * An OpenHAB device (item)
 */
public class OpenHABDevice
{
	@SerializedName("name")
	private String myName;

	@SerializedName("label")
	private String myLabel;

	@SerializedName("state")
	private String myState;

	@SerializedName("type")
	private String myType;

	@SerializedName("category")
	private String myCategory;

	@SerializedName("tags")
	private List<String> myTags;

	public String getName()
	{
		return myName;
	}

	public String getState()
	{
		return getLevel().equalsIgnoreCase("0") ? "0" : "1";
	}

	public String getLevel()
	{
		if (myType.equalsIgnoreCase("color"))
		{
			if (myState.lastIndexOf(",") < 0)
			{
				return "0";
			}
			return myState.substring(myState.lastIndexOf(",") + 1);
		} else if (myType.equalsIgnoreCase("switch"))
		{
			return myState.equalsIgnoreCase("off") ? "0" : "1";
		}
		return myState;
	}

	public String getType()
	{
		if (getTags().contains("ignore"))
		{
			return "0";
		}
		switch (myType.toLowerCase())
		{
			case "dimmer":
			case "color":
				return "2";
			case "switch":
				return "3";
		}
		return "0";
	}

	public String getCategory()
	{
		return myCategory;
	}

	public String getLabel()
	{
		return myLabel;
	}

	public List<String> getTags()
	{
		return myTags;
	}
}
