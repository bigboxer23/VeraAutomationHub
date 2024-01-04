package com.bigboxer23.lights.controllers.hue;

import com.bigboxer23.lights.controllers.scene.DaylightController;
import com.bigboxer23.utils.http.OkHttpUtil;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** */
public class HueCompatibleClient {
	private static final Logger logger = LoggerFactory.getLogger(HueCompatibleClient.class);
	private static OkHttpClient instance;

	public static OkHttpClient getClient() {
		if (instance == null) {
			try {
				TrustManager[] trustAllCerts = new TrustManager[] {
					new X509TrustManager() {
						@Override
						public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}

						@Override
						public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}

						@Override
						public java.security.cert.X509Certificate[] getAcceptedIssuers() {
							return new java.security.cert.X509Certificate[] {};
						}
					}
				};
				SSLContext sslContext = SSLContext.getInstance("SSL");
				sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
				instance = OkHttpUtil.getBuilder()
						.hostnameVerifier((hostname, session) -> true)
						.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
						.build();
			} catch (NoSuchAlgorithmException | KeyManagementException e) {
				logger.warn("getInstance", e);
				throw new RuntimeException(e);
			}
		}
		return instance;
	}
}
