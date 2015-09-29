package com.jones.matt.house.lights.client.ui;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPanel;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.ScrollPanel;
import com.jones.matt.house.lights.client.event.EventBusInstance;
import com.jones.matt.house.lights.client.event.FireableValueChangeEvent;
import com.jones.matt.house.lights.client.model.HouseStatus;
import com.jones.matt.house.lights.client.model.RoomVO;
import com.jones.matt.house.lights.client.ui.garage.GarageDoorButton;
import com.jones.matt.house.lights.client.ui.room.RoomButton;

import java.util.HashMap;
import java.util.Map;

/**
 * panel for showing global house status
 */
public class HousePanel extends FlexPanel implements ValueChangeHandler<HouseStatus>
{
	private Map<String, RoomButton> myRoomButtons = new HashMap<>();

	private FlexPanel myContent;
	private ScrollPanel myScrollPanel;

	public HousePanel()
	{
		add(new Header("House Lights", false));
		myScrollPanel = new ScrollPanel();
		myContent = new FlexPanel();
		myScrollPanel.setWidget(myContent);
		add(myScrollPanel);
		myScrollPanel.setHeight((Window.getClientHeight() - 60) + "px");//- header height
		myScrollPanel.refresh();
		myContent.add(new GarageDoorButton());
	}

	@Override
	public void onValueChange(ValueChangeEvent<HouseStatus> theEvent)
	{
		if (theEvent.getValue() != null)
		{
			for (int ai = 0; ai < theEvent.getValue().getRooms().length(); ai++)
			{
				generateRow(theEvent.getValue().getRooms().get(ai));
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
		myScrollPanel.refresh();
		EventBusInstance.getInstance().fireEvent(new FireableValueChangeEvent<>(theData));
	}
}
