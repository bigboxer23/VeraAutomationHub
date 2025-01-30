package com.bigboxer23.lights.util;

import com.bigboxer23.utils.http.OkHttpUtil;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import okhttp3.Response;

/** */
public class GsonUtil {
	private static GsonBuilder builder;

	public static <T> T fromJson(String theUrl, Class<T> theClass) throws JsonSyntaxException {
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

	private static GsonBuilder getBuilder() {
		if (builder == null) {
			builder = new GsonBuilder();
		}
		return builder;
	}
}
