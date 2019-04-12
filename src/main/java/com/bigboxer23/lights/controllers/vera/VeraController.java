package com.bigboxer23.lights.controllers.vera;

import com.bigboxer23.lights.controllers.AbstractBaseController;
import com.bigboxer23.lights.controllers.IStatusController;
import com.bigboxer23.lights.controllers.ISystemController;
import com.bigboxer23.util.http.HttpClientUtils;
import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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

	public VeraHouseVO getStatus()
	{
		myLogger.debug("Getting Vera Status");
		VeraHouseVO aHouseStatus = fromJson(kVeraHubUrl + "/data_request?id=sdata", VeraHouseVO.class);
		setStatus(aHouseStatus);
		myLogger.debug("Got Vera Status");
		return aHouseStatus;
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
		HttpClientUtils.execute(new HttpGet(kVeraHubUrl + (theScene ? kVeraSceneRequest : kVeraRequest) + theAction.getId() + (theScene ? kSceneUrn : kVeraServiceUrn) + theAction.getAction()));
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
					if (aVeraRoomVO.getId().equalsIgnoreCase(theCommands.get(1)))
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
			aLights.add(new DeviceAction(theCommands.get(2), theCommands.get(1)));
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

		private String myId;

		public DeviceAction(String theAction, String theId)
		{
			myAction = theAction;
			myId = theId;
		}

		public String getId()
		{
			return myId;
		}

		public String getAction()
		{
			return myAction;
		}
	}
}
