package com.jones.matt.lights.controllers.vera;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
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

	public List<VeraRoomVO> getRooms()
	{
		return myRooms;
	}

	public List<VeraDeviceVO> getDevices()
	{
		return myDevices;
	}

	@Override
	public VeraHouseVO deserialize(JsonElement theJsonElement, Type theType, JsonDeserializationContext theJsonDeserializationContext) throws JsonParseException
	{
		VeraHouseVO anInstance = new Gson().fromJson(theJsonElement, theType);
		Map<Integer, VeraRoomVO> aRooms = new HashMap<>();
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
		anInstance.getDevices().clear();
		return anInstance;
	}
}
