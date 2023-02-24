package com.bigboxer23.lights.controllers.frontdoor;

import com.bigboxer23.lights.controllers.AbstractBaseController;
import com.bigboxer23.lights.controllers.vera.VeraHouseVO;
import com.bigboxer23.utils.http.OkHttpCallback;
import com.bigboxer23.utils.http.OkHttpUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.net.HttpURLConnection;
import java.util.Optional;
import okhttp3.Call;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** */
@Tag(name = "Front Door Controller", description = "Service to control the front door camera")
@RestController
public class FrontDoorController extends AbstractBaseController {
	@Value("${frontDoor.url}")
	private String myFrontDoorURL;

	private int myFrontDoorPauseTime = 0;

	public void getStatus(VeraHouseVO theHouseStatus) {
		theHouseStatus.setFrontDoorPauseTime(myFrontDoorPauseTime);
	}

	@GetMapping(value = "/S/FrontDoor/{delay}", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(
			summary = "Pause the front door from triggering notifications",
			description = "Pass the delay in seconds to pause the front door camera from triggering"
					+ " notifications and sending emails")
	@ApiResponses({
		@ApiResponse(responseCode = HttpURLConnection.HTTP_UNAUTHORIZED + "", description = "unauthorized"),
		@ApiResponse(responseCode = HttpURLConnection.HTTP_OK + "", description = "success")
	})
	public String doAction(
			@Parameter(description = "time in seconds to pause") @PathVariable(value = "delay") Long delay) {
		myLogger.info("front door change requested: " + delay);
		OkHttpUtil.get(myFrontDoorURL + "/pause/" + delay, new OkHttpCallback() {
			public void onResponseBodyString(Call call, String responseBody) {
				myFrontDoorPauseTime =
						Optional.ofNullable(responseBody).map(Integer::parseInt).orElse(0);
				myLogger.info("front door changed");
			}
		});
		return null;
	}

	@Scheduled(fixedDelay = 10000)
	private void fetchFrontDoorStatus() {
		myLogger.debug("Fetching front door status");
		OkHttpUtil.get(myFrontDoorURL + "/isPaused", new OkHttpCallback() {
			@Override
			public void onResponseBodyString(Call call, String responseBody) {
				myFrontDoorPauseTime =
						Optional.ofNullable(responseBody).map(Integer::parseInt).orElse(0);
				myLogger.debug("Fetched front door status " + myFrontDoorPauseTime);
			}
		});
	}
}
