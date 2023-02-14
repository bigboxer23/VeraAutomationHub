package com.bigboxer23.lights.security;

import static java.util.Optional.ofNullable;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;

/** Filter to get the token from the request */
public class TokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
	public TokenAuthenticationFilter(
			final RequestMatcher requiresAuth, AuthenticationManager manager, AuthenticationSuccessHandler handler) {
		super(requiresAuth);
		setAuthenticationManager(manager);
		setAuthenticationSuccessHandler(handler);
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest theRequest, HttpServletResponse theResponse) {
		String aToken = ofNullable(theRequest.getParameter("t")).orElse(getTokenFromCookie(theRequest));
		if (aToken == null) {
			throw new BadCredentialsException("Missing Token");
		}
		return getAuthenticationManager()
				.authenticate(new UsernamePasswordAuthenticationToken(aToken.trim(), aToken.trim()));
	}

	@Override
	protected void successfulAuthentication(
			HttpServletRequest theRequest,
			HttpServletResponse theResponse,
			FilterChain theChain,
			Authentication theAuthResult)
			throws IOException, ServletException {
		super.successfulAuthentication(theRequest, theResponse, theChain, theAuthResult);
		theChain.doFilter(theRequest, theResponse);
	}

	private String getTokenFromCookie(HttpServletRequest theRequest) {
		if (theRequest.getCookies() == null) {
			return null;
		}
		return Arrays.stream(theRequest.getCookies())
				.filter(theCookie -> theCookie.getName().equalsIgnoreCase("t"))
				.map(Cookie::getValue)
				.findAny()
				.orElse(null);
	}
}
