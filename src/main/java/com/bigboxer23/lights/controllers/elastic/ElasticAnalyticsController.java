package com.bigboxer23.lights.controllers.elastic;

import com.bigboxer23.lights.controllers.vera.VeraDeviceVO;
import com.bigboxer23.lights.controllers.vera.VeraHouseVO;
import com.bigboxer23.lights.controllers.vera.VeraRoomVO;
import java.io.IOException;
import java.util.*;
import javax.annotation.PreDestroy;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Send statistics about house status to an elasticsearch backend */
@Component
public class ElasticAnalyticsController {
	@Value("${elastic.url}")
	private String myElasticUrl;

	private static final String kIndexName = "homeautomation";

	public static final String kType = "Status";

	private RestHighLevelClient myClient;

	private static final Logger myLogger = LoggerFactory.getLogger(ElasticAnalyticsController.class);

	public void logStatusEvent(VeraHouseVO theVeraHouseVO) {
		BulkRequest aBulkRequest = new BulkRequest();
		handleLightsData(theVeraHouseVO, aBulkRequest);
		handleClimateData(theVeraHouseVO, aBulkRequest);
		try {
			if (aBulkRequest.numberOfActions() > 0) {
				myLogger.debug("Sending Request to elastic");
				getClient().bulk(aBulkRequest);
			}
		} catch (IOException theE) {
			myLogger.error("logStatusEvent:", theE);
		}
	}

	private void handleLightsData(VeraHouseVO theVeraHouseVO, BulkRequest theRequest) {
		myLogger.debug("handleLightsData");
		theVeraHouseVO.getRooms().stream()
				.filter(theRoom -> !theRoom.getName().equalsIgnoreCase("scenes"))
				.filter(theRoom -> theRoom.getDevices() == null
						? false
						: theRoom.getDevices().stream()
								.filter(VeraDeviceVO::isLight)
								.findAny()
								.map(VeraDeviceVO::isLight)
								.orElse(false))
				.forEach(theRoom -> {
					long aNumberDevicesOn = getNumberOfDevicesOn(theRoom);
					Map<String, Object> aDocument = new HashMap<>();
					aDocument.put("on", aNumberDevicesOn > 0);
					aDocument.put("numberOfDevicesOn", getNumberOfDevicesOn(theRoom));
					aDocument.put("time", new Date());
					aDocument.put("name", theRoom.getName());
					aDocument.put("type", "light");
					aDocument.put("totalDevices", theRoom.getDevices().size());
					theRequest.add(new IndexRequest(kIndexName, kType, theRoom.getName() + System.currentTimeMillis())
							.source(aDocument));
				});
	}

	private long getNumberOfDevicesOn(VeraRoomVO theRoom) {
		return theRoom.getDevices().stream()
				.filter(VeraDeviceVO::isLight)
				.filter(ElasticAnalyticsController::isDeviceOn)
				.count();
	}

	private static boolean isDeviceOn(VeraDeviceVO device) {
		try {
			return Integer.parseInt(Optional.ofNullable(device.getLevel()).orElse("0")) > 0;
		} catch (NumberFormatException aNFE) {
			return false;
		}
	}

	private void handleClimateData(VeraHouseVO theVeraHouseVO, BulkRequest theRequest) {
		myLogger.debug("handleClimateData");
		theVeraHouseVO.getRooms().stream()
				.filter(theRoom -> theRoom.getName().equalsIgnoreCase("climate"))
				.findAny()
				.ifPresent(theRoom -> {
					Map<String, Object> aThermostatDocument = new HashMap<>();
					aThermostatDocument.put("time", new Date());
					aThermostatDocument.put("name", "thermostat");
					aThermostatDocument.put("type", "hvac");
					theRoom.getDevices().stream().filter(Objects::nonNull).forEach(theVeraDeviceVO -> {
						Map<String, Object> aDocument = new HashMap<>();
						aDocument.put("time", new Date());
						aDocument.put("name", theVeraDeviceVO.getName());
						aDocument.put("type", "hvac");
						switch (theVeraDeviceVO.getName().toLowerCase()) {
							case "high temperature":
							case "low temperature":
							case "outside temperature":
								double aTemp = getDoubleTemperature(theVeraDeviceVO);
								if (aTemp == -99) {
									return;
								}
								aDocument.put("temperature", aTemp);
								if (theVeraDeviceVO.getName().equalsIgnoreCase("outside temperature")) {
									aDocument.put("name", "temperature");
								}
								break;
							case "inside temperature":
								aTemp = getDoubleTemperature(theVeraDeviceVO);
								if (aTemp != -99) {
									aThermostatDocument.put("temperature", aTemp);
								}
								break;
							case "thermostat fan mode":
								aThermostatDocument.put("fanMode", theVeraDeviceVO.getLevel());
								break;
							case "thermostat battery":
								aThermostatDocument.put("batteryLevel", theVeraDeviceVO.getLevel());
								break;
							case "thermostat mode":
								aThermostatDocument.put("cool", "2".equals(theVeraDeviceVO.getLevel()));
								aThermostatDocument.put("heat", "1".equals(theVeraDeviceVO.getLevel()));
								break;
							case "heating setpoint":
							case "cooling setpoint":
								aThermostatDocument.put(theVeraDeviceVO.getName(), theVeraDeviceVO.getLevel());
								break;
							case "inside humidity":
							case "outside humidity":
								float aHumidity = getFloatTemperature(theVeraDeviceVO);
								if (aHumidity == -99) {
									return;
								}
								aDocument.put("humidity", aHumidity);
								aDocument.put(
										"name",
										theVeraDeviceVO.getName().equalsIgnoreCase("inside" + " humidity")
												? "humidity sensor"
												: "humidity");
								break;
						}
						if (aDocument.size() > 3) {
							theRequest.add(new IndexRequest(
											kIndexName, kType, theVeraDeviceVO.getName() + System.currentTimeMillis())
									.source(aDocument));
						}
					});
					if (aThermostatDocument.get("cool") != null) {
						String aSetPoint = ((boolean) aThermostatDocument.get("cool")) ? "Cooling" : "Heating";
						aThermostatDocument.put("setPoint", aThermostatDocument.get(aSetPoint + " Setpoint"));
					}
					theRequest.add(new IndexRequest(
									kIndexName,
									kType,
									((String) aThermostatDocument.get("name")) + System.currentTimeMillis())
							.source(aThermostatDocument));
				});
	}

	private float getFloatTemperature(VeraDeviceVO theDeviceVO) {
		String aLevel = theDeviceVO.getLevel();
		if (aLevel.contains(" ")) {
			aLevel = aLevel.substring(0, aLevel.indexOf(" "));
		}
		try {
			return Float.parseFloat(aLevel);
		} catch (NumberFormatException aNFE) {
			return -99;
		}
	}

	private double getDoubleTemperature(VeraDeviceVO theDeviceVO) {
		String aLevel = theDeviceVO.getLevel();
		if (aLevel.contains(" ")) {
			aLevel = aLevel.substring(0, aLevel.indexOf(" "));
		}
		try {
			return Double.parseDouble(aLevel);
		} catch (NumberFormatException aNFE) {
			return -99;
		}
	}

	@PreDestroy
	public void destroy() throws IOException {
		if (myClient != null) {
			myLogger.debug("closing elastic client");
			myClient.close();
		}
	}

	private RestHighLevelClient getClient() {
		if (myClient == null) {
			myClient = new RestHighLevelClient(RestClient.builder(HttpHost.create(myElasticUrl)));
		}
		return myClient;
	}
}
