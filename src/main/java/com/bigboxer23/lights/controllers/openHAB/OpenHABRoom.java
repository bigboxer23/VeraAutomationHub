package com.bigboxer23.lights.controllers.openHAB;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * an open hab group with a tag of 'room' applied to group items
 */
public class OpenHABRoom
{
	@SerializedName("name")
	private String myName;

	@SerializedName("label")
	private String myLabel;

	@SerializedName("members")
	private List<OpenHABDevice> myDevices;

	public String getName()
	{
		return myName;
	}

	public String getLabel()
	{
		return myLabel;
	}

	public List<OpenHABDevice> getDevices()
	{
		return myDevices;
	}
}
