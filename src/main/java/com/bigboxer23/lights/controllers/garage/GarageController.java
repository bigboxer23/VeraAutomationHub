package com.bigboxer23.lights.controllers.garage;

import com.bigboxer23.lights.controllers.AbstractBaseController;
import com.bigboxer23.lights.controllers.IStatusController;
import com.bigboxer23.lights.controllers.ISystemController;
import com.bigboxer23.lights.controllers.ITemperatureController;
import com.bigboxer23.lights.controllers.vera.VeraHouseVO;
import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.bigboxer23.lights.controllers.vera.VeraDeviceVO;
import com.bigboxer23.lights.data.WeatherData;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

/**
 * Control garage pi
 */
@Component
public class GarageController extends AbstractBaseController implements ISystemController, IStatusController, ITemperatureController
{
	private static String kGarageURL = System.getProperty("garageURL", "https://192.168.0.8");

	public static final String kControllerEndpoint = "Garage";

	private VeraDeviceVO myGarageData;

	public GarageController()
	{
		new Timer().scheduleAtFixedRate(new Task(), 0, 5000);
	}

	@Override
	public String doAction(List<String> theCommands)
	{
		if(theCommands.size() != 1)
		{
			return "Malformed input " + theCommands.size();
		}
		try
		{
			URLConnection aConnection = new URL(kGarageURL + "/" + theCommands.get(0)).openConnection();
			return new String(ByteStreams.toByteArray(aConnection.getInputStream()), Charsets.UTF_8);
		}
		catch (Throwable e)
		{
			myLogger.log(Level.WARNING, "GarageController: ", e);
		}
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
		try
		{
			DefaultHttpClient aHttpClient = new DefaultHttpClient();
			HttpGet aRequest = new HttpGet(kGarageURL + "/Weather");
			HttpResponse aResponse = aHttpClient.execute(aRequest);
			String aWeatherString = new String(ByteStreams.toByteArray(aResponse.getEntity().getContent()), Charsets.UTF_8);
			return new Gson().fromJson(aWeatherString, WeatherData.class);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return null;
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

	private String myFailTime;

	private class Task extends TimerTask
	{
		@Override
		public void run()
		{
			try
			{
				long aStartTime = System.currentTimeMillis();
				HttpResponse aResponse = getHttpClient().execute(new HttpGet(GarageController.kGarageURL + "/Status2"));
				myGarageData = getBuilder().create().fromJson(new String(ByteStreams.toByteArray(aResponse.getEntity().getContent()), Charsets.UTF_8), VeraDeviceVO.class);
				myGarageData.setName("Garage Opener");
				/*myGarageData.setCategory("99");
				if (System.currentTimeMillis() - aStartTime > 4000)
				{
					throw new IOException();
				}*/
				myFailTime = null;
			}
			catch (IOException theE)
			{
				myLogger.log(Level.WARNING, "getStatus", theE);
				if (myFailTime == null)
				{
					myFailTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz").format(new Date());
				}
				myGarageData = new VeraDeviceVO();
				myGarageData.setName("Garage Opener");
				myGarageData.setCategory("99");
				myGarageData.setStatus(myFailTime);
				myGarageData.setTemperature("99");
			}
		}
	}
}
