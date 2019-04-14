package com.bigboxer23.lights.controllers.climate;

import com.bigboxer23.lights.controllers.AbstractBaseController;
import com.bigboxer23.lights.controllers.vera.VeraDeviceVO;
import com.bigboxer23.lights.controllers.vera.VeraHouseVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Controller to integrate with https://github.com/bigboxer23/climate-service project service
 */
@Component
public class ClimateController extends AbstractBaseController
{
	@Value("${climate.url}")
	private String myClimateServiceUrl;

	private ClimateData myClimateData;

	public void getClimateData(VeraHouseVO theHouseStatus)
	{
		if (myClimateData != null && theHouseStatus != null && theHouseStatus.getRooms() != null)
		{
			theHouseStatus.getRooms().stream().filter(aRoom -> aRoom.getName().equals("Climate")).findAny().ifPresent(aRoom ->
			{
				aRoom.addDevice(new VeraDeviceVO("Inside Temperature", myClimateData.getTemperature()));
				aRoom.addDevice(new VeraDeviceVO("Inside Humidity", myClimateData.getHumidity()));
				aRoom.addDevice(new VeraDeviceVO("Barometric Pressure", myClimateData.getPressure()));
				aRoom.addDevice(new VeraDeviceVO("Air  Quality", myClimateData.getQuality()));
			});
		}
	}

	@Scheduled(fixedDelay = 30000)
	private void fetchClimateData()
	{
		try
		{
			myLogger.debug("Fetching new climate data");
			myClimateData = fromJson(myClimateServiceUrl + "/climate", ClimateData.class);
			if (myClimateData == null)
			{
				myLogger.info("Couldn't get status from garage node...");
				return;
			}
			myLogger.debug("Fetched new climate data");
		} catch (Exception e)
		{
			myLogger.error("fetchClimateData", e);
		}
	}
}
