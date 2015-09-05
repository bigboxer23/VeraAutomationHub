package com.jones.matt.lights.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * overlay over data retrieved from hue hub
 */
public class HueLightStateVO
{
	@SerializedName("on")
	private boolean myState;

	@SerializedName("bri")
	private int myBrightness;

	@SerializedName("hue")
	private int myHue;

	@SerializedName("sat")
	private int mySaturation;

	@SerializedName("effect")
	private String myEffect;

	@SerializedName("xy")
	private List<Float> myXY;

	@SerializedName("ct")
	private int myCT;

	@SerializedName("alert")
	private String myAlert;

	@SerializedName("colormode")
	private String myColorMode;

	@SerializedName("reachable")
	private boolean myReachable;

	public boolean isState()
	{
		return myState;
	}

	public int getBrightness()
	{
		return myBrightness;
	}

	public int getHue()
	{
		return myHue;
	}

	public int getSaturation()
	{
		return mySaturation;
	}

	public String getEffect()
	{
		return myEffect;
	}

	public List<Float> getXY()
	{
		return myXY;
	}

	public int getCT()
	{
		return myCT;
	}

	public String getAlert()
	{
		return myAlert;
	}

	public String getColorMode()
	{
		return myColorMode;
	}

	public boolean isReachable()
	{
		return myReachable;
	}
}
