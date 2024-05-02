package com.bigboxer23.lights.controllers.aggregate;

/** */
public interface IHumidityEventHandler {
	void outOfWaterEvent(String deviceId, String deviceName, String deviceModel);
}
