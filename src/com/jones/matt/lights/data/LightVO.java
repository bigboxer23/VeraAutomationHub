package com.jones.matt.lights.data;

import com.google.gson.annotations.SerializedName;

/**
 * Encapsulate a singular light including it's type, id, and brightness on action
 */
public class LightVO
{
	@SerializedName("type")
	private String myType;

	@SerializedName("id")
	private int myId;

	@SerializedName("brightness")
	private int myBrightness;

	@SerializedName("movie")
	private String myMovieModeAction;

	public String getType()
	{
		return myType;
	}

	public int getId()
	{
		return myId;
	}

	public int getBrightness()
	{
		return myBrightness;
	}

	public String getMovieModeAction()
	{
		return myMovieModeAction;
	}
}
