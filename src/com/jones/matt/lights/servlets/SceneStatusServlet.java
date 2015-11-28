package com.jones.matt.lights.servlets;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jones.matt.lights.HubContext;
import com.jones.matt.lights.controllers.garage.GarageController;
import com.jones.matt.lights.controllers.vera.VeraController;
import com.jones.matt.lights.controllers.vera.VeraDeviceVO;
import com.jones.matt.lights.controllers.vera.VeraHouseVO;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Get status from the vera controller for everything in the house
 */
public class SceneStatusServlet extends AbstractServlet
{
	private GsonBuilder myBuilder;

	private VeraDeviceVO myGarageData;

	@Override
	public void init()
	{
		myBuilder = new GsonBuilder();
		myBuilder.registerTypeAdapter(VeraHouseVO.class, new VeraHouseVO());
	}

	@Override
	public void process(HttpServletRequest theRequest, HttpServletResponse theResponse) throws ServletException, IOException
	{
		theResponse.setContentType("application/json");
		DefaultHttpClient aHttpClient = new DefaultHttpClient();
		try
		{
			HttpResponse aResponse = aHttpClient.execute(new HttpGet(VeraController.kVeraHubUrl + "/data_request?id=sdata"));
			String aStatusString = new String(ByteStreams.toByteArray(aResponse.getEntity().getContent()), Charsets.UTF_8);
			VeraHouseVO aHouseStatus = myBuilder.create().fromJson(aStatusString, VeraHouseVO.class);
			if (myGarageData != null)
			{
				aHouseStatus.getRooms().stream().filter(aRoom -> aRoom.getName().equals("Garage")).forEach(aRoom ->
				{
					aRoom.addDevice(myGarageData);
				});
			}
			((VeraController)HubContext.getInstance().getControllers().get(VeraController.kControllerEndpoint)).setStatus(aHouseStatus);
			theResponse.getOutputStream().print(new Gson().toJson(aHouseStatus));

			aResponse = aHttpClient.execute(new HttpGet(GarageController.kGarageURL + "/Status2"));
			myGarageData = myBuilder.create().fromJson(new String(ByteStreams.toByteArray(aResponse.getEntity().getContent()), Charsets.UTF_8), VeraDeviceVO.class);
			myGarageData.setName("Garage Opener");
			myGarageData.setCategory("99");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		theResponse.getOutputStream().flush();
		theResponse.getOutputStream().close();
	}
}
