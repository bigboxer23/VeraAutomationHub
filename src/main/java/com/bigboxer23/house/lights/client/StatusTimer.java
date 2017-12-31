package com.bigboxer23.house.lights.client;

import com.bigboxer23.house.lights.client.model.HouseStatus;
import com.bigboxer23.house.lights.client.ui.animation.AnimationStack;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Timer;
import com.bigboxer23.house.lights.client.event.FireableValueChangeEvent;
import com.bigboxer23.house.lights.client.ui.HousePanel;
import com.bigboxer23.house.lights.client.utility.DefaultRequestBuilder;

/**
 * timer to fetch new data from the server at specific interval
 */
public class StatusTimer extends Timer
{
	private static final String kLightDataKey = "lightData";

	private Storage myLightData;

	private HousePanel myHousePanel;

	public StatusTimer()
	{
		myLightData = Storage.getLocalStorageIfSupported();
		myHousePanel = new HousePanel();
		AnimationStack.getInstance().forward(myHousePanel);
		generateFromData(getHouseStatus());
		run();
	}

	@Override
	public void run()
	{
		new DefaultRequestBuilder(HouseLights.getBaseUrl() + "SceneStatus")
		{
			protected RequestCallback createCallback()
			{
				return new RequestCallback()
				{
					@Override
					public void onResponseReceived(Request theRequest, Response theResponse)
					{
						HouseStatus aStatus = JsonUtils.safeEval(theResponse.getText());
						if (myLightData != null)
						{
							myLightData.setItem(kLightDataKey, JsonUtils.stringify(aStatus));
						}
						generateFromData(aStatus);
						schedule(HouseLights.getPollingDelay());
					}

					@Override
					public void onError(Request theRequest, Throwable theException)
					{
						schedule(HouseLights.getPollingDelay());
					}
				};
			}
		}.send();
	}

	private void generateFromData(HouseStatus theData)
	{
		if (theData != null)
		{
			myHousePanel.onValueChange(new FireableValueChangeEvent<>(theData));
		}
	}

	private HouseStatus getHouseStatus()
	{
		return myLightData != null ? (HouseStatus)JsonUtils.safeEval(myLightData.getItem(kLightDataKey)) : null;
	}
}
