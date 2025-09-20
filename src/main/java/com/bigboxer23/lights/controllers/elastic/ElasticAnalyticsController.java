package com.bigboxer23.lights.controllers.elastic;

import com.bigboxer23.lights.controllers.vera.VeraDeviceVO;
import com.bigboxer23.lights.controllers.vera.VeraHouseVO;
import com.bigboxer23.lights.controllers.vera.VeraRoomVO;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Send statistics about house status to an elasticsearch backend */
@Slf4j
@Component
public class ElasticAnalyticsController implements DisposableBean {
	@Value("${elastic.url}")
	private String myElasticUrl;

	private static final String kIndexName = "homeautomation";

	public static final String kType = "Status";

	private RestHighLevelClient myClient;

	public void logStatusEvent(VeraHouseVO theVeraHouseVO) {
		BulkRequest aBulkRequest = new BulkRequest();
		handleLightsData(theVeraHouseVO, aBulkRequest);
		handleClimateData(theVeraHouseVO, aBulkRequest);
		if (aBulkRequest.numberOfActions() > 0) {
			log.debug("Sending Request to elastic");
			getClient().bulkAsync(aBulkRequest, RequestOptions.DEFAULT, new ActionListener<>() {
				@Override
				public void onResponse(BulkResponse theBulkItemResponses) {}

				@Override
				public void onFailure(Exception e) {
					log.debug("logStatusEvent:", e);
				}
			});
		}
	}

	private void handleLightsData(VeraHouseVO theVeraHouseVO, BulkRequest theRequest) {
		log.debug("handleLightsData");
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
		log.debug("handleClimateData");
		theVeraHouseVO.getRooms().stream()
				.filter(theRoom -> theRoom.getName().equalsIgnoreCase("climate"))
				.findAny()
				.ifPresent(theRoom -> {
					Map<String, Object> aThermostatDocument = new HashMap<>();
					aThermostatDocument.put("time", new Date());
					aThermostatDocument.put("name", "thermostat");
					aThermostatDocument.put("type", "hvac");
					theRoom.getDevices().stream().filter(Objects::nonNull).forEach(deviceVO -> {
						Map<String, Object> aDocument = new HashMap<>();
						aDocument.put("time", new Date());
						aDocument.put("name", deviceVO.getName());
						aDocument.put("type", "hvac");
						switch (deviceVO.getName().toLowerCase()) {
							case "high temperature":
							case "low temperature":
							case "outside temperature":
								double aTemp = getDoubleTemperature(deviceVO);
								if (aTemp == -99) {
									return;
								}
								aDocument.put("temperature", aTemp);
								if (deviceVO.getName().equalsIgnoreCase("outside temperature")) {
									aDocument.put("name", "temperature");
								}
								break;
							case "inside temperature":
								aTemp = getDoubleTemperature(deviceVO);
								if (aTemp != -99) {
									aThermostatDocument.put("temperature", aTemp);
								}
								break;
							case "thermostat fan mode":
								aThermostatDocument.put("fanMode", deviceVO.getLevel());
								break;
							case "thermostat battery":
								aThermostatDocument.put("batteryLevel", deviceVO.getLevel());
								break;
							case "thermostat mode":
								aThermostatDocument.put("cool", "2".equals(deviceVO.getLevel()));
								aThermostatDocument.put("heat", "1".equals(deviceVO.getLevel()));
								break;
							case "heating setpoint":
							case "cooling setpoint":
								aThermostatDocument.put(deviceVO.getName(), deviceVO.getLevel());
								break;
							case "inside humidity":
							case "outside humidity":
								float aHumidity = getFloatTemperature(deviceVO);
								if (aHumidity == -99) {
									return;
								}
								aDocument.put("humidity", aHumidity);
								aDocument.put(
										"name",
										deviceVO.getName().equalsIgnoreCase("inside" + " humidity")
												? "humidity sensor"
												: "humidity");
								break;
							case "air quality":
								float airQuality = getFloatTemperature(deviceVO);
								if (airQuality == -99) {
									return;
								}
								aDocument.put("air quality", airQuality);
								break;
						}
						if (aDocument.size() > 3) {
							theRequest.add(
									new IndexRequest(kIndexName, kType, deviceVO.getName() + System.currentTimeMillis())
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

	@Scheduled(cron = "0 0 2 15 * ?")
	public void deleteOldMetricbeatIndices() {
		try {
			LocalDate cutoffDate = LocalDate.now().minusMonths(4);
			String cutoffDateStr = cutoffDate.format(DateTimeFormatter.ofPattern("yyyy.MM"));

			GetIndexRequest getIndexRequest = new GetIndexRequest("metricbeat-*" + cutoffDateStr + ".*");
			GetIndexResponse getIndexResponse = getClient().indices().get(getIndexRequest, RequestOptions.DEFAULT);

			String[] indicesToDelete = getIndexResponse.getIndices();

			if (indicesToDelete.length > 0) {
				log.info("Deleting {} old metricbeat indices older than {}", indicesToDelete.length, cutoffDateStr);
				for (String indexToDelete : indicesToDelete) {
					getClient().indices().delete(new DeleteIndexRequest(indexToDelete), RequestOptions.DEFAULT);
					log.info("Deleted index: {}", indexToDelete);
				}
				log.info("Successfully deleted {} old metricbeat indices", indicesToDelete.length);
			} else {
				log.info("No old metricbeat indices found to delete");
			}
		} catch (Exception e) {
			log.error("Error deleting old metricbeat indices", e);
		}
	}

	@Override
	public void destroy() throws IOException {
		if (myClient != null) {
			log.debug("closing elastic client");
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
