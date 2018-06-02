package com.bigboxer23.lights.controllers.vera;

import com.bigboxer23.lights.controllers.AbstractBaseController;
import com.bigboxer23.lights.controllers.IStatusController;
import com.bigboxer23.lights.controllers.ISystemController;
import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Vera controller to make requests to a vera UI7 device
 */
@Component
public class VeraController extends AbstractBaseController implements ISystemController, IStatusController
{
	/**
	 * Location of Vera Hub, assume locally running on default port
	 */
	@Value("${veraUrl}")
	private String kVeraHubUrl;

	private static final String kVeraBaseRequest = "/data_request?id=action&output_format=json";

	public static final String kVeraRequest = kVeraBaseRequest + "&DeviceNum=";

	private static final String kVeraSceneRequest = kVeraBaseRequest + "&SceneNum=";

	public static final String kVeraServiceUrn = "&serviceId=urn:upnp-org:serviceId:";

	private static final String kSceneUrn = "&serviceId=urn:micasaverde-com:serviceId:";

	public static final String kDimmingCommand = "Dimming1&action=SetLoadLevelTarget&newLoadlevelTarget=";

	private VeraHouseVO myStatus;

	public static final String kControllerEndpoint = "Vera";

	@Override
	public boolean getStatus(int theLightId)
	{
		return false;
	}

	/**
	 * Query particular scene id to get data about that scene
	 * @param theSceneID
	 * @return
	 */
	public JsonObject getSceneInformation(int theSceneID)
	{
		myLogger.info("Getting Vera Level");
		try
		{
			HttpResponse aResponse = getHttpClient().execute(new HttpGet(kVeraHubUrl + "/data_request?id=scene&action=list&scene=" + theSceneID));
			return new JsonParser().parse(new String(ByteStreams.toByteArray(aResponse.getEntity().getContent()), Charsets.UTF_8)).getAsJsonObject();
		}
		catch (IOException theE)
		{
			myLogger.log(Level.WARNING, "getRoomLevels", theE);
		}
		return null;
	}

	public VeraHouseVO getStatus()
	{
		myLogger.info("Getting Vera Status");
		try
		{
			HttpResponse aResponse = getHttpClient().execute(new HttpGet(kVeraHubUrl + "/data_request?id=sdata"));
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
		return null;
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
		findLights(theCommands).forEach(theDeviceAction -> doDeviceAction(theDeviceAction, theCommands.get(0).equalsIgnoreCase("scene")));
		return null;
	}

	private void doDeviceAction(DeviceAction theAction, boolean theScene)
	{
		DefaultHttpClient aHttpClient = new DefaultHttpClient();
		try
		{
			HttpGet aGet = new HttpGet(kVeraHubUrl + (theScene ? kVeraSceneRequest : kVeraRequest) + theAction.getId() + (theScene ? kSceneUrn : kVeraServiceUrn) + theAction.getAction());
			aHttpClient.execute(aGet);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Find all the lights we need to do the action to, vera doesn't seem to have any API exposed to hit the room directly.
	 *
	 * @param theCommands
	 * @return
	 */
	private List<DeviceAction> findLights(List<String> theCommands)
	{
		List<DeviceAction> aLights = new ArrayList<>();
		if (theCommands.get(0).equalsIgnoreCase("Room"))
		{
			if (myStatus != null)
			{
				for (VeraRoomVO aVeraRoomVO : myStatus.getRooms())
				{
					if (aVeraRoomVO.getId() == Integer.parseInt(theCommands.get(1)))
					{
						aVeraRoomVO.getDevices().stream().filter(VeraDeviceVO::isLight).forEach(theVeraDeviceVO ->
						{
							String anAction = theCommands.get(2);
							if (theVeraDeviceVO.getDefinedDim() > 0 && theVeraDeviceVO.getDefinedDim() <= 100 && !anAction.endsWith("0"))
							{
								anAction = kDimmingCommand + theVeraDeviceVO.getDefinedDim();
							}
							if (theVeraDeviceVO.getDefinedDim() != 0 || anAction.endsWith("0"))
							{
								aLights.add(new DeviceAction(anAction, theVeraDeviceVO.getId()));
							}
						});
					}
				}
			}
		} else
		{
			aLights.add(new DeviceAction(theCommands.get(2), Integer.parseInt(theCommands.get(1))));
		}
		return aLights;
	}

	public void setStatus(VeraHouseVO theStatus)
	{
		myStatus = theStatus;
	}

	/**
	 * Encapsulate the action to run (dim or on/off) and the device id
	 */
	private class DeviceAction
	{
		private String myAction;

		private int myId;

		public DeviceAction(String theAction, int theId)
		{
			myAction = theAction;
			myId = theId;
		}

		public int getId()
		{
			return myId;
		}

		public String getAction()
		{
			return myAction;
		}
	}
}
