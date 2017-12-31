package com.bigboxer23.house.lights.client.ui.room;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPropertyHelper;
import com.googlecode.mgwt.ui.client.widget.panel.flex.RootFlexPanel;
import com.bigboxer23.house.lights.client.utility.DefaultRequestBuilder;
import com.bigboxer23.house.lights.client.model.DeviceVO;
import com.bigboxer23.house.lights.client.ui.Header;
import com.bigboxer23.house.lights.client.ui.TimedSlider;
import com.bigboxer23.house.lights.client.utility.VeraUrlUtility;

/**
 *
 */
public class DeviceDimPanel extends RootFlexPanel
{
	public DeviceDimPanel(final DeviceVO theData)
	{
		setAlignment(FlexPropertyHelper.Alignment.CENTER);
		add(new Header(theData.getName()));
		final TimedSlider aTimedSlider = new TimedSlider();
		aTimedSlider.setValue(Integer.parseInt(theData.getLevel()));
		aTimedSlider.addChangeHandler(new ChangeHandler()
		{
			@Override
			public void onChange(ChangeEvent theEvent)
			{
				new DefaultRequestBuilder(VeraUrlUtility.getDimUrl(aTimedSlider.getValue(), theData.getID())).send();
			}
		});
		add(aTimedSlider);
	}
}
