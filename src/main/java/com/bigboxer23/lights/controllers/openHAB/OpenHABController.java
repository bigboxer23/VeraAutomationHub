package com.bigboxer23.lights.controllers.openHAB;

import com.bigboxer23.lights.controllers.AbstractBaseController;
import com.bigboxer23.utils.http.OkHttpCallback;
import com.bigboxer23.utils.http.OkHttpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** Control OpenHab instance via REST URL */
@Tag(name = "OpenHAB Controller", description = "Service to send commands to OpenHAB")
@RestController
public class OpenHABController extends AbstractBaseController {
	/** Location of OpenHAB */
	@Value("${openHABUrl}")
	private String kOpenHABUrl;

	private Set<String> mySmartRooms;

	public OpenHABHouse getStatus() {
		myLogger.debug("Getting OpenHAB Status");
		OpenHABHouse aHouseStatus =
				fromJson(kOpenHABUrl + "/rest/items?type=Group&tags=Room&recursive=true", OpenHABHouse.class);
		myLogger.debug("Got OpenHAB Status");
		return aHouseStatus;
	}

	public Set<String> getSmartRooms() {
		if (mySmartRooms == null) {
			fetchSmartRooms();
		}
		return mySmartRooms;
	}

	@GetMapping(value = "/S/OpenHAB/{deviceId}/{command}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			summary = "Pause the front door from triggering notifications",
			description = "Pass the delay in seconds to pause the front door camera from triggering"
					+ " notifications and sending emails")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_UNAUTHORIZED + "", description = "unauthorized"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public String doAction(
			@Parameter(description = "deviceId to run command on") @PathVariable(value = "deviceId") String deviceId,
			@Parameter(description = "command to run.  Possible values [0-100, ON, OFF]")
					@PathVariable(value = "command")
					String command) {
		OkHttpUtil.post(kOpenHABUrl + "/rest/items/" + deviceId, new OkHttpCallback(), builder -> {
			try {
				builder.post(RequestBody.create(URLDecoder.decode(command, StandardCharsets.UTF_8.displayName())
						.getBytes(StandardCharsets.UTF_8)));
			} catch (UnsupportedEncodingException theE) {
				myLogger.warn("OpenHABController:doAction", theE);
			}
			return builder;
		});
		return null;
	}

	public OpenHABHouse getItemsByTag(String theTag) {
		return fromJson(kOpenHABUrl + "/rest/items?tags=" + theTag, OpenHABHouse.class);
	}

	public List<OpenHABItem> getItemByName(String theName) {
		OpenHABItem anItem = fromJson(kOpenHABUrl + "/rest/items/" + theName, OpenHABItem.class);
		return anItem == null || anItem.getName() == null ? null : Collections.singletonList(anItem);
	}

	public void setLevel(String theItem, int theLevel) {
		doAction(theItem, "" + theLevel);
	}

	/**
	 * Turn on or off vacation mode to change global behavior based on further scene rules
	 *
	 * @param theVacationMode
	 */
	public void setVacationMode(boolean theVacationMode) {
		setModeFromCalendar(theVacationMode, "VacationMode");
	}

	public void setPTOMode(boolean thePTOMode) {
		setModeFromCalendar(thePTOMode, "IsPTO");
	}

	public void setExtendedEveningMode(boolean extendedEveningMode) {
		setModeFromCalendar(extendedEveningMode, "IsExtendedEvening");
	}

	private void setModeFromCalendar(boolean theMode, String theDevice) {
		myLogger.info(theDevice + " requested: " + theMode);
		doAction(theDevice, theMode ? "ON" : "OFF");
	}

	@Scheduled(fixedDelay = 5000)
	private void fetchSmartRooms() {
		try {
			myLogger.debug("Getting Smart Rooms");
			mySmartRooms = Optional.ofNullable(fromJson(kOpenHABUrl + "/rest/items?tags=SmartRoom", OpenHABHouse.class))
					.orElse(new OpenHABHouse())
					.stream()
					.map(OpenHABItem::getName)
					.collect(Collectors.toSet());
			myLogger.debug("Retrieved Smart Rooms");
		} catch (Exception e) {
			myLogger.error("fetchSmartRooms", e);
		}
	}
}
