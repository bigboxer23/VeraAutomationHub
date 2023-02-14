package com.bigboxer23.lights.servlets;

import com.bigboxer23.lights.data.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.net.HttpURLConnection;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Servlet has two endpoints used for distributing the token, and enabling access to distribute.
 * Workflow surrounding this looks like an authorized user calls the enableTokenFetch endpoint,
 * which then enables a non-auth'd client to fetch the token without needing to manually enter a
 * username or password (kind of similar to how hue bridge's tap button works to give access to new
 * users). Vision is to create a hue device that can trigger this endpoint via voice allowing
 * registration of new client from local access to home.
 *
 * <p>enableTokenFetch: requires auth, once hit enables hitting the getToken endpoint once within
 * auth.token.window timeframe
 *
 * <p>getToken: allows fetching of token value (and setting secure, httponly cookie) for usage in
 * calling protected APIs. By default the url is not callable until explicitly enabled for a single
 * fetch in a defined time period by the enableTokenFetch
 */
@RestController
@EnableAutoConfiguration
@Tag(
		name = "Token Distribution",
		description = "Service with endpoints to help with distributing tokens to unauthenticated" + " devices")
public class TokenDistributionServlet {
	@Value("${spring.security.user.password}")
	private String myToken;

	private long myTokenValidTime;

	@Value("${auth.token.window}")
	private long kTokenValidTime;

	private static final Logger myLogger = LoggerFactory.getLogger(TokenDistributionServlet.class);

	/** Enable calling the getToken endpoint */
	@GetMapping(value = "/enableTokenFetch", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			summary = "Allows `getToken` endpoint to return the token.",
			description = "Enables an endpoint to distribute the token, but this endpoint itself is"
					+ " protected by the normal authentication.  Essentially allows a"
					+ " device/user that already is trusted to grant access to another"
					+ " unauthenticated device/user")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_UNAUTHORIZED + "", description = "unauthorized"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public void enableTokenRequest(HttpServletRequest theRequest) {
		myTokenValidTime = System.currentTimeMillis();
		myLogger.error("Enabling token access, requested by " + theRequest.getRemoteAddr());
	}

	/**
	 * Set token in cookie, return in JSON value
	 *
	 * @param theResponse
	 * @return
	 */
	@GetMapping(value = "/getToken", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			summary = "Set the token in a cookie and return it",
			description = "Sets the token in a cookie and returns a JSON object with the token.  This"
					+ " servlet will return unauthorized unless the `enableTokenFetch` endpoint"
					+ " is first called.  This endpoint is not protected by needing the token"
					+ " passed with it.")
	@ApiResponses({
		@ApiResponse(
				responseCode = HttpURLConnection.HTTP_UNAUTHORIZED + "",
				description = "unauthorized, if `enableTokenFetch`" + " isn't called first"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public TokenResponse getToken(HttpServletResponse theResponse, HttpServletRequest theRequest) throws IOException {
		if (myTokenValidTime + kTokenValidTime > System.currentTimeMillis()) {
			myLogger.error("Token distributed to " + theRequest.getRemoteAddr());
			Cookie anAuthCookie = new Cookie("t", myToken);
			anAuthCookie.setHttpOnly(true);
			anAuthCookie.setSecure(true);
			anAuthCookie.setMaxAge(60 * 60 * 24 * 365 * 5); // Store cookie for 5 year
			theResponse.addCookie(anAuthCookie); // put cookie in response
			myTokenValidTime = -1;
			return new TokenResponse(myToken);
		}
		theResponse.sendError(HttpURLConnection.HTTP_UNAUTHORIZED);
		return null;
	}
}
