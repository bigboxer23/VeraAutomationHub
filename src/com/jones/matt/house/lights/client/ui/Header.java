package com.jones.matt.house.lights.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.googlecode.mgwt.ui.client.widget.image.ImageHolder;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPanel;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPropertyHelper;
import com.jones.matt.house.lights.client.ui.animation.AnimationStack;

/**
 * Header for top of display
 */
public class Header extends FlexPanel
{
	public Header(String theHeaderText, boolean theBackButton)
	{
		addStyleName("header");
		if (theBackButton)
		{
			Image aBack = new Image(ImageHolder.get().previousItem());
			aBack.addStyleName("RoomDetails");
			aBack.addClickHandler(new ClickHandler()
			{
				@Override
				public void onClick(ClickEvent event)
				{
					AnimationStack.getInstance().backward();
				}
			});
			addStyleName("fillWidth");
			setOrientation(FlexPropertyHelper.Orientation.HORIZONTAL);
			setAlignment(FlexPropertyHelper.Alignment.CENTER);
			add(aBack);
		}
		Label aRoomLabel = new Label(theHeaderText);
		aRoomLabel.addStyleName("button-grow");
		add(aRoomLabel);
	}

	public Header(String theHeaderText)
	{
		this(theHeaderText, true);
	}
}
