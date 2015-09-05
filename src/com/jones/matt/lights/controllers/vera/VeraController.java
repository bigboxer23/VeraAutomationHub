package com.jones.matt.lights.controllers.vera;

import com.google.gson.GsonBuilder;
import com.jones.matt.lights.controllers.IStatusController;
import com.jones.matt.lights.controllers.ISystemController;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Vera controller to make requests to a vera UI7 device
 */
public class VeraController implements ISystemController, IStatusController
{
	/**
	 * Location of Vera Hub, assume locally running on default port
	 */
	public static final String kVeraHubUrl = System.getProperty("vera.url", "http://localhost:3480");

	private GsonBuilder myBuilder;

	private VeraHouseVO myStatus;

	public static final String kControllerEndpoint = "Vera";

	public VeraController()
	{
		myBuilder = new GsonBuilder();
		myBuilder.registerTypeAdapter(VeraHouseVO.class, new VeraHouseVO());
	}

	@Override
	public boolean getStatus(int theLightId)
	{
		return false;
	}

	/**
	 * Reference: http://wiki.micasaverde.com/index.php/Luup_Requests
	 *
	 * @param theCommands
	 * @return
	 */
	@Override
	public String doAction(List<String> theCommands)
	{
		if (theCommands.size() != 3)
		{
			//TODO:fail, bad request
		}
		boolean anIsDim = true;
		String anAction = theCommands.get(0);
		try
		{
			Integer.parseInt(anAction);
		} catch (NumberFormatException aNFE)
		{
			//not dim
			anIsDim = false;
			anAction = anAction.equalsIgnoreCase("false") ? "0" : "1";
		}
		List<Integer> aLights = findLights(theCommands);
		for (Integer aLight : aLights)
		{
			DefaultHttpClient aHttpClient = new DefaultHttpClient();
			try
			{
				HttpGet aGet = new HttpGet(kVeraHubUrl + "/data_request?id=action&output_format=json&DeviceNum=" + aLight + "&serviceId=urn:upnp-org:serviceId:" + (anIsDim ? "Dimming1&action=SetLoadLevelTarget&newLoadlevelTarget=" : "SwitchPower1&action=SetTarget&newTargetValue=") + anAction);
				aHttpClient.execute(aGet);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Find all the lights we need to do the action to, vera doesn't seem to have any API exposed to hit the room directly.
	 *
	 * @param theCommands
	 * @return
	 */
	private List<Integer> findLights(List<String> theCommands)
	{
		List<Integer> aLights = new ArrayList<>();
		if (theCommands.get(1).equalsIgnoreCase("Room"))
		{
			if (myStatus != null)
			{
				for (VeraRoomVO aVeraRoomVO : myStatus.getRooms())
				{
					if (aVeraRoomVO.getId() == Integer.parseInt(theCommands.get(2)))
					{
						for (VeraDeviceVO aDevice : aVeraRoomVO.getDevices())
						{
							aLights.add(aDevice.getId());
						}
					}
				}
			}
		} else
		{
			aLights.add(Integer.parseInt(theCommands.get(2)));
		}
		return aLights;
	}

	public void setStatus(VeraHouseVO theStatus)
	{
		myStatus = theStatus;
	}
}
