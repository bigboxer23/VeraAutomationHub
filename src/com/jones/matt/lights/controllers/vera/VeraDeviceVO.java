package com.jones.matt.lights.controllers.vera;

import com.google.gson.annotations.SerializedName;

/**
 *
 */
public class VeraDeviceVO
{
	@SerializedName("name")
	private String myName;

	@SerializedName("id")
	private int myId;

	@SerializedName("category")
	private String myCategory;

	@SerializedName("room")
	private int myRoom;

	@SerializedName("status")
	private String myStatus;

	@SerializedName("level")
	private String myLevel;

	public String getName()
	{
		return myName;
	}

	public int getId()
	{
		return myId;
	}

	public String getCategory()
	{
		return myCategory;
	}

	public int getRoom()
	{
		return myRoom;
	}

	public boolean getStatus()
	{
		return myStatus != null && myStatus.equalsIgnoreCase("1");
	}

	public int getLevel()
	{
		try
		{
			return Integer.parseInt(myLevel);
		} catch (NumberFormatException aNFE)
		{
			return 0;
		}
	}
}
