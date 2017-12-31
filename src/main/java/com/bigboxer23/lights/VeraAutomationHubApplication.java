package com.bigboxer23.lights;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 *
 */
@SpringBootApplication
@EnableScheduling
public class VeraAutomationHubApplication
{
	public static void main(String[] args)
	{
		SpringApplication.run(VeraAutomationHubApplication.class, args);
	}

	public VeraAutomationHubApplication()
	{

	}
}
