package com.bigboxer23.lights.security;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/** Add authentication based on the token stored with the application */
@EnableWebSecurity
@Configuration
public class SecurityConfiguration {
	private static final List<String> protectedUrlStrings = new ArrayList<String>() {
		{
			add("/SceneStatus");
			add("/S/**");
			add("/enableTokenFetch/**");
			add("/SceneStatusSmart");
			add("/swagger-ui/**");
			add("/v3/**");
		}
	};

	private static final RequestMatcher kProtectedUrls = new OrRequestMatcher(protectedUrlStrings.stream()
			.map(AntPathRequestMatcher::new)
			.toList()
			.toArray(new AntPathRequestMatcher[0]));

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, TokenAuthenticationFilter filter) throws Exception {
		http.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
				.and()
				.exceptionHandling()
				.defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(UNAUTHORIZED), kProtectedUrls)
				.and()
				.authenticationProvider(new TokenAuthenticationProvider())
				.addFilterBefore(filter, AnonymousAuthenticationFilter.class)
				.authorizeHttpRequests(auth -> auth.requestMatchers(protectedUrlStrings.toArray(new String[0]))
						.authenticated())
				.authorizeHttpRequests(auth -> auth.requestMatchers("/**").anonymous())
				.formLogin()
				.disable()
				.httpBasic()
				.disable()
				.logout()
				.disable()
				.csrf()
				.csrfTokenRequestHandler(csrfTokenRequestAttributeHandler())
				.csrfTokenRepository(getCSRFRepo());
		return http.build();
	}

	private CookieCsrfTokenRepository getCSRFRepo() {
		CookieCsrfTokenRepository csrfRepo = CookieCsrfTokenRepository.withHttpOnlyFalse();
		csrfRepo.setSecure(true);
		csrfRepo.setCookieMaxAge(60 * 60); // Store cookie for 1 min
		return csrfRepo;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	TokenAuthenticationFilter restAuthenticationFilter(AuthenticationManager auth) {
		return new TokenAuthenticationFilter(kProtectedUrls, auth, successHandler());
	}

	@Bean
	SimpleUrlAuthenticationSuccessHandler successHandler() {
		final SimpleUrlAuthenticationSuccessHandler aSuccessHandler = new SimpleUrlAuthenticationSuccessHandler();
		aSuccessHandler.setRedirectStrategy((theRequest, theResponse, theUrl) -> {});
		return aSuccessHandler;
	}

	@Bean
	CsrfTokenRequestAttributeHandler csrfTokenRequestAttributeHandler() {
		CsrfTokenRequestAttributeHandler handler = new CsrfTokenRequestAttributeHandler();
		handler.setCsrfRequestAttributeName(null);
		return handler;
	}
}
