package com.bigboxer23.lights.controllers;

import com.bigboxer23.util.http.HttpClientUtils;
import com.google.gson.GsonBuilder;
import com.bigboxer23.lights.controllers.vera.VeraHouseVO;
import com.google.gson.JsonSyntaxException;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * encapsulate common logging code
 */
public class AbstractBaseController
{
	protected static final Logger myLogger = LoggerFactory.getLogger(AbstractBaseController.class);

	private GsonBuilder myBuilder;

	protected <T> T fromJson(String theUrl, Class<T> theClass) throws JsonSyntaxException
	{
		return getBuilder().create().fromJson(HttpClientUtils.execute(new HttpGet(theUrl)), theClass);
	}

	private GsonBuilder getBuilder()
	{
		if (myBuilder == null)
		{
			myBuilder = new GsonBuilder();
			myBuilder.registerTypeAdapter(VeraHouseVO.class, new VeraHouseVO());
		}
		return myBuilder;
	}
}
