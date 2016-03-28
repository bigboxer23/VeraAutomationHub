package com.jones.matt.lights.controllers.vera;

import com.google.gson.annotations.SerializedName;

/**
 *
 */
public class VeraSceneVO
{
	@SerializedName("name")
	private String myName;

	@SerializedName("id")
	private int myId;

	@SerializedName("active")
	private int myActive;

	@SerializedName("room")
	private Integer myRoom;

	public Integer getRoom()
	{
		return myRoom;
	}

	public String getName()
	{
		return myName;
	}

	public int getId()
	{
		return myId;
	}

	public int getActive()
	{
		return myActive;
	}
}
