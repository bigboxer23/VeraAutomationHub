package com.bigboxer23.lights.controllers;

import com.bigboxer23.utils.mail.MailSender;
import com.bigboxer23.utils.time.ITimeConstants;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

/** */
@Slf4j
@Controller
public class EmailController {
	@Value("${toEmail}")
	private String toEmail;

	@Value("${fromEmail}")
	private String fromEmail;

	@Value("${fromEmailPassword}")
	private String fromPassword;

	private final Map<String, Long> events = new HashMap<>();
	private final Map<String, Long> mailSent = new HashMap<>();

	public boolean sendMessageThrottled(String deviceId, String deviceName) {
		return sendMessageThrottled(
				deviceId,
				deviceName,
				"%s reservoir may be empty",
				"Reservoir for %s may be empty, please check & fill.");
	}

	public boolean sendMessageThrottled(
			String deviceId, String deviceName, String subjectTemplate, String bodyTemplate) {
		Long lastEvent = events.getOrDefault(deviceId, Long.MIN_VALUE);
		events.put(deviceId, System.currentTimeMillis() + ITimeConstants.FIFTEEN_MINUTES);
		boolean isRecent = System.currentTimeMillis() <= lastEvent;
		if (isRecent) {
			log.info(deviceName + ":" + deviceId + " Event recent " + lastEvent + ":" + System.currentTimeMillis());
			Long lastMailEvent = mailSent.getOrDefault(deviceId, Long.MIN_VALUE);
			mailSent.put(deviceId, System.currentTimeMillis() + ITimeConstants.HOUR);
			if (System.currentTimeMillis() > lastMailEvent) {
				MailSender.sendGmail(
						toEmail,
						fromEmail,
						fromPassword,
						String.format(subjectTemplate, deviceName),
						String.format(bodyTemplate, deviceName),
						null);
			}
		}
		return isRecent;
	}
}
