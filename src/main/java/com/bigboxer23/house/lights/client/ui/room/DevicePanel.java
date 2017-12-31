package com.bigboxer23.house.lights.client.ui.room;

import com.bigboxer23.house.lights.client.event.EventBusInstance;
import com.bigboxer23.house.lights.client.model.DeviceVO;
import com.bigboxer23.house.lights.client.model.RoomVO;
import com.bigboxer23.house.lights.client.ui.animation.AnimationStack;
import com.bigboxer23.house.lights.client.utility.DefaultRequestBuilder;
import com.bigboxer23.house.lights.client.utility.VeraUrlUtility;
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

/**
 *
 */
public class DevicePanel extends FlexPanel implements ValueChangeHandler<RoomVO>
{
	private RoomVO myRoomVO;

	private DeviceVO myDeviceVO;

	private Button myButton;

	/**
	 * We set status immediately of the button for light on/off.  Status can come back
	 * before it's done however and cause a toggle of state display, which doesn't hurt anything
	 * but is confusing to the user.  Don't pay attention to the initial status coming back (as it should match
	 * or last button press) to eliminate the toggle
	 */
	private boolean mySetStatusInProgress = false;

	public DevicePanel(RoomVO theRoomVO, DeviceVO theDeviceVO)
	{
		myButton = new Button(theDeviceVO.getName());
		myButton.addStyleName("button-grow");
		myButton.addTapHandler(new TapHandler()
		{
			@Override
			public void onTap(TapEvent event)
			{
				myButton.setImportant(!myDeviceVO.isOn());
				mySetStatusInProgress = true;
				new DefaultRequestBuilder(VeraUrlUtility.getOnOffUrl(!myDeviceVO.isOn(), myDeviceVO.getID())).send();
			}
		});
		setOrientation(FlexPropertyHelper.Orientation.HORIZONTAL);
		add(myButton);
		Image aRoomDetails = new Image(ImageHolder.get().nextItem());
		aRoomDetails.addClickHandler(new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event)
			{
				AnimationStack.getInstance().forward(new DeviceDimPanel(myDeviceVO));
			}
		});
		aRoomDetails.addStyleName("RoomDetails");
		add(aRoomDetails);
		addStyleName("fillWidth");
		setData(theRoomVO, theDeviceVO);
		EventBusInstance.getInstance().addValueChangeHandler(this);
	}

	private void setData(RoomVO theRoomVO, DeviceVO theDeviceVO)
	{
		myRoomVO = theRoomVO;
		myDeviceVO = theDeviceVO;
		if (!mySetStatusInProgress)
		{
			myButton.setImportant(myDeviceVO.isOn());
		}
		mySetStatusInProgress = false;
	}

	@Override
	public void onValueChange(ValueChangeEvent<RoomVO> theEvent)
	{
		if (myRoomVO.getID() == theEvent.getValue().getID())
		{
			for (DeviceVO aDevice : theEvent.getValue().getDevices())
			{
				if (aDevice.getID() == myDeviceVO.getID())
				{
					setData(theEvent.getValue(), aDevice);
					return;
				}
			}
		}
	}
}
