package com.jones.matt.house.lights.client.animation;

import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.mgwt.ui.client.widget.animation.AnimationEndCallback;
import com.googlecode.mgwt.ui.client.widget.animation.AnimationWidget;
import com.googlecode.mgwt.ui.client.widget.animation.Animations;

import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class AnimationStack implements AnimationEndCallback
{
	private static AnimationStack myInstance;

	private AnimationWidget myAnimationWidget;

	private List<Widget> myWidgets;

	private boolean myFirstWidget = true;

	private AnimationStack()
	{
		myAnimationWidget = new AnimationWidget();
		myWidgets = new LinkedList<>();
		RootPanel.get().add(myAnimationWidget);
	}

	public static AnimationStack getInstance()
	{
		if (myInstance == null)
		{
			myInstance = new AnimationStack();
		}
		return myInstance;
	}

	public void forward(Widget theWidget)
	{
		myWidgets.add(theWidget);
		myAnimationWidget.removeStyleName("overflow");
		if (myFirstWidget)
		{
			myAnimationWidget.setFirstWidget(theWidget);
		} else
		{
			myAnimationWidget.setSecondWidget(theWidget);
		}
		myAnimationWidget.animate(Animations.SLIDE, myFirstWidget, this);
		myFirstWidget = !myFirstWidget;
	}

	public void backward()
	{
		if (myWidgets.size() == 1)
		{
			return;
		}
		myAnimationWidget.removeStyleName("overflow");
		Widget aWidget = myWidgets.get(myWidgets.size() - 2);
		if (myFirstWidget)
		{
			myAnimationWidget.setFirstWidget(aWidget);
		} else
		{
			myAnimationWidget.setSecondWidget(aWidget);
		}
		myWidgets.remove(myWidgets.size() - 1);
		myAnimationWidget.animate(Animations.SLIDE_REVERSE, myFirstWidget, this);
		myFirstWidget = !myFirstWidget;
	}

	@Override
	public void onAnimationEnd()
	{
		myAnimationWidget.addStyleName("overflow");
	}
}
