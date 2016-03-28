package com.jones.matt.lights.servlets;

import com.jones.matt.lights.HubContext;
import com.jones.matt.lights.controllers.ISystemController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class HubServlet extends AbstractServlet
{
	private static String kServletPrefix = "Lights/";

	@Override
	public void process(HttpServletRequest theRequest, HttpServletResponse theResponse) throws ServletException, IOException
	{
		String[] anArgs = processUrl(theRequest.getRequestURI());
		if(anArgs.length < 2)
		{
			theResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Malformed input " + anArgs.length);
			return;
		}
		ISystemController aController = getController(theRequest.getRequestURI());
		if (aController == null)
		{
			theResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "No controller specified");
			return;
		}
		String aJsonResponse = aController.doAction(getCommands(theRequest.getRequestURI()));
		if (aJsonResponse != null)
		{
			theResponse.setContentType("application/json; charset=utf-8");
			theResponse.getWriter().print(aJsonResponse);
		}
		theResponse.setStatus(HttpServletResponse.SC_OK);
	}

	public static String[] processUrl(String theUrl)
	{
		theUrl = theUrl.substring(theUrl.indexOf(kServletPrefix) + kServletPrefix.length());
		return theUrl.split("/");
	}

	public static List<String> getCommands(String theUrl)
	{
		String[] aProcessedUrl = processUrl(theUrl);
		return Arrays.asList(Arrays.copyOfRange(aProcessedUrl, 2, aProcessedUrl.length));
	}

	public static ISystemController getController(String theUrl)
	{
		return HubContext.getInstance().getController(processUrl(theUrl)[1], ISystemController.class);
	}
}
