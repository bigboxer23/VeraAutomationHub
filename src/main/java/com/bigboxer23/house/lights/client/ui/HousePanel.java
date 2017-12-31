package com.bigboxer23.house.lights.client.ui;

import com.bigboxer23.house.lights.client.event.EventBusInstance;
import com.bigboxer23.house.lights.client.event.FireableValueChangeEvent;
import com.bigboxer23.house.lights.client.model.HouseStatus;
import com.bigboxer23.house.lights.client.ui.climate.ClimateButton;
import com.bigboxer23.house.lights.client.ui.garage.GarageDoorButton;
import com.bigboxer23.house.lights.client.ui.room.RoomButton;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPanel;
import com.googlecode.mgwt.ui.client.widget.panel.scroll.ScrollPanel;
import com.bigboxer23.house.lights.client.model.RoomVO;

import java.sql.Date;
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

	private Label myUpdateLabel;

	public HousePanel()
	{
		Header aHeader = new Header("House Lights", false);
		myUpdateLabel = new Label();
		myUpdateLabel.addStyleName("debugLabel");
		aHeader.add(myUpdateLabel);
		add(aHeader);
		myScrollPanel = new ScrollPanel();
		myContent = new FlexPanel();
		myScrollPanel.setWidget(myContent);
		add(myScrollPanel);
		myScrollPanel.setHeight((Window.getClientHeight() - 60) + "px");//- header height
		myScrollPanel.refresh();
		myContent.add(new ClimateButton());
		myContent.add(new GarageDoorButton());
	}

	@Override
	public void onValueChange(ValueChangeEvent<HouseStatus> theEvent)
	{
		myUpdateLabel.setText(DateTimeFormat.getFormat("hh:mm:ss a").format(new Date(System.currentTimeMillis())));
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
		if (theData.shouldDisplay())
		{
			RoomButton aButton = myRoomButtons.get(theData.getName());
			if (aButton == null)
			{
				aButton = new RoomButton(theData);
				myContent.add(aButton);
				myRoomButtons.put(theData.getName(), aButton);
			}
			myScrollPanel.refresh();
		}
		EventBusInstance.getInstance().fireEvent(new FireableValueChangeEvent<>(theData));
	}
}
