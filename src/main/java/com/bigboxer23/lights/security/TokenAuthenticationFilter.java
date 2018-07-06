package com.bigboxer23.lights.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.google.common.net.HttpHeaders.AUTHORIZATION;
import static java.util.Optional.ofNullable;

/**
 * Filter to get the token from the request
 */
public class TokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter
{
	public TokenAuthenticationFilter(final RequestMatcher requiresAuth)
	{
		super(requiresAuth);
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest theRequest, HttpServletResponse theResponse)
	{
		String aTokenParam = ofNullable(theRequest.getHeader(AUTHORIZATION))
				.orElse(theRequest.getParameter("t"));
		//TODO handle getting from cookie
		String aToken = ofNullable(aTokenParam)
				.map(String::trim)
				.orElseThrow(() -> new BadCredentialsException("Missing Token"));
		return getAuthenticationManager().authenticate(new UsernamePasswordAuthenticationToken(aToken, aToken));
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest theRequest, HttpServletResponse theResponse, FilterChain theChain, Authentication theAuthResult) throws IOException, ServletException
	{
		super.successfulAuthentication(theRequest, theResponse, theChain, theAuthResult);
		theChain.doFilter(theRequest, theResponse);
	}
}