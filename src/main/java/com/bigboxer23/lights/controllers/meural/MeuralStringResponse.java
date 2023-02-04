package com.bigboxer23.lights.controllers.meural;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 *
 */
@Data
public class MeuralStringResponse
{
	@Schema(description = "String response from the Meural service", required = true, example = "A prompt to generate an image")
	private String response;
}
