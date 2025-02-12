package com.bigboxer23.lights.controllers.aggregate;

import com.bigboxer23.lights.controllers.switchbot.SwitchBotController;
import com.bigboxer23.switch_bot.IDeviceCommands;
import com.bigboxer23.switch_bot.data.Device;
import com.bigboxer23.utils.command.RetryingCommand;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * This component takes one or more sensors (switchbot) and takes an average humidity reading. It
 * will turn on or off a switchbot smart switch which should be connected to a dehumidifier based on
 * the reading and defined low/high values of RH
 */
@Slf4j
@Component
public class MultipointDehumidifierController implements InitializingBean {
	@Value("${dehumidifierId}")
	private String dehumidifierId;

	@Value("${sensorIds}")
	private String sensorIdString;

	@Value("${dehumidifierLowHumidity}")
	private float lowHumidity;

	@Value("${dehumidifierHighHumidity}")
	private float highHumidity;

	@Value("${humidifierLowHumidity}")
	private float humidifierLowHumidity;

	@Value("${humidifierHighHumidity}")
	private float humidifierHighHumidity;

	private boolean humidityMode = true;

	private List<String> sensorIds;

	private final SwitchBotController switchbotController;

	public MultipointDehumidifierController(SwitchBotController switchbotController) {
		this.switchbotController = switchbotController;
		log.info("MultipointDehumidifierController initialized");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		sensorIds = Arrays.stream(sensorIdString.split(",")).map(String::trim).toList();
	}

	@Scheduled(fixedDelay = 300000) // 5min
	private void checkHumidity() throws IOException {
		double humidity = getHumidity();
		log.info("humidity: " + humidity);
		if (humidityMode) {
			humidifierImpl(humidity);
			return;
		}
		dehumidifierImpl(humidity);
	}

	private void humidifierImpl(double humidity) throws IOException {
		if (humidity > humidifierHighHumidity && isHumidifierPowerOn()) {
			RetryingCommand.execute(
					() -> {
						switchbotController
								.getSwitchbotAPI()
								.getDeviceApi()
								.sendDeviceControlCommands(dehumidifierId, IDeviceCommands.PLUG_MINI_OFF);
						return null;
					},
					"Off " + switchbotController.getIdentifier(dehumidifierId),
					switchbotController.failureCommand(dehumidifierId));
		} else if (humidity < humidifierLowHumidity && !isHumidifierPowerOn()) {
			RetryingCommand.execute(
					() -> {
						switchbotController
								.getSwitchbotAPI()
								.getDeviceApi()
								.sendDeviceControlCommands(dehumidifierId, IDeviceCommands.PLUG_MINI_ON);
						return null;
					},
					"On " + switchbotController.getIdentifier(dehumidifierId),
					switchbotController.failureCommand(dehumidifierId));
		}
	}

	private void dehumidifierImpl(double humidity) throws IOException {
		if (humidity > highHumidity && !isHumidifierPowerOn()) {
			RetryingCommand.execute(
					() -> {
						switchbotController
								.getSwitchbotAPI()
								.getDeviceApi()
								.sendDeviceControlCommands(dehumidifierId, IDeviceCommands.PLUG_MINI_ON);
						return null;
					},
					"On " + switchbotController.getIdentifier(dehumidifierId),
					switchbotController.failureCommand(dehumidifierId));
		} else if (humidity < lowHumidity && isHumidifierPowerOn()) {
			RetryingCommand.execute(
					() -> {
						switchbotController
								.getSwitchbotAPI()
								.getDeviceApi()
								.sendDeviceControlCommands(dehumidifierId, IDeviceCommands.PLUG_MINI_OFF);
						return null;
					},
					"Off " + switchbotController.getIdentifier(dehumidifierId),
					switchbotController.failureCommand(dehumidifierId));
		}
	}

	private boolean isHumidifierPowerOn() throws IOException {
		return RetryingCommand.execute(
						() -> switchbotController
								.getSwitchbotAPI()
								.getDeviceApi()
								.getDeviceStatus(dehumidifierId),
						"Get power " + switchbotController.getIdentifier(dehumidifierId),
						switchbotController.failureCommand(dehumidifierId))
				.isPowerOn();
	}

	private double getHumidity() {
		return sensorIds.stream()
				.map(id -> {
					try {
						return RetryingCommand.execute(
								() -> switchbotController
										.getSwitchbotAPI()
										.getDeviceApi()
										.getDeviceStatus(id),
								"Get humidity " + switchbotController.getIdentifier(id),
								switchbotController.failureCommand(dehumidifierId));
					} catch (IOException e) {
						log.error("getHumidity", e);
						throw new RuntimeException(e);
					}
				})
				.map(Device::getHumidity)
				.mapToDouble(a -> a)
				.average()
				.orElse(-1);
	}
}
