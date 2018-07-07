package com.bigboxer23.lights;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

/**
 *
 */
public class TimeoutEnabledHttpClient extends DefaultHttpClient
{
	public TimeoutEnabledHttpClient()
	{
		super(getDefaultParams());
	}

	private static HttpParams getDefaultParams()
	{
		HttpParams aParams = new BasicHttpParams();
		HttpConnectionParams.setSoTimeout(aParams, 5000);
		HttpConnectionParams.setConnectionTimeout(aParams, 5000);
		return aParams;
	}
}