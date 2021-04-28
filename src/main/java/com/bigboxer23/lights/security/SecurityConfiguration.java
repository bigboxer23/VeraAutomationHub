package com.bigboxer23.lights.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

/**
 * Add authentication based on the token stored with the application
 */
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter
{
	private static List<String> kProtectedUrlStrings = new ArrayList<String>()
	{{
		add("/SceneStatus");
		add("/S/**");
		add("/enableTokenFetch/**");
		add("/SceneStatusSmart");
	}};

	private static final RequestMatcher kProtectedUrls = new OrRequestMatcher(kProtectedUrlStrings.
			stream().
			map(AntPathRequestMatcher::new).
			collect(Collectors.toList()).
			toArray(new AntPathRequestMatcher[0]));

	private TokenAuthenticationProvider myProvider;

	@Autowired
	public void setTokenAuthenticationProvider(TokenAuthenticationProvider theProvider)
	{
		myProvider = theProvider;
	}

	@Override
	protected void configure(final AuthenticationManagerBuilder auth) {
		auth.authenticationProvider(myProvider);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.sessionManagement()
				.sessionCreationPolicy(STATELESS)
				.and()
				.exceptionHandling()
				.defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(UNAUTHORIZED), kProtectedUrls)
				.and()
				.authenticationProvider(myProvider)
				.addFilterBefore(restAuthenticationFilter(), AnonymousAuthenticationFilter.class)
				.authorizeRequests()
				.antMatchers(kProtectedUrlStrings.toArray(new String[0]))
				.authenticated()
				.and()
				.formLogin().disable()
				.httpBasic().disable()
				.logout().disable();
	}

	@Bean
	TokenAuthenticationFilter restAuthenticationFilter() throws Exception {
		final TokenAuthenticationFilter aTokenAuthenticationFilter = new TokenAuthenticationFilter(kProtectedUrls);
		aTokenAuthenticationFilter.setAuthenticationManager(authenticationManager());
		aTokenAuthenticationFilter.setAuthenticationSuccessHandler(successHandler());
		return aTokenAuthenticationFilter;
	}

	@Bean
	SimpleUrlAuthenticationSuccessHandler successHandler() {
		final SimpleUrlAuthenticationSuccessHandler aSuccessHandler = new SimpleUrlAuthenticationSuccessHandler();
		aSuccessHandler.setRedirectStrategy((theRequest, theResponse, theUrl) -> { });
		return aSuccessHandler;
	}
}
