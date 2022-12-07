package com.bigboxer23.lights.controllers.meural;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 *
 */
public class MeuralCallback implements Callback
{
	private static final Logger logger = LoggerFactory.getLogger(MeuralCallback.class);

	@Override
	public void onFailure(@NotNull Call call, @NotNull IOException e)
	{
		logger.warn("call to " + call.request().url().url() + " failed.", e);
	}

	@Override
	public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException
	{
		if (!response.isSuccessful())
		{
			throw new IOException("call to " + call.request().url().url() + " failed. " + response.body().string());
		}
	}
}
