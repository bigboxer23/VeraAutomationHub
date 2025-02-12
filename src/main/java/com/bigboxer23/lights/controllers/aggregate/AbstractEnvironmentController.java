package com.bigboxer23.lights.controllers.aggregate;

import com.bigboxer23.lights.controllers.EmailController;
import com.bigboxer23.lights.controllers.switchbot.SwitchBotController;
import com.bigboxer23.switch_bot.IDeviceCommands;
import com.bigboxer23.switch_bot.data.Device;
import com.bigboxer23.utils.command.RetryingCommand;
import com.bigboxer23.utils.file.FilePersistedBoolean;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

/** */
@Slf4j
public abstract class AbstractEnvironmentController {
	private final SwitchBotController switchBotController;

	private final EmailController emailController;

	private final FilePersistedBoolean disabled;

	private final EnvironmentCluster cluster;

	public AbstractEnvironmentController(
			SwitchBotController controller, EmailController emailController, String heaterMap) {
		switchBotController = controller;
		this.emailController = emailController;
		cluster = new GsonBuilder().create().fromJson(heaterMap, EnvironmentCluster.class);
		disabled = new FilePersistedBoolean("AbstractEnvironmentController" + cluster.getSwitchId());
	}

	public abstract float getEnvironmentValue(SwitchBotController controller, String deviceId) throws IOException;

	public void run() throws IOException {
		if (disabled.get()) {
			return;
		}
		double averageEnvFactor = cluster.getEnvironmentDevices().stream()
				.map(deviceId -> {
					try {
						return RetryingCommand.execute(
								() -> getEnvironmentValue(switchBotController, deviceId),
								"Get env: " + switchBotController.getIdentifier(deviceId),
								switchBotController.failureCommand(deviceId));
					} catch (IOException e) {
						log.error("AbstractEnvironmentController: ", e);
					}
					return -1f;
				})
				.filter(temperature -> temperature > 0)
				.mapToInt(Float::intValue)
				.average()
				.orElse(0.0);
		Device device = RetryingCommand.execute(
				() -> switchBotController.getSwitchbotAPI().getDeviceApi().getDeviceStatus(cluster.getSwitchId()),
				"power on " + switchBotController.getIdentifier(cluster.getSwitchId()),
				switchBotController.failureCommand(cluster.getSwitchId()));
		boolean shouldTurnOff = device.isPowerOn()
				&& ((!cluster.isDehumidifier() && averageEnvFactor > cluster.getHigh())
						|| (cluster.isDehumidifier() && averageEnvFactor < cluster.getLow()));

		boolean shouldTurnOn = !device.isPowerOn()
				&& ((!cluster.isDehumidifier() && averageEnvFactor < cluster.getLow())
						|| (cluster.isDehumidifier() && averageEnvFactor > cluster.getHigh()));
		log.info(switchBotController.getIdentifier(cluster.getSwitchId())
				+ " "
				+ averageEnvFactor
				+ " "
				+ cluster.getHigh()
				+ " "
				+ cluster.getLow()
				+ " "
				+ shouldTurnOff
				+ " "
				+ shouldTurnOn);
		if (!cluster.isDehumidifier()
				&& averageEnvFactor < cluster.getLow()
				&& device.isPowerOn()
				&& device.getWatts() < 5) {
			emailController.sendMessageThrottled(
					device.getDeviceId(), switchBotController.getIdentifier(device.getDeviceId()));
		}
		if (shouldTurnOff || shouldTurnOn) {
			String action = shouldTurnOff ? "turn off " : "turn on ";
			RetryingCommand.execute(
					() -> switchBotController
							.getSwitchbotAPI()
							.getDeviceApi()
							.sendDeviceControlCommands(
									cluster.getSwitchId(),
									shouldTurnOff ? IDeviceCommands.PLUG_MINI_OFF : IDeviceCommands.PLUG_MINI_ON),
					action + switchBotController.getIdentifier(cluster.getSwitchId()),
					switchBotController.failureCommand(cluster.getSwitchId()));
		}
	}

	public void disable(boolean enabled) {
		disabled.set(enabled);
	}
}
