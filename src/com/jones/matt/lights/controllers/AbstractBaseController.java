package com.jones.matt.lights.controllers;

import com.google.gson.GsonBuilder;
import com.jones.matt.lights.TimeoutEnabledHttpClient;
import com.jones.matt.lights.controllers.vera.VeraHouseVO;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.logging.Logger;

/**
 * encapsulate common logging code
 */
public class AbstractBaseController
{
	protected static Logger myLogger = Logger.getLogger("com.jones");

	private DefaultHttpClient myHttpClient;

	private GsonBuilder myBuilder;

	protected GsonBuilder getBuilder()
	{
		if (myBuilder == null)
		{
			myBuilder = new GsonBuilder();
			myBuilder.registerTypeAdapter(VeraHouseVO.class, new VeraHouseVO());
		}
		return myBuilder;
	}

	protected DefaultHttpClient getHttpClient()
	{
		if (myHttpClient == null)
		{
			myHttpClient = new TimeoutEnabledHttpClient();
		}
		return myHttpClient;
	}
}
