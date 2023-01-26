package com.bigboxer23.lights.security;

import static java.util.Optional.ofNullable;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/** */
@Component
public class TokenAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {
	@Value("${spring.security.user.password}")
	private String myToken;

	@Override
	protected void additionalAuthenticationChecks(
			UserDetails theUserDetails, UsernamePasswordAuthenticationToken theAuth) {
		// Nothing to do
	}

	@Override
	protected UserDetails retrieveUser(
			final String theUsername, final UsernamePasswordAuthenticationToken theAuthentication) {
		return ofNullable(theAuthentication.getCredentials())
				.map(String::valueOf)
				.flatMap(this::findByToken)
				.orElseThrow(() -> new UsernameNotFoundException(
						"Cannot find user with token:" + theAuthentication.getCredentials()));
	}

	public Optional<User> findByToken(String theToken) {
		if (theToken == null || !theToken.equalsIgnoreCase(myToken)) {
			return Optional.empty();
		}
		return Optional.of(new User(theToken));
	}
}
