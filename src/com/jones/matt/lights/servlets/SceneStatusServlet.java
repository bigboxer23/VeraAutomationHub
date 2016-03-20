package com.jones.matt.lights.servlets;

import com.google.gson.Gson;
import com.jones.matt.lights.HubContext;
import com.jones.matt.lights.controllers.garage.GarageController;
import com.jones.matt.lights.controllers.vera.VeraController;
import com.jones.matt.lights.controllers.vera.VeraHouseVO;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Get status from the vera controller for everything in the house
 */
public class SceneStatusServlet extends AbstractServlet
{
	@Override
	public void process(HttpServletRequest theRequest, HttpServletResponse theResponse) throws ServletException, IOException
	{
		theResponse.setContentType("application/json");
		VeraHouseVO aHouseStatus = HubContext.getInstance().getController(VeraController.kControllerEndpoint, VeraController.class).getStatus();
		HubContext.getInstance().getController(GarageController.kControllerEndpoint, GarageController.class).getStatus(aHouseStatus);
		theResponse.getOutputStream().print(new Gson().toJson(aHouseStatus));
		theResponse.getOutputStream().flush();
		theResponse.getOutputStream().close();
	}
}
