package com.bigboxer23.lights.servlets;

import com.bigboxer23.lights.HubContext;
import com.bigboxer23.lights.controllers.ISystemController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 */
@RestController
@EnableAutoConfiguration
public class HubServlet extends HubContext
{
	private ThreadPoolExecutor myExecutor;

	private static final Logger myLogger = LoggerFactory.getLogger(HubServlet.class);

	private ThreadPoolExecutor getExecutors()
	{
		if (myExecutor == null)
		{
			myExecutor = new ThreadPoolExecutor(5, 5, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(5));
		}
		return myExecutor;
	}

	@RequestMapping(value = "/S/**", produces = {MediaType.APPLICATION_JSON_VALUE})
	public void process(HttpServletRequest theRequest)
	{
		List<String> anArgs = processUrl(theRequest.getRequestURI());
		if(anArgs.size() < 2)
		{
			throw new RuntimeException("Malformed input " + anArgs.size());
		}
		ISystemController aController = getControllers().get(anArgs.get(1));
		if (aController == null)
		{
			throw new RuntimeException("No controller specified");
		}
		String aRequestURL = theRequest.getRequestURI();
		List<String> aCommands = getCommands(aRequestURL);
		getExecutors().execute(() ->
		{
			String aJsonResponse = aController.doAction(aCommands);
			if (aJsonResponse != null)
			{
				myLogger.error("Error running request: " + aRequestURL);
				myLogger.error("Message: " + aJsonResponse);
			}
		});
	}

	private static List<String> processUrl(String theUrl)
	{
		return Arrays.stream(theUrl.split("/")).filter(theS -> !theS.isEmpty()).collect(Collectors.toList());
	}

	private static List<String> getCommands(String theUrl)
	{
		List<String> aProcessedUrl = processUrl(theUrl);
		return aProcessedUrl.subList(2, aProcessedUrl.size());
	}
}
