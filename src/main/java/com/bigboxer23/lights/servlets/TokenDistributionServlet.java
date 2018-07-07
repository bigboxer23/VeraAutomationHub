package com.bigboxer23.lights.servlets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Servlet has two endpoints used for distributing the token, and enabling access to distribute.  Workflow surrounding
 * this looks like an authorized user calls the enableTokenFetch endpoint, which then enables a non-auth'd client to
 * fetch the token without needing to manually enter a username or password (kind of similar to how hue bridge's tap
 * button works to give access to new users).  Vision is to create a hue device that can trigger this endpoint via voice
 * allowing registration of new client from local access to home.
 *
 * enableTokenFetch:
 * requires auth, once hit enables hitting the getToken endpoint once within auth.token.window timeframe
 *
 * getToken:
 * allows fetching of token value (and setting secure, httponly cookie) for usage in calling protected APIs.  By default
 * the url is not callable until explicitly enabled for a single fetch in a defined time period by the enableTokenFetch
 */
@RestController
@EnableAutoConfiguration
public class TokenDistributionServlet
{
	@Value("${spring.security.user.password}")
	private String myToken;

	private long myTokenValidTime;

	@Value("${auth.token.window}")
	private long kTokenValidTime;

	private static Logger myLogger = Logger.getLogger("com.bigboxer23");

	/**
	 * Enable calling the getToken endpoint
	 */
	@RequestMapping(value = "/enableTokenFetch")
	public void enableTokenRequest(HttpServletRequest theRequest)
	{
		myTokenValidTime  = System.currentTimeMillis();
		myLogger.warning("Enabling token access, requested by " + theRequest.getRemoteAddr());
	}

	/**
	 * Set token in cookie, return in JSON value
	 *
	 * @param theResponse
	 * @return
	 */
	@RequestMapping(value = "/getToken", produces = {MediaType.APPLICATION_JSON_VALUE})
	public String getToken(HttpServletResponse theResponse, HttpServletRequest theRequest) throws IOException
	{
		if (myTokenValidTime + kTokenValidTime > System.currentTimeMillis())
		{
			myLogger.warning("Token distributed to " + theRequest.getRemoteAddr());
			Cookie anAuthCookie = new Cookie("t", myToken);
			anAuthCookie.setHttpOnly(true);
			anAuthCookie.setSecure(true);
			theResponse.addCookie(anAuthCookie); //put cookie in response
			myTokenValidTime = -1;
			return "{t:" + myToken + "}";
		}
		theResponse.sendError(401);
		return null;
	}
}
