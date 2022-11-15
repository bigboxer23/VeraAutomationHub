package com.bigboxer23.lights;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 *
 */
@SpringBootApplication
@EnableScheduling
@OpenAPIDefinition(info =
@Info(title = "Automation Hub", version = "1", description = "Application to stitch together various home automation technologies",
		contact = @Contact(name = "bigboxer23@gmail.com", url="https://github.com/bigboxer23/VeraAutomationHub"))
)
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
