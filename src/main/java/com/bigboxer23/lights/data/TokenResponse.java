package com.bigboxer23.lights.data;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 *
 */
@Data
@Schema(description = "JSON to pass back security token to call other services")
public class TokenResponse
{
	@Schema(description = "security token for making requests", required = true)
	private String t;

	public TokenResponse(String token)
	{
		t = token;
	}
}
