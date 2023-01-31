package com.bigboxer23.lights.controllers.meural;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** */
@Data
public class MeuralResponse {
	@Schema(
			description = "Gives more specific reason request is successful or fails",
			required = true,
			example = "Unsupported file format uploaded")
	private Boolean response;
}
