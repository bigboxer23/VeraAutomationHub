package lights;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 *
 */
@SpringBootApplication
@EnableScheduling
public class AutomationHubApplication
{
	public static void main(String[] args)
	{
		SpringApplication.run(AutomationHubApplication.class, args);
	}

}
