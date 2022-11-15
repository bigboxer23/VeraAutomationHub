package com.bigboxer23.lights.controllers.garage;

import com.bigboxer23.lights.controllers.AbstractBaseController;
import com.bigboxer23.lights.controllers.ITemperatureController;
import com.bigboxer23.lights.controllers.vera.VeraDeviceVO;
import com.bigboxer23.lights.controllers.vera.VeraHouseVO;
import com.bigboxer23.lights.data.WeatherData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Control garage pi
 */
@Tag(name = "Garage Controller", description = "Service to control the garage door pi")
@RestController
public class GarageController extends AbstractBaseController implements ITemperatureController
{
	@Value("${garage.url}")
	private String myGarageURL;

	private VeraDeviceVO myGarageData;

	@GetMapping(value = {"/S/Garage/{command}", "/S/Garage/{command}/{delay}"},
			produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Service for communicating with the garage pi",
			description = "Allows pass through of various commands associated with the garage pi.")
	@ApiResponses({@ApiResponse(responseCode = HttpURLConnection.HTTP_UNAUTHORIZED + "", description = "unauthorized"),
			@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")})
	public String doAction(@Parameter(description = "command to run.  Possible values [Open, Close, Status2, SetAutoCloseDelay, DisableAutoClose]") @PathVariable(value = "command") String command,
	                       @Parameter(description = "used with SetAutoCloseDelay. Seconds to set delay for", required = false)
	                       @PathVariable(value = "delay", required = false) Long delay)
	{
		myLogger.error("Garage Door change requested: " + command);
		myGarageData = fromJson(myGarageURL + "/" + command + (delay != null ? "/" + delay : ""), VeraDeviceVO.class);
		myGarageData.setName("Garage Opener");
		myGarageData.setStatus(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz").format(new Date()));
		return null;
	}

	/**
	 * Fetch data about the weather from the sensor in the garage
	 *
	 * @return
	 * @throws IOException
	 */
	@Override
	public WeatherData getWeatherData()
	{
		return new WeatherData(Float.parseFloat(myGarageData.getTemperature()), myGarageData.getHumidity());
	}

	public void getStatus(VeraHouseVO theHouseStatus)
	{
		if (myGarageData != null && theHouseStatus != null && theHouseStatus.getRooms() != null)
		{
			theHouseStatus.getRooms().stream().filter(aRoom -> aRoom.getName().equals("Garage")).forEach(aRoom -> aRoom.addDevice(myGarageData));
		}
	}

	@Scheduled(fixedDelay = 5000)
	private void fetchGarageStatus()
	{
		try
		{
			myLogger.debug("Fetching new garage data");
			myGarageData = fromJson(myGarageURL + "/Status2", VeraDeviceVO.class);
			if (myGarageData == null)
			{
				myLogger.info("Couldn't get status from garage node...");
				return;
			}
			myGarageData.setName("Garage Opener");
			myLogger.debug("Fetched new garage data");
		} catch (Exception e)
		{
			myLogger.error("FetchGarageStatus", e);
		}
	}
}
