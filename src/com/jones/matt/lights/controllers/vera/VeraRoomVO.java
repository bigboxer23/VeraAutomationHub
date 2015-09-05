package com.jones.matt.lights.controllers.vera;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * single room data structure
 */
public class VeraRoomVO
{
	@SerializedName("name")
	private String myName;

	@SerializedName("id")
	private int myId;

	@SerializedName("section")
	private int mySection;

	@SerializedName("devices")
	private List<VeraDeviceVO> myDevices;

	public List<VeraDeviceVO> getDevices()
	{
		return myDevices;
	}

	public String getName()
	{
		return myName;
	}

	public int getId()
	{
		return myId;
	}

	public int getSection()
	{
		return mySection;
	}

	public void addDevice(VeraDeviceVO theDevice)
	{
		if (myDevices == null)
		{
			myDevices = new ArrayList<>();
		}
		myDevices.add(theDevice);
	}
}
