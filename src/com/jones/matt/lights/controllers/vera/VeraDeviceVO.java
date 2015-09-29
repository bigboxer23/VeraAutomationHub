package com.jones.matt.lights.controllers.vera;

import com.google.gson.annotations.SerializedName;

/**
 * Device returned from Vera controller
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

	@SerializedName("fanmode")
	private String myFanMode;

	@SerializedName("mode")
	private String myMode;

	@SerializedName("batterylevel")
	private String myBatteryLevel;

	@SerializedName("setpoint")
	private String mySetPoint;

	@SerializedName("heat")
	private String myHeat;

	@SerializedName("cool")
	private String myCool;

	@SerializedName("hvacstate")
	private String myHvacState;

	@SerializedName("temperature")
	private String myTemperature;

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

	public String getFanMode()
	{
		return myFanMode;
	}

	public String getMode()
	{
		return myMode;
	}

	public String getBatteryLevel()
	{
		return myBatteryLevel;
	}

	public String getSetPoint()
	{
		return mySetPoint;
	}

	public String getHeat()
	{
		return myHeat;
	}

	public String getCool()
	{
		return myCool;
	}

	public String getHvacState()
	{
		return myHvacState;
	}

	public String getTemperature()
	{
		return myTemperature;
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
