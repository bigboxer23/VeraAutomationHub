package com.bigboxer23.lights.controllers.meural;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** */
@Data
public class MeuralResponse {
	@Schema(description = "Boolean response from the Meural service", required = true, example = "true")
	private Boolean response;
}
