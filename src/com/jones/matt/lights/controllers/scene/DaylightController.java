package com.jones.matt.lights.controllers.scene;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.jones.matt.lights.controllers.hue.HueController;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Get value from hue bridge
 */
public class DaylightController extends HueController
{
	private static Logger myLogger = Logger.getLogger("com.jones");

	public static final String kControllerEndpoint = "Daylight";

	@Override
	public String doAction(List<String> theCommands)
	{
		return "" + isDaylight();
	}

	/**
	 * Get daylight value from hue bridge
	 *
	 * @return
	 */
	public boolean isDaylight()
	{
		try
		{
			DefaultHttpClient aHttpClient = new DefaultHttpClient();
			HttpGet aRequest = new HttpGet(getBaseUrl() + "/sensors/1");
			HttpResponse aResponse = aHttpClient.execute(aRequest);
			String aDaylightString = new String(ByteStreams.toByteArray(aResponse.getEntity().getContent()), Charsets.UTF_8);
			myLogger.config(aDaylightString);
			JsonElement anElement = new JsonParser().parse(aDaylightString);
			return anElement.getAsJsonObject().get("state").getAsJsonObject().get("daylight").getAsBoolean();
		}
		catch (IOException e)
		{
			myLogger.log(Level.WARNING, "isDaylight:", e);
		}
		return false;
	}
}
