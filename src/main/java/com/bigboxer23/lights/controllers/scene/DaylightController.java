package com.bigboxer23.lights.controllers.scene;

import com.bigboxer23.lights.controllers.hue.HueController;
import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Get value from hue bridge
 */
@Component
public class DaylightController extends HueController
{
	private static final Logger myLogger = LoggerFactory.getLogger(DaylightController.class);

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
			myLogger.debug(aDaylightString);
			JsonElement anElement = new JsonParser().parse(aDaylightString);
			return anElement.getAsJsonObject().get("state").getAsJsonObject().get("daylight").getAsBoolean();
		}
		catch (IOException e)
		{
			myLogger.error("isDaylight:", e);
		}
		return false;
	}
}
