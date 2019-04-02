package com.bigboxer23.lights.controllers.vera;

import com.bigboxer23.lights.controllers.openHAB.OpenHABItem;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * single room data structure
 */
public class VeraRoomVO
{
	@SerializedName("name")
	private String myName;

	@SerializedName("id")
	private String myId;

	@SerializedName("devices")
	private List<VeraDeviceVO> myDevices;

	@SerializedName("scenes")
	private List<VeraSceneVO> myScenes;

	public VeraRoomVO(String theName, int theId)
	{
		myName = theName;
		myId = theId + "";
	}

	private VeraRoomVO(OpenHABItem theRoom)
	{
		myName = theRoom.getLabel();
		myId = theRoom.getName();
		myDevices = VeraDeviceVO.fromOpenHab(theRoom.getItems());
	}

	public static List<VeraRoomVO> fromOpenHab(List<OpenHABItem> theOpenHABHouse)
	{
		return theOpenHABHouse.stream().map(VeraRoomVO::new).collect(Collectors.toList());
	}

	public List<VeraDeviceVO> getDevices()
	{
		return myDevices;
	}

	public String getName()
	{
		return myName;
	}

	public String getId()
	{
		return myId;
	}

	public void addDevice(VeraDeviceVO theDevice)
	{
		if (myDevices == null)
		{
			myDevices = new ArrayList<>();
		}
		myDevices.add(theDevice);
	}

	public void addScene(VeraSceneVO theScene)
	{
		if (myScenes == null)
		{
			myScenes = new ArrayList<>();
		}
		myScenes.add(theScene);
	}

	public void setSmart(boolean theSmart)
	{
		if (theSmart && !myId.startsWith("Smart"))
		{
			myId = "Smart" + myId;
		}
	}
}
