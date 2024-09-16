package com.bigboxer23.lights.controllers.aggregate;

import com.bigboxer23.lights.controllers.switchbot.SwitchBotController;
import com.bigboxer23.switch_bot.IDeviceCommands;
import com.bigboxer23.utils.command.RetryingCommand;
import com.bigboxer23.utils.time.ITimeConstants;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

/** */
@Controller
public class FanSystemController {
	private static final Logger logger = LoggerFactory.getLogger(FanSystemController.class);

	/** id of the switchbot switch device */
	@Value("${fanSwitchId}")
	private String fanSwitchId;

	/** Minutes to run fan */
	@Value("${fanDuration}")
	private long fanDuration;

	private final SwitchBotController switchbotController;

	public FanSystemController(SwitchBotController switchbotController) {
		this.switchbotController = switchbotController;
	}

	@Scheduled(fixedDelay = 3600000) // every 6 hours
	public void runFans() throws IOException, InterruptedException {
		logger.info("Turning on fan system");
		RetryingCommand.execute(
				() -> {
					switchbotController
							.getSwitchbotAPI()
							.getDeviceApi()
							.sendDeviceControlCommands(fanSwitchId, IDeviceCommands.PLUG_MINI_ON);
					return null;
				},
				fanSwitchId);

		logger.debug("sleeping fan system controller");
		Thread.sleep(fanDuration * ITimeConstants.MINUTE);
		logger.info("Turning off fan system");
		RetryingCommand.execute(
				() -> {
					switchbotController
							.getSwitchbotAPI()
							.getDeviceApi()
							.sendDeviceControlCommands(fanSwitchId, IDeviceCommands.PLUG_MINI_OFF);
					return null;
				},
				fanSwitchId);
	}
}
