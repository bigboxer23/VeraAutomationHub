package com.bigboxer23.lights.controllers.hue;

import com.bigboxer23.lights.controllers.ISystemController;
import com.bigboxer23.lights.data.HueLightVO;
import com.bigboxer23.utils.http.OkHttpCallback;
import com.bigboxer23.utils.http.OkHttpUtil;
import com.google.gson.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

/** controller for a philips hue light system */
@Slf4j
public class HueController implements ISystemController {
	/** Username to access lights with */
	private static final String kUserName = System.getProperty("hueUserName", "test");

	/** Hue bridge address */
	private static final String kHueBridge = System.getProperty("hueBridge", "localhost");

	private static final int kCachingDelay = Integer.getInteger("hueStatusCacheTime", 5000);

	private long myStatusTime = -1;

	/** Fetch global and cache for a tiny bit for speed */
	private JsonElement myStatusObject;

	/**
	 * Get url to bridge with username
	 *
	 * @return
	 */
	protected String getBaseUrl() {
		return "http://" + kHueBridge + "/api/" + kUserName;
	}

	@Override
	public String doAction(List<String> theCommands) {
		// TODO: status fetch command (use for pulse as well so we reset initial state)
		String aCommand = theCommands.get(1);
		JsonObject aJsonElement = new JsonObject();
		String aLight = theCommands.get(0);
		String aUrl = getBaseUrl() + "/lights/" + aLight + "/state";
		if (aLight.equalsIgnoreCase("99")) {
			aUrl = getBaseUrl() + "/groups/0/action";
		}
		if (aCommand.equalsIgnoreCase("off")) {
			aJsonElement.addProperty("on", false);
		} else if (aCommand.equalsIgnoreCase("on")) {
			aJsonElement.addProperty("on", true);
			aJsonElement.addProperty("bri", 255);
			aJsonElement.addProperty("colormode", "ct");
			aJsonElement.addProperty("ct", 287);
		} else if (aCommand.equalsIgnoreCase("alert")) {
			aJsonElement.addProperty("on", true);
			aJsonElement.addProperty("alert", "select");
		} else if (aCommand.equalsIgnoreCase("pulse")) {
			aJsonElement.addProperty("on", true);
			aJsonElement.addProperty("transitiontime", 10);
			for (int ai = 1; ai <= 6; ai++) {
				// TODO: pretty rough pulse, need to smooth out still
				aJsonElement.addProperty("bri", ai % 2 == 0 ? 255 : 0);
				callBridge(aUrl, aJsonElement);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return null;
		} else if (aCommand.equalsIgnoreCase("movie")) {
			aJsonElement.addProperty("bri", 175);
			aJsonElement.addProperty("colormode", "ct");
			aJsonElement.addProperty("ct", 420);
		} else if (aCommand.equalsIgnoreCase("xy")) {
			aJsonElement.addProperty("on", true);
			aJsonElement.addProperty("colormode", "xy");
			float[] anXY = new float[] {Float.parseFloat(theCommands.get(2)), Float.parseFloat(theCommands.get(3))};
			aJsonElement.add("xy", new Gson().toJsonTree(anXY));
		}
		// Brightness command passed with
		if (theCommands.size() == 3) {
			try {
				aJsonElement.addProperty("bri", Integer.parseInt(theCommands.get(2)));
			} catch (NumberFormatException e) {
			}
		}
		callBridge(aUrl, aJsonElement);
		return null;
	}

	private void callBridge(String theUrl, JsonObject theJsonObject) {
		OkHttpUtil.put(
				theUrl,
				new OkHttpCallback(),
				builder -> builder.put(RequestBody.create(
						theJsonObject.toString().getBytes(StandardCharsets.UTF_8),
						MediaType.parse("application/json"))));
	}

	public boolean getStatus(int theLightId) {
		getAllLightsCache();
		JsonElement aLight = myStatusObject.getAsJsonObject().get("" + theLightId);
		return aLight == null
				|| new Gson().fromJson(aLight, HueLightVO.class).getState().isState();
	}

	/**
	 * Making lots of calls to the hub is much slower than just making a single call for all light
	 * status and caching for a tiny amount of time (assuming calls are simultaneously issued)
	 */
	private void getAllLightsCache() {
		if (myStatusObject == null || (System.currentTimeMillis() - myStatusTime) > kCachingDelay) {
			log.trace("Getting new status");
			myStatusTime = System.currentTimeMillis();
			try {
				OkHttpUtil.get(getBaseUrl() + "/lights/", new OkHttpCallback() {
					@Override
					public void onResponseBodyString(Call call, String stringBody) {
						log.trace("Status: " + stringBody);
						myStatusObject = JsonParser.parseString(stringBody);
					}
				});
			} catch (JsonSyntaxException e) {
				// don't care
			}
		}
	}
}
