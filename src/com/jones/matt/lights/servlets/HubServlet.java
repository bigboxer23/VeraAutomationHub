package com.jones.matt.lights.servlets;

import com.jones.matt.lights.HubContext;
import com.jones.matt.lights.controllers.ISystemController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 *
 */
public class HubServlet extends AbstractServlet
{
	private static String kServletPrefix = "Lights/";

	private ThreadPoolExecutor myExecutor;

	private static Logger myLogger = Logger.getLogger("com.jones");

	@Override
	public void init() throws ServletException
	{
		myExecutor = new ThreadPoolExecutor(5,
				5,
				1,
				TimeUnit.SECONDS,
				new ArrayBlockingQueue<>(5));
	}

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
		List<String> aCommands = getCommands(theRequest.getRequestURI());
		myExecutor.execute(() ->
		{
			String aJsonResponse = aController.doAction(aCommands);
			if (aJsonResponse != null)
			{
				myLogger.warning("Error running request: " + theRequest.getRequestURI());
				myLogger.warning("Message: " + aJsonResponse);
			}
		});
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
