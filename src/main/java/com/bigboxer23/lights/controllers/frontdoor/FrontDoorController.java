package com.bigboxer23.lights.controllers.frontdoor;

import com.bigboxer23.lights.controllers.AbstractBaseController;
import com.bigboxer23.lights.controllers.ISystemController;
import com.bigboxer23.lights.controllers.vera.VeraHouseVO;
import com.bigboxer23.util.http.HttpClientUtils;
import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 *
 */
@Component
public class FrontDoorController extends AbstractBaseController implements ISystemController
{
	@Value("${frontDoor.url}")
	private String myFrontDoorURL;

	public static final String kControllerEndpoint = "FrontDoor";

	private int myFrontDoorPauseTime = 0;

	public void getStatus(VeraHouseVO theHouseStatus)
	{
		theHouseStatus.setFrontDoor(myFrontDoorPauseTime);
	}

	@Override
	public String doAction(List<String> theCommands)
	{
		return null;//TODO
	}

	@Scheduled(fixedDelay = 10000)
	private void fetchFrontDoorStatus()
	{
		try
		{
			myLogger.debug("Fetching front door status");
			myFrontDoorPauseTime = Optional.ofNullable(
					HttpClientUtils.execute(new HttpGet(myFrontDoorURL + "/isPaused")))
					.map(Integer::parseInt)
					.orElse(0);
			myLogger.debug("Fetched front door status " + myFrontDoorPauseTime);
		} catch (Exception e)
		{
			myLogger.error("fetchFrontDoorStatus", e);
		}
	}
}
