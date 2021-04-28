package com.bigboxer23.lights.controllers.scene;

import com.bigboxer23.lights.controllers.hue.HueController;
import com.bigboxer23.util.http.HttpClientUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
		String aDaylightString = HttpClientUtils.execute(new HttpGet(getBaseUrl() + "/sensors/1"));
		myLogger.debug(aDaylightString);
		JsonElement anElement = JsonParser.parseString(aDaylightString);
		return anElement.getAsJsonObject().get("state").getAsJsonObject().get("daylight").getAsBoolean();
	}
}
