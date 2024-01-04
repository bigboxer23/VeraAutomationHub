package com.bigboxer23.lights.controllers.hue.data;

import lombok.Data;

/** */
@Data
public class HueResource {
	private String id;

	private HueSceneStatus status;

	private boolean auto_dynamic;

	private double speed;

	private HueSceneGroup group;

	private HueMetadata metadata;

	private String type;
}
