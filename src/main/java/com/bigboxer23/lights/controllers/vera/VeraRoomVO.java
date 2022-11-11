package com.bigboxer23.lights.controllers.vera;

import com.bigboxer23.lights.controllers.openHAB.OpenHABItem;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * single room data structure
 */
@Schema(description = "JSON representing a room")
public class VeraRoomVO
{
	@Schema(description = "name of room")
	private String name;

	@Schema(description = "room's id")
	private String id;

	@Schema(description = "Individual devices within a room")
	private List<VeraDeviceVO> devices;

	@Schema(description = "Scenes within the room")
	private List<VeraSceneVO> scenes;

	public VeraRoomVO(String theName, int theId)
	{
		name = theName;
		id = theId + "";
	}

	private VeraRoomVO(OpenHABItem theRoom)
	{
		name = theRoom.getLabel();
		id = theRoom.getName();
		devices = VeraDeviceVO.fromOpenHab(theRoom.getItems());
	}

	public static List<VeraRoomVO> fromOpenHab(List<OpenHABItem> theOpenHABHouse)
	{
		return theOpenHABHouse.stream().map(VeraRoomVO::new).collect(Collectors.toList());
	}

	public List<VeraDeviceVO> getDevices()
	{
		return devices;
	}

	public String getName()
	{
		return name;
	}

	public String getId()
	{
		return id;
	}

	public void addDevice(VeraDeviceVO theDevice)
	{
		if (devices == null)
		{
			devices = new ArrayList<>();
		}
		devices.add(theDevice);
	}

	public void addScene(VeraSceneVO theScene)
	{
		if (scenes == null)
		{
			scenes = new ArrayList<>();
		}
		scenes.add(theScene);
	}

	@Hidden
	public void setSmart(boolean theSmart)
	{
		if (theSmart && !id.startsWith("Smart"))
		{
			id = "Smart" + id;
		}
	}
}
