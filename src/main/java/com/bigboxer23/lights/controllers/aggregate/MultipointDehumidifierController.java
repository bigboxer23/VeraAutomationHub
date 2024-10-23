package com.bigboxer23.lights.controllers.aggregate;

import com.bigboxer23.lights.controllers.switchbot.SwitchBotController;
import com.bigboxer23.switch_bot.IDeviceCommands;
import com.bigboxer23.switch_bot.data.Device;
import com.bigboxer23.utils.command.RetryingCommand;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * This component takes one or more sensors (switchbot) and takes an average humidity reading. It
 * will turn on or off a switchbot smart switch which should be connected to a dehumidifier based on
 * the reading and defined low/high values of RH
 */
@Component
public class MultipointDehumidifierController implements InitializingBean {
	private static final Logger logger = LoggerFactory.getLogger(MultipointDehumidifierController.class);

	@Value("${dehumidifierId}")
	private String dehumidifierId;

	@Value("${sensorIds}")
	private String sensorIdString;

	@Value("${lowHumidity}")
	private float lowHumidity;

	@Value("${highHumidity}")
	private float highHumidity;

	private List<String> sensorIds;

	private final SwitchBotController switchbotController;

	public MultipointDehumidifierController(SwitchBotController switchbotController) {
		this.switchbotController = switchbotController;
		logger.info("MultipointDehumidifierController initialized");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		sensorIds = Arrays.stream(sensorIdString.split(",")).map(String::trim).toList();
	}

	@Scheduled(fixedDelay = 300000) // 5min
	private void checkHumidity() throws IOException {
		double humidity = getHumidity();
		logger.info("humidity: " + humidity);
		if (humidity > highHumidity && !isHumidifierPowerOn()) {
			logger.info("turning on dehumidifier " + dehumidifierId);
			RetryingCommand.execute(
					() -> {
						switchbotController
								.getSwitchbotAPI()
								.getDeviceApi()
								.sendDeviceControlCommands(dehumidifierId, IDeviceCommands.PLUG_MINI_ON);
						return null;
					},
					switchbotController.getIdentifier(dehumidifierId));
		} else if (humidity < lowHumidity && isHumidifierPowerOn()) {
			logger.info("turning off dehumidifier " + dehumidifierId);
			RetryingCommand.execute(
					() -> {
						switchbotController
								.getSwitchbotAPI()
								.getDeviceApi()
								.sendDeviceControlCommands(dehumidifierId, IDeviceCommands.PLUG_MINI_OFF);
						return null;
					},
					switchbotController.getIdentifier(dehumidifierId));
		}
	}

	private boolean isHumidifierPowerOn() throws IOException {
		return RetryingCommand.execute(
						() -> switchbotController
								.getSwitchbotAPI()
								.getDeviceApi()
								.getDeviceStatus(dehumidifierId),
						switchbotController.getIdentifier(dehumidifierId))
				.isPowerOn();
	}

	private double getHumidity() {
		logger.debug("checking humidity");
		return sensorIds.stream()
				.map(id -> {
					try {
						return RetryingCommand.execute(
								() -> switchbotController
										.getSwitchbotAPI()
										.getDeviceApi()
										.getDeviceStatus(id),
								switchbotController.getIdentifier(id));
					} catch (IOException e) {
						logger.error("", e);
						throw new RuntimeException(e);
					}
				})
				.map(Device::getHumidity)
				.mapToDouble(a -> a)
				.average()
				.orElse(-1);
	}
}
