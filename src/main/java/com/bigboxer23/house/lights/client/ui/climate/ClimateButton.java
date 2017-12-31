package com.bigboxer23.house.lights.client.ui.climate;

import com.bigboxer23.house.lights.client.ui.animation.AnimationStack;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Image;
import com.googlecode.mgwt.ui.client.widget.image.ImageHolder;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPanel;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPropertyHelper;

/**
 * "button" which shows current temp inside/outside + ability to go to detail panel for climate system
 */
public class ClimateButton extends FlexPanel
{
	private ClimateDetailPanel myClimateDetailPanel;

	public ClimateButton()
	{
		addStyleName("relative ClimateButton");
		setOrientation(FlexPropertyHelper.Orientation.HORIZONTAL);
		myClimateDetailPanel = new ClimateDetailPanel();
		add(new TemperaturePanel());
		Image aRoomDetails = new Image(ImageHolder.get().nextItem());
		aRoomDetails.addClickHandler(new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent theEvent)
			{
				AnimationStack.getInstance().forward(myClimateDetailPanel);
			}
		});
		aRoomDetails.addStyleName("RoomDetails");
		add(aRoomDetails);
	}
}
