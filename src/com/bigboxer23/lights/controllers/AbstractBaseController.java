package com.bigboxer23.lights.controllers;

import com.google.gson.GsonBuilder;
import com.bigboxer23.lights.TimeoutEnabledHttpClient;
import com.bigboxer23.lights.controllers.vera.VeraHouseVO;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;

import javax.net.ssl.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

/**
 * encapsulate common logging code
 */
public class AbstractBaseController
{
	protected static Logger myLogger = Logger.getLogger("com.jones");

	private DefaultHttpClient myHttpClient;

	private GsonBuilder myBuilder;

	protected GsonBuilder getBuilder()
	{
		if (myBuilder == null)
		{
			myBuilder = new GsonBuilder();
			myBuilder.registerTypeAdapter(VeraHouseVO.class, new VeraHouseVO());
		}
		return myBuilder;
	}

	protected DefaultHttpClient getHttpClient()
	{
		if (myHttpClient == null)
		{
			myHttpClient = new TimeoutEnabledHttpClient();
			try
			{
				SSLContext anSSLContext = SSLContext.getInstance("SSL");
				X509TrustManager aTrustManager = new X509TrustManager()
				{
					public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException { }

					public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException { }

					public X509Certificate[] getAcceptedIssuers()
					{
						return null;
					}
				};
				anSSLContext.init(null, new TrustManager[]{aTrustManager}, null);
				HostnameVerifier allHostsValid = new HostnameVerifier() {
					public boolean verify(String hostname, SSLSession session) {
						return true;
					}
				};
				HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
				HttpsURLConnection.setDefaultSSLSocketFactory(anSSLContext.getSocketFactory());
				SSLSocketFactory anSSLSockFactory = new SSLSocketFactory(anSSLContext);
				anSSLSockFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
				ClientConnectionManager aClientConnectionManager = myHttpClient.getConnectionManager();
				SchemeRegistry aSchemeRegistry = aClientConnectionManager.getSchemeRegistry();
				aSchemeRegistry.register(new Scheme("https", anSSLSockFactory, 443));
				return new DefaultHttpClient(aClientConnectionManager, myHttpClient.getParams());
			} catch (Exception ex)
			{
				myHttpClient = null;
			}
		}
		return myHttpClient;
	}
}
