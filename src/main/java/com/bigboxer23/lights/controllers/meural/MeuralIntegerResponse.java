package com.bigboxer23.lights.controllers.meural;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** */
@Data
public class MeuralIntegerResponse {
	@Schema(description = "Integer response from the Meural service", required = true, example = "0")
	private int response;
}
