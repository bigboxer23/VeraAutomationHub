package com.bigboxer23.lights.controllers.vera;

import com.bigboxer23.lights.controllers.openHAB.OpenHABHouse;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data structure returned from vera when status is requested
 */
public class VeraHouseVO implements JsonDeserializer<VeraHouseVO>
{
	@SerializedName("rooms")
	private List<VeraRoomVO> myRooms;

	@SerializedName("devices")
	private List<VeraDeviceVO> myDevices;

	@SerializedName("scenes")
	private List<VeraSceneVO> myScenes;

	public List<VeraRoomVO> getRooms()
	{
		return myRooms;
	}

	public List<VeraDeviceVO> getDevices()
	{
		return myDevices;
	}

	public List<VeraSceneVO> getScenes()
	{
		return myScenes;
	}

	public VeraHouseVO()
	{

	}

	public VeraHouseVO(OpenHABHouse theHouse)
	{
		myRooms = VeraRoomVO.fromOpenHab(theHouse);
	}

	@Override
	public VeraHouseVO deserialize(JsonElement theJsonElement, Type theType, JsonDeserializationContext theJsonDeserializationContext) throws JsonParseException
	{
		VeraHouseVO anInstance = new Gson().fromJson(theJsonElement, theType);
		Map<String, VeraRoomVO> aRooms = new HashMap<>();
		for (VeraRoomVO aRoom : anInstance.getRooms())
		{
			aRooms.put(aRoom.getId(), aRoom);
		}
		for (VeraDeviceVO aDevice : anInstance.getDevices())
		{
			VeraRoomVO aRoom = aRooms.get(aDevice.getRoom());
			if (aRoom != null)
			{
				aRoom.addDevice(aDevice);
			}
		}
		for (VeraSceneVO aScene : anInstance.getScenes())
		{
			VeraRoomVO aRoom = aRooms.get(aScene.getRoom());
			if (aRoom == null)
			{
				aRoom = new VeraRoomVO("Scenes", 0);
				aRooms.put("0", aRoom);
				anInstance.getRooms().add(aRoom);
			}
			if (aRoom != null)
			{
				aRoom.addScene(aScene);
			}
		}
		//Sort scene to top of list so displayed first in UI
		Collections.sort(anInstance.getRooms(), (theRoomVO, theRoomVO2) -> theRoomVO.getName().equalsIgnoreCase("scenes") ? -1 : theRoomVO.getName().compareToIgnoreCase(theRoomVO2.getName()));
		return anInstance;
	}
}
