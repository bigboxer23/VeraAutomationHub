package com.bigboxer23.house.lights.client.ui;

import com.bigboxer23.house.lights.client.ui.animation.AnimationStack;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.googlecode.mgwt.ui.client.widget.image.ImageHolder;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPanel;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPropertyHelper;

/**
 * Header for top of display
 */
public class Header extends FlexPanel
{
	public Header(String theHeaderText, boolean theBackButton)
	{
		addStyleName("header fillWidth");
		setOrientation(FlexPropertyHelper.Orientation.HORIZONTAL);
		setAlignment(FlexPropertyHelper.Alignment.CENTER);
		Image aBack = new Image(ImageHolder.get().previousItem());
		aBack.addStyleName("RoomDetails");
		aBack.getElement().getStyle().setOpacity(0);
		if (theBackButton)
		{
			aBack.addClickHandler(new ClickHandler()
			{
				@Override
				public void onClick(ClickEvent event)
				{
					AnimationStack.getInstance().backward();
				}
			});
			aBack.getElement().getStyle().clearOpacity();
		}
		add(aBack);
		Label aRoomLabel = new Label(theHeaderText);
		aRoomLabel.addStyleName("button-grow");
		add(aRoomLabel);
		aBack = new Image(ImageHolder.get().nextItem());
		aBack.addStyleName("RoomDetails");
		aBack.getElement().getStyle().setOpacity(0);
		add(aBack);
	}

	public Header(String theHeaderText)
	{
		this(theHeaderText, true);
	}
}
