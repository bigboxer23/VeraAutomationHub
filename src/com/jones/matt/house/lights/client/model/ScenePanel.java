package com.jones.matt.house.lights.client.model;

import com.googlecode.mgwt.dom.client.event.tap.TapEvent;
import com.googlecode.mgwt.dom.client.event.tap.TapHandler;
import com.googlecode.mgwt.ui.client.widget.button.Button;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPanel;
import com.googlecode.mgwt.ui.client.widget.panel.flex.FlexPropertyHelper;
import com.jones.matt.house.lights.client.utility.DefaultRequestBuilder;
import com.jones.matt.house.lights.client.utility.VeraUrlUtility;

/**
 * Panel for displaying a button for a scene.  Does not have on/off state, no extra "dim" panel to push to
 */
public class ScenePanel extends FlexPanel
{
	private SceneVO mySceneVO;

	public ScenePanel(SceneVO theScene)
	{
		addStyleName("ScenePanel");
		mySceneVO = theScene;
		Button aButton = new Button(theScene.getName());
		aButton.addStyleName("button-grow");
		aButton.addTapHandler(new TapHandler()
		{
			@Override
			public void onTap(TapEvent event)
			{
				new DefaultRequestBuilder(VeraUrlUtility.getSceneUrl(mySceneVO.getID())).send();
			}
		});
		setOrientation(FlexPropertyHelper.Orientation.HORIZONTAL);
		add(aButton);
		addStyleName("fillWidth");
	}
}
