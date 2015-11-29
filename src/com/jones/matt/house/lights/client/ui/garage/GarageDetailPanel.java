package com.jones.matt.house.lights.client.ui.garage;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.googlecode.mgwt.dom.client.event.tap.TapEvent;
import com.googlecode.mgwt.dom.client.event.tap.TapHandler;
import com.googlecode.mgwt.ui.client.widget.button.Button;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPanel;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPropertyHelper;
import com.googlecode.mgwt.ui.client.widget.panel.flex.RootFlexPanel;
import com.jones.matt.house.lights.client.HouseLights;
import com.jones.matt.house.lights.client.model.DeviceVO;
import com.jones.matt.house.lights.client.model.RoomVO;
import com.jones.matt.house.lights.client.ui.Header;
import com.jones.matt.house.lights.client.ui.room.DevicePanel;
import com.jones.matt.house.lights.client.utility.DefaultRequestBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Panel containing button to disable auto close, any lights associated with garage
 */
public class GarageDetailPanel extends RootFlexPanel implements ValueChangeHandler<DeviceVO>
{
	private Button myAutoCloseButton;

	private RoomVO myRoom;

	private Map<String, DevicePanel> myDevices = new HashMap<>();

	public GarageDetailPanel()
	{
		setAlignment(FlexPropertyHelper.Alignment.CENTER);
		add(new Header("Garage"));
		FlexPanel aHolder = new FlexPanel();
		aHolder.addStyleName("fillWidth");
		myAutoCloseButton = new Button("Disable Auto Close");
		myAutoCloseButton.setDisabled(true);
		myAutoCloseButton.addStyleName("button-grow");
		myAutoCloseButton.addTapHandler(new TapHandler()
		{
			@Override
			public void onTap(TapEvent event)
			{
				new DefaultRequestBuilder(HouseLights.getBaseUrl() + "S/Garage/DisableAutoClose").send();
			}
		});
		aHolder.add(myAutoCloseButton);
		add(aHolder);
	}

	@Override
	public void onValueChange(ValueChangeEvent<DeviceVO> theEvent)
	{
		if (theEvent.getValue().getName().equals("Garage Opener"))
		{
			myAutoCloseButton.setText("Disable Auto Close" + getAutoCloseString(theEvent.getValue()));
			myAutoCloseButton.setDisabled(!theEvent.getValue().getDoor());
		} else
		{
			if (theEvent.getValue().isLight() && !myDevices.containsKey(theEvent.getValue().getName()))
			{
				myDevices.put(theEvent.getValue().getName(), new DevicePanel(myRoom, theEvent.getValue()));
				add(myDevices.get(theEvent.getValue().getName()));
			}
		}
	}

	public void onRoomUpdate(RoomVO theRoomVO)
	{
		myRoom = theRoomVO;
	}

	/**
	 * Countdown of minutes/seconds until auto close happens
	 *
	 * @param theDeviceVO
	 * @return
	 */
	private String getAutoCloseString(DeviceVO theDeviceVO)
	{
		if (theDeviceVO.getAutoClose() <= 0)
		{
			return "";
		}
		int aSeconds = (theDeviceVO.getAutoClose() /1000 ) % 60;
		int aMinutes=(theDeviceVO.getAutoClose() / (1000 * 60)) % 60;
		int aHours=(theDeviceVO.getAutoClose() / (1000 * 60 * 60)) % 24;
		StringBuilder aStringBuilder = new StringBuilder(" (");
		if (aHours > 0)
		{
			aStringBuilder.append(aHours).append(":");
		}
		return aStringBuilder.toString() + (aMinutes < 10 ? "0" : "") + aMinutes + ":" + (aSeconds < 10 ? "0" : "") + aSeconds + ")";
	}

}
