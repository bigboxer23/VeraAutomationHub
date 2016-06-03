package com.jones.matt.house.lights.client.ui.room;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Image;
import com.googlecode.mgwt.dom.client.event.tap.TapEvent;
import com.googlecode.mgwt.dom.client.event.tap.TapHandler;
import com.googlecode.mgwt.ui.client.widget.button.Button;
import com.googlecode.mgwt.ui.client.widget.image.ImageHolder;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPanel;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPropertyHelper;
import com.jones.matt.house.lights.client.utility.DefaultRequestBuilder;
import com.jones.matt.house.lights.client.event.EventBusInstance;
import com.jones.matt.house.lights.client.ui.animation.AnimationStack;
import com.jones.matt.house.lights.client.model.RoomVO;
import com.jones.matt.house.lights.client.utility.VeraUrlUtility;

/**
 *
 */
public class RoomButton extends FlexPanel implements ValueChangeHandler<RoomVO>
{
	private RoomVO myData;

	private Button myButton;

	private boolean mySetStatusInProgress = false;

	public RoomButton(RoomVO theData)
	{
		myButton = new Button(theData.getName());
		myButton.addStyleName("button-grow");
		if (theData.hasLights())
		{
			myButton.addTapHandler(new TapHandler()
			{
				@Override
				public void onTap(TapEvent event)
				{
					myButton.setImportant(!myButton.isImportant());
					mySetStatusInProgress = true;
					new DefaultRequestBuilder(VeraUrlUtility.getRoomOnOffUrl(!myData.isOn(), myData.getID())).send();
				}
			});
		}
		setData(theData);
		//myButton.setDisabled(!myData.hasLights());
		setOrientation(FlexPropertyHelper.Orientation.HORIZONTAL);
		add(myButton);
		Image aRoomDetails = new Image(ImageHolder.get().nextItem());
		aRoomDetails.addClickHandler(new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event)
			{
				AnimationStack.getInstance().forward(new RoomPanel(myData));
			}
		});
		aRoomDetails.addStyleName("RoomDetails");
		add(aRoomDetails);
		if (myData.isScenesList())
		{
			aRoomDetails.addStyleName("transparent");
			myButton.addTapHandler(new TapHandler()
			{
				@Override
				public void onTap(TapEvent event)
				{
					AnimationStack.getInstance().forward(new RoomPanel(myData));
				}
			});
		}
		EventBusInstance.getInstance().addValueChangeHandler(this);
	}

	public void setData(RoomVO theData)
	{
		myData = theData;
		if (!mySetStatusInProgress)
		{
			myButton.setImportant(myData.isOn());
		}
		mySetStatusInProgress = false;
	}

	@Override
	public void onValueChange(ValueChangeEvent<RoomVO> theEvent)
	{
		if (theEvent.getValue().getID() == myData.getID())
		{
			setData(theEvent.getValue());
		}
	}
}
