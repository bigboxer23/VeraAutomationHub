package com.bigboxer23.lights.controllers.hue;

import com.bigboxer23.util.http.HttpClientUtils;
import com.google.gson.*;
import com.bigboxer23.lights.controllers.IStatusController;
import com.bigboxer23.lights.controllers.ISystemController;
import com.bigboxer23.lights.data.HueLightVO;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * controller for a philips hue light system
 */
public class HueController implements ISystemController, IStatusController
{
	private static final Logger myLogger = LoggerFactory.getLogger(HueController.class);

	/**
	 * Username to access lights with
	 */
	private static final String kUserName = System.getProperty("hueUserName", "test");

	/**
	 * Hue bridge address
	 */
	private static final String kHueBridge = System.getProperty("hueBridge", "localhost");

	private static final int kCachingDelay = Integer.getInteger("hueStatusCacheTime", 5000);

	private long myStatusTime = -1;

	/**
	 * Fetch global and cache for a tiny bit for speed
	 */
	private JsonElement myStatusObject;

	/**
	 * Get url to bridge with username
	 *
	 * @return
	 */
	protected String getBaseUrl()
	{
		return "http://" + kHueBridge + "/api/" + kUserName;
	}

	@Override
	public String doAction(List<String> theCommands)
	{
		//TODO: status fetch command (use for pulse as well so we reset initial state)
		String aCommand = theCommands.get(1);
		JsonObject aJsonElement = new JsonObject();
		String aLight = theCommands.get(0);
		String aUrl = getBaseUrl() + "/lights/" + aLight + "/state";
		if (aLight.equalsIgnoreCase("99"))
		{
			aUrl = getBaseUrl() + "/groups/0/action";
		}
		if (aCommand.equalsIgnoreCase("off"))
		{
			aJsonElement.addProperty("on", false);
		} else if(aCommand.equalsIgnoreCase("on"))
		{
			aJsonElement.addProperty("on", true);
			aJsonElement.addProperty("bri", 255);
			aJsonElement.addProperty("colormode", "ct");
			aJsonElement.addProperty("ct", 287);
		} else if (aCommand.equalsIgnoreCase("alert"))
		{
			aJsonElement.addProperty("on", true);
			aJsonElement.addProperty("alert", "select");
		} else if (aCommand.equalsIgnoreCase("pulse"))
		{
			aJsonElement.addProperty("on", true);
			aJsonElement.addProperty("transitiontime", 10);
			for (int ai = 1; ai <= 6; ai++)
			{
				//TODO: pretty rough pulse, need to smooth out still
				aJsonElement.addProperty("bri", ai % 2 == 0 ? 255 : 0);
				callBridge(aUrl, aJsonElement);
				try
				{
					Thread.sleep(1000);
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
			return null;
		} else if(aCommand.equalsIgnoreCase("movie"))
		{
			aJsonElement.addProperty("bri", 175);
			aJsonElement.addProperty("colormode", "ct");
			aJsonElement.addProperty("ct", 420);
		} else if(aCommand.equalsIgnoreCase("xy"))
		{
			aJsonElement.addProperty("on", true);
			aJsonElement.addProperty("colormode", "xy");
			float[] anXY= new float[]{Float.parseFloat(theCommands.get(2)), Float.parseFloat(theCommands.get(3))};
			aJsonElement.add("xy", new Gson().toJsonTree(anXY));
		}
		//Brightness command passed with
		if (theCommands.size() == 3)
		{
			try
			{
				aJsonElement.addProperty("bri", Integer.parseInt(theCommands.get(2)));
			} catch (NumberFormatException e){}
		}
		callBridge(aUrl, aJsonElement);
		return null;
	}

	private void callBridge(String theUrl, JsonObject theJsonObject)
	{
		HttpPut aPost = new HttpPut(theUrl);
		StringEntity anEntity = new StringEntity(theJsonObject.toString(), HTTP.UTF_8);
		anEntity.setContentType("application/json");
		aPost.setEntity(anEntity);
		HttpClientUtils.execute(aPost);
	}

	@Override
	public boolean getStatus(int theLightId)
	{
		getAllLightsCache();
		JsonElement aLight = myStatusObject.getAsJsonObject().get("" + theLightId);
		return aLight == null || new Gson().fromJson(aLight, HueLightVO.class).getState().isState();
	}

	/**
	 * Making lots of calls to the hub is much slower than just making a single call for all light status and caching
	 * for a tiny amount of time (assuming calls are simultaneously issued)
	 */
	private void getAllLightsCache()
	{
		if (myStatusObject == null || (System.currentTimeMillis() - myStatusTime) > kCachingDelay)
		{
			myLogger.trace("Getting new status");
			myStatusTime = System.currentTimeMillis();
			try
			{
				String aStatusString = HttpClientUtils.execute(new HttpGet(getBaseUrl() + "/lights/"));
				myLogger.trace("Status: " + aStatusString);
				myStatusObject = JsonParser.parseString(aStatusString);
			}
			catch (JsonSyntaxException e)
			{
				//don't care
			}
		}
	}
}
