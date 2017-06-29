package lights.servlets;

import lights.HubContext;
import lights.controllers.ISystemController;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
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
import java.util.stream.Collectors;

/**
 *
 */
@RestController
@EnableAutoConfiguration
public class HubServlet
{
	private ThreadPoolExecutor myExecutor;

	private static Logger myLogger = Logger.getLogger("com.jones");

	@PostConstruct
	public void init()
	{
		myExecutor = new ThreadPoolExecutor(5, 5, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(5));
	}

	@RequestMapping("/S/**")
	public void process(HttpServletRequest theRequest, HttpServletResponse theResponse) throws ServletException, IOException
	{
		List<String> anArgs = processUrl(theRequest.getRequestURI());
		if(anArgs.size() < 2)
		{
			theResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Malformed input " + anArgs.size());
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

	private static List<String> processUrl(String theUrl)
	{
		return Arrays.stream(theUrl.split("/")).filter(theItem -> theItem != null && !theItem.equalsIgnoreCase("")).collect(Collectors.toList());
	}

	public static List<String> getCommands(String theUrl)
	{
		List<String> aProcessedUrl = processUrl(theUrl);
		aProcessedUrl.remove(0);
		aProcessedUrl.remove(0);
		return aProcessedUrl;
	}

	public static ISystemController getController(String theUrl)
	{
		return HubContext.getInstance().getController(processUrl(theUrl).get(1), ISystemController.class);
	}
}
