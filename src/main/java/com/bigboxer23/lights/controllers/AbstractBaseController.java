package com.bigboxer23.lights.controllers;

import com.google.gson.GsonBuilder;
import com.bigboxer23.lights.controllers.vera.VeraHouseVO;

import java.util.logging.Logger;

/**
 * encapsulate common logging code
 */
public class AbstractBaseController
{
	protected static Logger myLogger = Logger.getLogger("com.bigboxer23");

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
}
