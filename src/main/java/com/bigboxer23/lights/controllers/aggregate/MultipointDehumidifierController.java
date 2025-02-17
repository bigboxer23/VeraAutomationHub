package com.bigboxer23.lights.controllers.aggregate;

import com.bigboxer23.lights.controllers.switchbot.SwitchBotController;
import com.bigboxer23.switch_bot.IDeviceCommands;
import com.bigboxer23.switch_bot.data.Device;
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
			switchbotController.sendDeviceControlCommands(dehumidifierId, IDeviceCommands.PLUG_MINI_OFF);
		} else if (humidity < humidifierLowHumidity && !isHumidifierPowerOn()) {
			switchbotController.sendDeviceControlCommands(dehumidifierId, IDeviceCommands.PLUG_MINI_ON);
		}
	}

	private void dehumidifierImpl(double humidity) throws IOException {
		if (humidity > highHumidity && !isHumidifierPowerOn()) {
			switchbotController.sendDeviceControlCommands(dehumidifierId, IDeviceCommands.PLUG_MINI_ON);
		} else if (humidity < lowHumidity && isHumidifierPowerOn()) {
			switchbotController.sendDeviceControlCommands(dehumidifierId, IDeviceCommands.PLUG_MINI_OFF);
		}
	}

	private boolean isHumidifierPowerOn() throws IOException {
		return switchbotController.getDeviceStatus(dehumidifierId).isPowerOn();
	}

	private double getHumidity() {
		return sensorIds.stream()
				.map(id -> {
					try {
						return switchbotController.getDeviceStatus(id);
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
