package com.bigboxer23.lights.controllers;

import com.bigboxer23.utils.http.OkHttpUtil;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** encapsulate common logging code */
public class AbstractBaseController {
	protected static final Logger myLogger = LoggerFactory.getLogger(AbstractBaseController.class);

	private GsonBuilder myBuilder;

	protected <T> T fromJson(String theUrl, Class<T> theClass) throws JsonSyntaxException {
		try (Response response = OkHttpUtil.getSynchronous(theUrl, null)) {
			String body = response.body().string();
			if (!response.isSuccessful()) {
				throw new IOException("call to " + theUrl + " failed. " + body);
			}
			return getBuilder().create().fromJson(body, theClass);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private GsonBuilder getBuilder() {
		if (myBuilder == null) {
			myBuilder = new GsonBuilder();
		}
		return myBuilder;
	}
}
