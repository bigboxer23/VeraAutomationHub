package com.jones.matt.house.lights.client;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.storage.client.Storage;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Image;
import com.googlecode.mgwt.ui.client.widget.image.ImageHolder;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPanel;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPropertyHelper;
import com.googlecode.mgwt.ui.client.widget.panel.flex.RootFlexPanel;
import com.jones.matt.house.lights.client.animation.AnimationStack;
import com.jones.matt.house.lights.client.event.FireableValueChangeEvent;
import com.jones.matt.house.lights.client.model.HouseStatus;
import com.jones.matt.house.lights.client.model.RoomVO;
import com.jones.matt.house.lights.client.room.RoomPanel;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class StatusTimer extends Timer
{
	private static final String kLightDataKey = "lightData";

	private Storage myLightData;

	private long myTime = -1;

	private FlexPanel myContent;

	private Map<String, RoomButton> myRoomButtons = new HashMap<>();

	public StatusTimer(FlexPanel theContent)
	{
		myLightData = Storage.getLocalStorageIfSupported();
		myContent = theContent;
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
						myLightData.setItem(kLightDataKey, JsonUtils.stringify(aStatus));
						generateFromData(getHouseStatus());
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
			for (int ai = 0; ai < theData.getRooms().length(); ai++)
			{
				generateRow(theData.getRooms().get(ai));
			}
		}
	}
	/**
	 * Generate a single row of controls
	 *
	 * @param theLabel label for the row
	 * @param theLabel1 label for the first button
	 * @param theTapHandler1 tap handler for the first button
	 * @param theLabel2 label for the second button
	 * @param theTapHandler2 tap handler for the second button
	 */
	private void generateRow(final RoomVO theData)
	{
		RoomButton aButton = myRoomButtons.get(theData.getName());
		if (aButton == null)
		{
			aButton = new RoomButton(theData);
			myContent.add(aButton);
			myRoomButtons.put(theData.getName(), aButton);
		}
		EventBusInstance.getInstance().fireEvent(new FireableValueChangeEvent<>(theData));
	}

	private HouseStatus getHouseStatus()
	{
		return JsonUtils.safeEval(myLightData.getItem(kLightDataKey));
	}
}
