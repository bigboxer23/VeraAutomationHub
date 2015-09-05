package com.jones.matt.lights.servlets;

import com.jones.matt.lights.HubContext;
import com.jones.matt.lights.controllers.ISystemController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 *
 */
public class HubServlet extends AbstractServlet
{
	private static String kServletPrefix = "Lights/";

	@Override
	public void process(HttpServletRequest theRequest, HttpServletResponse theResponse) throws ServletException, IOException
	{
		String aURI = theRequest.getRequestURI();
		aURI = aURI.substring(aURI.indexOf(kServletPrefix) + kServletPrefix.length());
		String[] anArgs = aURI.split("/");
		if(anArgs.length < 2)
		{
			theResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Malformed input " + anArgs.length);
			return;
		}
		ISystemController aController = HubContext.getInstance().getControllers().get(anArgs[1]);
		if (aController == null)
		{
			theResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "No controller specified");
			return;
		}
		String aJsonResponse = aController.doAction(Arrays.<String>asList(Arrays.copyOfRange(anArgs, 2, anArgs.length)));
		if (aJsonResponse != null)
		{
			theResponse.setContentType("application/json; charset=utf-8");
			theResponse.getWriter().print(aJsonResponse);
		}
		theResponse.setStatus(HttpServletResponse.SC_OK);
	}
}
