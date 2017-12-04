package com.bigboxer23.house.lights.client.utility;

import com.bigboxer23.house.lights.client.HouseLights;

/**
 * Wrap calls to service here so we don't have to expose gross vera UPNP URL details
 */
public class VeraUrlUtility
{
	private static final String kVera = "S/Vera";

	private static final String kDevice = "/Device/";

	public static final String kScene = "/Scene/";

	private static final String kRoom = "/Room/";

	public static final String kSceneUrl = "/HomeAutomationGateway1&action=RunScene";

	private static final String kDim = "/Dimming1&action=SetLoadLevelTarget&newLoadlevelTarget=";

	private static final String kOnOff = "/SwitchPower1&action=SetTarget&newTargetValue=";

	private static final String kSetThermostat = "/TemperatureSetpoint1&action=SetCurrentSetpoint&NewCurrentSetpoint=";

	private static final String kSetThermostatMode = "/HVAC_UserOperatingMode1&action=SetModeTarget&NewModeTarget=";

	private static final String kSetThermostatFanMode = "/HVAC_FanOperatingMode1&action=SetMode&NewMode=";

	public static String getDimUrl(int theDim, int theDeviceId)
	{
		return getDimUrlInternal(theDim, theDeviceId, kDevice);
	}

	public static String getRoomDimUrl(int theDim, int theDeviceId)
	{
		return getDimUrlInternal(theDim, theDeviceId, kRoom);
	}

	private static String getDimUrlInternal(int theDim, int theDeviceId, String theSubject)
	{
		return HouseLights.getBaseUrl() + kVera + theSubject + theDeviceId + kDim + theDim;
	}

	public static String getSceneUrl(int theId)
	{
		return HouseLights.getBaseUrl() + kVera + kScene + theId + kSceneUrl;
	}

	public static String getOnOffUrl(boolean theTurnOn, int theDeviceId)
	{
		return getOnOffUrlInternal(theTurnOn, theDeviceId, kDevice);
	}

	public static String getRoomOnOffUrl(boolean theTurnOn, int theDeviceId)
	{
		return getOnOffUrlInternal(theTurnOn, theDeviceId, kRoom);
	}

	private static String getOnOffUrlInternal(boolean theTurnOn, int theDeviceId, String theSubject)
	{
		return HouseLights.getBaseUrl() + kVera + theSubject + theDeviceId + kOnOff + (theTurnOn ? "1" : "0");
	}

	public static String getThermostatSetUrl(int theTemperature, int theDeviceId)
	{
		return HouseLights.getBaseUrl() + kVera + kDevice + theDeviceId + kSetThermostat + theTemperature;
	}

	/**
	 * Possible values: HeatOn CoolOn Off
	 *
	 * @param theMode HeatOn CoolOn Off
	 * @param theDeviceId
	 * @return
	 */
	public static String getThermostatModeUrl(String theMode, int theDeviceId)
	{
		return HouseLights.getBaseUrl() + kVera + kDevice + theDeviceId + kSetThermostatMode + theMode;
	}

	public static String getFanModeUrl(String theMode, int theDeviceId)
	{
		return HouseLights.getBaseUrl() + kVera + kDevice + theDeviceId + kSetThermostatFanMode + theMode;
	}
}
