package com.jones.matt.lights.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Encapsulate a "scene" (or room whatevs)
 */
public class SceneVO
{
	@SerializedName("name")
	private String mySceneName;

	@SerializedName("lights")
	private List<LightVO> myLights;

	@SerializedName("status")
	private boolean myStatus;

	@SerializedName("weather")
	private WeatherData myWeatherData;

	@SerializedName("events")
	private List<String> myNotificationEvents;

	public SceneVO(){}

	public SceneVO(String theName)
	{
		mySceneName = theName;
	}

	public String getSceneName()
	{
		return mySceneName;
	}

	public String getSceneUrl()
	{
		return mySceneName.replace(" ", "");
	}

	public List<LightVO> getLights()
	{
		return myLights;
	}

	public boolean isStatus()
	{
		return myStatus;
	}

	public void setStatus(boolean theStatus)
	{
		myStatus = theStatus;
	}

	public WeatherData getWeatherData()
	{
		return myWeatherData;
	}

	public void setWeatherData(WeatherData theWeatherData)
	{
		myWeatherData = theWeatherData;
	}

	/**
	 * Events our lights should respond with an alert to
	 *
	 * @return
	 */
	public List<String> getNotificationEvents()
	{
		return myNotificationEvents;
	}

	public void setNotificationEvents(List<String> theNotificationEvents)
	{
		myNotificationEvents = theNotificationEvents;
	}
}
