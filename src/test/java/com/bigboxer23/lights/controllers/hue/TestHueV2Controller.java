package com.bigboxer23.lights.controllers.hue;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.bigboxer23.lights.controllers.hue.data.HueAPIResponse;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class TestHueV2Controller {

	@Autowired
	private HueV2Controller component;

	@Test
	public void getZones() throws IOException
	{
		HueAPIResponse response = component.getZones();
		assertNotNull(response);
	}
	@Test
	public void getScenes() throws IOException {
		HueAPIResponse response = component.getScenes();
		assertNotNull(response);
	}
}
