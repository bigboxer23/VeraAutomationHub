package com.bigboxer23.lights.controllers.openHAB;

import com.bigboxer23.lights.controllers.AbstractBaseController;
import com.bigboxer23.lights.controllers.IStatusController;
import com.bigboxer23.lights.controllers.ISystemController;
import com.bigboxer23.util.http.HttpClientUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Control OpenHab instance via REST URL
 */
@Component
public class OpenHABController extends AbstractBaseController implements ISystemController, IStatusController
{
	/**
	 * Location of OpenHAB
	 */
	@Value("${openHABUrl}")
	private String kOpenHABUrl;

	public static final String kControllerEndpoint = "OpenHAB";

	public OpenHABHouse getStatus()
	{
		myLogger.debug("Getting OpenHAB Status");
		OpenHABHouse aHouseStatus = getBuilder().create().fromJson(HttpClientUtils.execute(new HttpGet(kOpenHABUrl + "/rest/items?type=Group&tags=Room&recursive=true")), OpenHABHouse.class);
		myLogger.debug("Got OpenHAB Status");
		return aHouseStatus;
	}

	@Override
	public boolean getStatus(int theLightId)
	{
		return false;
	}

	@Override
	public String doAction(List<String> theCommands)
	{
		if (theCommands.size() != 3)
		{
			myLogger.warn("Bad Request: " + theCommands);
			return null;
		}
		HttpPost aHttpPost = new HttpPost(kOpenHABUrl + "/rest/items/" + theCommands.get(1));
		String aContent = theCommands.get(2);
		if (theCommands.get(0).equalsIgnoreCase("room") && aContent.equalsIgnoreCase("1"))
		{
			aContent = "100";
		}
		try
		{
			aHttpPost.setEntity(new ByteArrayEntity(aContent.getBytes("UTF-8")));
		}
		catch (UnsupportedEncodingException theE)
		{
			myLogger.warn("OpenHABController:doAction", theE);
		}
		HttpClientUtils.execute(aHttpPost);
		return null;
	}
}
