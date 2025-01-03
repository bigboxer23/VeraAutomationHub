package com.bigboxer23.lights.controllers.govee;

import lombok.Data;

/** */
@Data
public class HumidifierCluster {
	private String pump;

	private String humidifier;

	private String humidifierModel;

	private String outlet;

	private String humiditySensor;

	private int lowHumidityPoint = 60;
}
