package com.bigboxer23.lights.controllers;

import com.google.gson.GsonBuilder;
import com.bigboxer23.lights.controllers.vera.VeraHouseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * encapsulate common logging code
 */
public class AbstractBaseController
{
	protected static final Logger myLogger = LoggerFactory.getLogger(AbstractBaseController.class);

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
