package com.bigboxer23.lights.controllers.elastic;

import com.bigboxer23.lights.controllers.vera.VeraDeviceVO;
import com.bigboxer23.lights.controllers.vera.VeraHouseVO;
import com.bigboxer23.lights.controllers.vera.VeraRoomVO;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;

/**
 * Send statistics about house status to an elasticsearch backend
 */
@Component
public class ElasticAnalyticsController
{
	@Value("${elastic.url}")
	private String myElasticUrl;

	private static final String kIndexName = "homeautomation";

	public static final String kType = "Status";

	private RestHighLevelClient myClient;

	private static final Logger myLogger = LoggerFactory.getLogger(ElasticAnalyticsController.class);

	public void logStatusEvent(VeraHouseVO theVeraHouseVO)
	{
		BulkRequest aBulkRequest = new BulkRequest();
		handleLightsData(theVeraHouseVO, aBulkRequest);
		handleClimateData(theVeraHouseVO, aBulkRequest);
		try
		{
			if (aBulkRequest.numberOfActions() > 0)
			{
				myLogger.debug("Sending Request to elastic");
				getClient().bulk(aBulkRequest);
			}
		}
		catch (IOException theE)
		{
			myLogger.error("logStatusEvent:", theE);
		}
	}

	private void handleLightsData(VeraHouseVO theVeraHouseVO, BulkRequest theRequest)
	{
		myLogger.debug("handleLightsData");
		theVeraHouseVO
				.getRooms()
				.stream()
				.filter(theRoom -> theRoom.getDevices() == null ? false : theRoom.getDevices()
						.stream()
						.filter(VeraDeviceVO::isLight)
						.findAny()
						.map(VeraDeviceVO::isLight)
						.orElse(false))
				.forEach(theRoom ->
				{
					Map<String, Object> aDocument = new HashMap<>();
					aDocument.put("on", isRoomOn(theRoom));
					aDocument.put("time", new Date());
					aDocument.put("name", theRoom.getName());
					aDocument.put("type", "light");
					aDocument.put("totalDevices", theRoom.getDevices().size());
					theRequest.add(new IndexRequest(kIndexName, kType, theRoom.getName() + System.currentTimeMillis()).source(aDocument));
				});
	}

	private boolean isRoomOn(VeraRoomVO theRoom)
	{
		return theRoom
				.getDevices()
				.stream()
				.filter(VeraDeviceVO::isLight)
				.filter(theDevice -> theDevice.getLevel() > 0)
				.findAny()
				.map(theDevice -> theDevice.getLevel() > 0)
				.orElse(false);
	}

	private void handleClimateData(VeraHouseVO theVeraHouseVO, BulkRequest theRequest)
	{
		myLogger.debug("handleClimateData");
		theVeraHouseVO.getRooms()
				.stream()
				.filter(theRoom -> theRoom.getName().equalsIgnoreCase("climate control"))
				.findAny()
				.ifPresent(theRoom ->
				{
					theRoom
							.getDevices()
							.stream()
							.filter(Objects::nonNull)
							.forEach(theVeraDeviceVO ->
					{
						Map<String, Object> aDocument = new HashMap<>();
						aDocument.put("time", new Date());
						aDocument.put("name", theVeraDeviceVO.getName());
						aDocument.put("type", "hvac");
						switch (theVeraDeviceVO.getName().toLowerCase())
						{
							case "high temperature":
							case "low temperature":
							case "temperature":
							case "thermostat":
								aDocument.put("temperature", Double.parseDouble(theVeraDeviceVO.getTemperature()));
								if (theVeraDeviceVO.getName().equalsIgnoreCase("thermostat"))
								{
									aDocument.put("fanMode", theVeraDeviceVO.getFanMode());
									aDocument.put("batteryLevel", Integer.parseInt(theVeraDeviceVO.getBatteryLevel()));
									aDocument.put("cool", theVeraDeviceVO.getMode().equalsIgnoreCase("coolon"));
									aDocument.put("heat", theVeraDeviceVO.getMode().equalsIgnoreCase("heaton"));
									aDocument.put("setPoint", theVeraDeviceVO.getSetPoint());
								}
								break;
							case "humidity":
							case "humidity sensor":
								aDocument.put("humidity", theVeraDeviceVO.getHumidity());
								break;
						}
						if (aDocument.size() > 3)
						{
							theRequest.add(new IndexRequest(kIndexName, kType, theVeraDeviceVO.getName() + System.currentTimeMillis()).source(aDocument));
						}
					});
				});
	}

	@PreDestroy
	public void destroy() throws IOException
	{
		if (myClient != null)
		{
			myLogger.debug("closing elastic client");
			myClient.close();
		}
	}

	private RestHighLevelClient getClient()
	{
		if (myClient == null)
		{
			myClient = new RestHighLevelClient(RestClient.builder(HttpHost.create(myElasticUrl)));
		}
		return myClient;
	}
}
