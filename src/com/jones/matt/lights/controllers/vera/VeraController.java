package com.jones.matt.lights.controllers.vera;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.jones.matt.lights.controllers.AbstractBaseController;
import com.jones.matt.lights.controllers.IStatusController;
import com.jones.matt.lights.controllers.ISystemController;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Vera controller to make requests to a vera UI7 device
 */
public class VeraController extends AbstractBaseController implements ISystemController, IStatusController
{
	/**
	 * Location of Vera Hub, assume locally running on default port
	 */
	private static final String kVeraHubUrl = System.getProperty("vera.url", "http://localhost:3480");

	private static final String kVeraRequest = "/data_request?id=action&output_format=json&DeviceNum=";

	private static final String kVeraServiceUrn = "&serviceId=urn:upnp-org:serviceId:";

	private VeraHouseVO myStatus;

	public static final String kControllerEndpoint = "Vera";

	@Override
	public boolean getStatus(int theLightId)
	{
		return false;
	}

	public VeraHouseVO getStatus()
	{
		myLogger.info("Getting Vera Status");
		try
		{
			HttpResponse aResponse = getHttpClient().execute(new HttpGet(VeraController.kVeraHubUrl + "/data_request?id=sdata"));
			String aStatusString = new String(ByteStreams.toByteArray(aResponse.getEntity().getContent()), Charsets.UTF_8);
			VeraHouseVO aHouseStatus = getBuilder().create().fromJson(aStatusString, VeraHouseVO.class);
			setStatus(aHouseStatus);
			myLogger.info("Got Vera Status");
			return aHouseStatus;
		}
		catch (IOException theE)
		{
			myLogger.log(Level.WARNING, "getStatus", theE);
		}
		return new VeraHouseVO();
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
		String anAction = theCommands.get(2);
		List<Integer> aLights = findLights(theCommands);
		for (Integer aDeviceId : aLights)
		{
			DefaultHttpClient aHttpClient = new DefaultHttpClient();
			try
			{
				HttpGet aGet = new HttpGet(kVeraHubUrl + kVeraRequest + aDeviceId + kVeraServiceUrn + anAction);
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
		if (theCommands.get(0).equalsIgnoreCase("Room"))
		{
			if (myStatus != null)
			{
				for (VeraRoomVO aVeraRoomVO : myStatus.getRooms())
				{
					if (aVeraRoomVO.getId() == Integer.parseInt(theCommands.get(1)))
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
			aLights.add(Integer.parseInt(theCommands.get(1)));
		}
		return aLights;
	}

	public void setStatus(VeraHouseVO theStatus)
	{
		myStatus = theStatus;
	}
}
