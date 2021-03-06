package com.bigboxer23.lights.controllers.openHAB;

import com.bigboxer23.lights.controllers.AbstractBaseController;
import com.bigboxer23.lights.controllers.IStatusController;
import com.bigboxer23.lights.controllers.ISystemController;
import com.bigboxer23.util.http.HttpClientUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

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

	private Set<String> mySmartRooms;

	public OpenHABHouse getStatus()
	{
		myLogger.debug("Getting OpenHAB Status");
		OpenHABHouse aHouseStatus = fromJson(kOpenHABUrl + "/rest/items?type=Group&tags=Room&recursive=true", OpenHABHouse.class);
		myLogger.debug("Got OpenHAB Status");
		return aHouseStatus;
	}

	public Set<String> getSmartRooms()
	{
		if (mySmartRooms == null)
		{
			fetchSmartRooms();
		}
		return mySmartRooms;
	}

	@Override
	public boolean getStatus(int theLightId)
	{
		return false;
	}

	@Override
	public String doAction(List<String> theCommands)
	{
		if (theCommands.size() != 2)
		{
			myLogger.warn("Bad Request: " + theCommands);
			return null;
		}
		HttpPost aHttpPost = new HttpPost(kOpenHABUrl + "/rest/items/" + theCommands.get(0));
		try
		{
			aHttpPost.setEntity(new ByteArrayEntity(URLDecoder.decode(theCommands.get(1), StandardCharsets.UTF_8.displayName()).getBytes(StandardCharsets.UTF_8)));
		}
		catch (UnsupportedEncodingException theE)
		{
			myLogger.warn("OpenHABController:doAction", theE);
		}
		HttpClientUtils.execute(aHttpPost);
		return null;
	}

	public OpenHABHouse getItemsByTag(String theTag)
	{
		return fromJson(kOpenHABUrl + "/rest/items?tags=" + theTag, OpenHABHouse.class);
	}

	public List<OpenHABItem> getItemByName(String theName)
	{
		OpenHABItem anItem = fromJson(kOpenHABUrl + "/rest/items/" + theName, OpenHABItem.class);
		return anItem == null || anItem.getName() == null ? null : Collections.singletonList(anItem);
	}

	public void setLevel(String theItem, int theLevel)
	{
		List<String> aCommands = new ArrayList<>();
		aCommands.add(theItem);
		aCommands.add("" + theLevel);
		doAction(aCommands);
	}

	/**
	 * Turn on or off vacation mode to change global behavior based on further scene rules
	 *
	 * @param theVacationMode
	 */
	public void setVacationMode(boolean theVacationMode)
	{
		myLogger.info("Vacation mode requested: " + theVacationMode);
		List<String> aCommands = new ArrayList<>();
		aCommands.add("VacationMode");
		aCommands.add(theVacationMode ? "ON" : "OFF");
		doAction(aCommands);
	}

	@Scheduled(fixedDelay = 5000)
	private void fetchSmartRooms()
	{
		try
		{
			myLogger.debug("Getting Smart Rooms");
			mySmartRooms = Optional.ofNullable(fromJson(kOpenHABUrl + "/rest/items?tags=SmartRoom", OpenHABHouse.class)).
					orElse(new OpenHABHouse()).
					stream().
					map(OpenHABItem::getName).
					collect(Collectors.toSet());
			myLogger.debug("Retrieved Smart Rooms");
		} catch (Exception e)
		{
			myLogger.error("fetchSmartRooms", e);
		}
	}
}
