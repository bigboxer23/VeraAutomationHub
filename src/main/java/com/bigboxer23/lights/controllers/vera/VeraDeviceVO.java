package com.bigboxer23.lights.controllers.vera;

import com.bigboxer23.lights.controllers.openHAB.OpenHABItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Device returned from Vera controller
 */
@Data
@Schema(description = "JSON representing a device (light, switch, etc)")
public class VeraDeviceVO
{
	@Schema(description = "device name", required = true)
	private String name;

	@Schema(description = "device id", required = true)
	private String id;

	/**
	 * If set to value other than -1, use this value if a room control is requested.  IE
	 * load is 0, don't turn this light on if room light is requested.  If set to 50, dim and turn on to 50%
	 * Leaving at 100 or -1 means turn on as normal
	 */
	private int definedDim = -1;

	@Schema(description = "category of device")
	private String category;

	@Schema(description = "associated room's id")
	private int room;

	@Schema(description = "status of device")
	private String status;

	@Schema(description = "level of the device")
	private String level;

	@Schema(description = "string representing temperature at device (in c)")
	private String temperature;

	@Schema(description = "humidity level associated with device")
	private float humidity;

	@Schema(description = "auto close (in ms) associated with the device")
	private long autoClose;

	public VeraDeviceVO(String theName, float theLevel)
	{
		name = theName;
		level = "" + theLevel;
	}

	private VeraDeviceVO(OpenHABItem theDevice)
	{
		id = theDevice.getName();
		name = theDevice.getLabel();
		status = theDevice.getState();
		level = theDevice.getLevel();
		category = theDevice.getType();
	}

	public static List<VeraDeviceVO> fromOpenHab(List<OpenHABItem> theDevices)
	{
		return theDevices.stream().map(VeraDeviceVO::new).collect(Collectors.toList());
	}

	public final boolean isLight()
	{
		return getCategory() != null && (getCategory().equals("2") || getCategory().equalsIgnoreCase("3"));
	}
}
