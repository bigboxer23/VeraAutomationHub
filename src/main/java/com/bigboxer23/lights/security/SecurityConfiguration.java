package com.bigboxer23.lights.security;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
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
		CookieCsrfTokenRepository csrfRepo = CookieCsrfTokenRepository.withHttpOnlyFalse();
		csrfRepo.setSecure(true);
		csrfRepo.setCookieMaxAge(60 * 60); // Store cookie for 1 min
		http.sessionManagement()
				.sessionCreationPolicy(STATELESS)
				.and()
				.csrf()
				.csrfTokenRepository(csrfRepo).
				and()
				.exceptionHandling()
				.defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(UNAUTHORIZED), kProtectedUrls)
				.and()
				.formLogin()
				.disable()
				.httpBasic()
				.disable()
				.logout()
				.disable()
				/*.csrf()
				.csrfTokenRepository(csrfRepo)*/
				//.and()
				.authenticationProvider(new TokenAuthenticationProvider())
				//.addFilterBefore(restAuthenticationFilter(http), AnonymousAuthenticationFilter.class)
				.authorizeHttpRequests(auth ->
				{
					auth.requestMatchers(protectedUrlStrings.toArray(new String[0]))
							.authenticated();
				})
				.addFilterBefore(filter, AnonymousAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(
			AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	TokenAuthenticationFilter restAuthenticationFilter(HttpSecurity http, AuthenticationManager auth) throws Exception {
		return new TokenAuthenticationFilter(kProtectedUrls, auth, successHandler());
	}

	@Bean
	SimpleUrlAuthenticationSuccessHandler successHandler() {
		final SimpleUrlAuthenticationSuccessHandler aSuccessHandler = new SimpleUrlAuthenticationSuccessHandler();
		aSuccessHandler.setRedirectStrategy((theRequest, theResponse, theUrl) -> {});
		return aSuccessHandler;
	}
}
