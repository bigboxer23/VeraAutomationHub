package com.bigboxer23.lights.controllers.hue;

import static org.junit.jupiter.api.Assertions.assertFalse;

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
	public void getZones() {
		assertFalse(component.getZones().isEmpty());
	}

	@Test
	public void getScenes() {
		assertFalse(component.getScenes().isEmpty());
	}
}
