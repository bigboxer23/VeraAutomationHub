package com.bigboxer23.lights.controllers.garage;

import com.bigboxer23.lights.controllers.AbstractBaseController;
import com.bigboxer23.lights.controllers.IStatusController;
import com.bigboxer23.lights.controllers.ISystemController;
import com.bigboxer23.lights.controllers.ITemperatureController;
import com.bigboxer23.lights.controllers.vera.VeraDeviceVO;
import com.bigboxer23.lights.controllers.vera.VeraHouseVO;
import com.bigboxer23.lights.data.WeatherData;
import com.bigboxer23.util.http.HttpClientUtils;
import com.google.gson.Gson;
import org.apache.http.client.methods.HttpGet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Control garage pi
 */
@Component
public class GarageController extends AbstractBaseController implements ISystemController, IStatusController, ITemperatureController
{
	@Value("${garage.url}")
	private String myGarageURL;

	public static final String kControllerEndpoint = "Garage";

	private VeraDeviceVO myGarageData;

	@Override
	public String doAction(List<String> theCommands)
	{
		if(theCommands.size() == 0 || theCommands.size() > 2)
		{
			return "Malformed input " + theCommands.size();
		}
		myLogger.error("Garage Door change requested: " + theCommands.get(0));
		myGarageData = fromJson(myGarageURL + "/" + theCommands.get(0) + (theCommands.size() == 2 ? "/" + theCommands.get(1) : ""), VeraDeviceVO.class);
		myGarageData.setName("Garage Opener");
		myGarageData.setStatus(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz").format(new Date()));
		return null;
	}

	/**
	 * Fetch data about the weather from the sensor in the garage
	 *
	 * @return
	 * @throws IOException
	 */
	@Override
	public WeatherData getWeatherData()
	{
		return new Gson().fromJson(HttpClientUtils.execute(new HttpGet(myGarageURL + "/Weather")), WeatherData.class);
	}

	@Override
	public boolean getStatus(int theLightId)
	{
		List<String> aList = new ArrayList<>();
		aList.add("Status");
		return Boolean.parseBoolean(doAction(aList));
	}

	public void getStatus(VeraHouseVO theHouseStatus)
	{
		if (myGarageData != null && theHouseStatus != null && theHouseStatus.getRooms() != null)
		{
			theHouseStatus.getRooms().stream().filter(aRoom -> aRoom.getName().equals("Garage")).forEach(aRoom -> aRoom.addDevice(myGarageData));
		}
	}

	@Scheduled(fixedDelay = 5000)
	private void fetchGarageStatus()
	{
		try
		{
			myLogger.debug("Fetching new garage data");
			myGarageData = fromJson(myGarageURL + "/Status2", VeraDeviceVO.class);
			if (myGarageData == null)
			{
				myLogger.info("Couldn't get status from garage node...");
				return;
			}
			myGarageData.setName("Garage Opener");
			myGarageData.setStatus(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz").format(new Date()));
			myLogger.debug("Fetched new garage data");
		} catch (Exception e)
		{
			myLogger.error("FetchGarageStatus", e);
		}
	}
}
