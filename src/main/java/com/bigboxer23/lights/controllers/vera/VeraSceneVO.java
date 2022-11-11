package com.bigboxer23.lights.controllers.vera;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 *
 */
@Data
@Schema(description = "JSON representing a scene")
public class VeraSceneVO
{
	@Schema(description = "name of the scene")
	private String name;

	@Schema(description = "id of the scene")
	private int id;

	@Schema(description = "is the scene active")
	private int active;

	@Schema(description = "id of a room associated with the scene")
	private Integer room;
}
