package com.bigboxer23.lights.controllers.aggregate;

import java.util.List;
import lombok.Data;

/** */
@Data
public class EnvironmentCluster {
	private int low;

	private int high;

	private String switchId;

	private List<String> environmentDevices;

	private boolean isDehumidifier = false;
}
