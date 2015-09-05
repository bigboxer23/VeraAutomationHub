package com.jones.matt.house.lights.client;

import com.google.gwt.http.client.*;

/**
 * Wrap send exceptions so we don't have to catch everywhere.  Default callback to do nothing
 */
public class DefaultRequestBuilder extends RequestBuilder
{
	public DefaultRequestBuilder(String theUrl)
	{
		super(RequestBuilder.GET, theUrl);
		setCallback(createCallback());
	}

	@Override
	public Request send()
	{
		try
		{
			return super.send();
		}
		catch (RequestException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	protected RequestCallback createCallback()
	{
		return new RequestCallback()
		{
			public void onResponseReceived(Request request, Response response){}

			public void onError(Request request, Throwable exception){}
		};
	}
}