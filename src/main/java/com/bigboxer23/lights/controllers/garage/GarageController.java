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
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

	public GarageController()
	{
		Executors.newScheduledThreadPool(10).scheduleWithFixedDelay(new Task(), 0, 5, TimeUnit.SECONDS);
	}

	@Override
	public String doAction(List<String> theCommands)
	{
		if(theCommands.size() != 1)
		{
			return "Malformed input " + theCommands.size();
		}
		myLogger.error("Garage Door change requested: " + theCommands.get(0));
		return HttpClientUtils.execute(new HttpGet(myGarageURL + "/" + theCommands.get(0)));
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

	private class Task extends TimerTask
	{
		@Override
		public void run()
		{
			myGarageData = getBuilder().create().fromJson(HttpClientUtils.execute(new HttpGet(myGarageURL + "/Status2")), VeraDeviceVO.class);
			myGarageData.setName("Garage Opener");
			myGarageData.setStatus(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz").format(new Date()));
		}
	}
}
