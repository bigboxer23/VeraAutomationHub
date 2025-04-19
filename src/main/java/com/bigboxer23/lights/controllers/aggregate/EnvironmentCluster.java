package com.bigboxer23.lights.controllers.aggregate;

import java.util.List;
import lombok.Data;

/** */
@Data
public class EnvironmentCluster {
	private int range = 2;

	private int target;

	private String switchId;

	private List<String> environmentDevices;

	private boolean isDehumidifier = false;

	public double getHigh() {
		return target + range;
	}

	public double getLow() {
		return target - range;
	}
}
