
package com.androidesk.camera.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;

import com.nostra13.universalimageloader.core.download.ImageDownloader;

public class MyHttpClientDownloader extends ImageDownloader{
	private HttpClient httpClient;

	public MyHttpClientDownloader(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public MyHttpClientDownloader(Context ctx) {
		this.httpClient = Http.createHttpClient(ctx);
	}

	public String getStringFromNetwork(URI imageUri) throws IOException {
		InputStream is = getStreamFromNetwork(imageUri);
		return Stream.convertStreamToString(is);
	}

	@Override
	protected InputStream getStreamFromNetwork(URI imageUri) throws IOException {
		HttpGet httpRequest = new HttpGet(imageUri.toString());
		HttpResponse response = httpClient.execute(httpRequest);
		HttpEntity entity = response.getEntity();
		BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);
		return bufHttpEntity.getContent();
	}
}

final class Http {
	public static final int SO_TIMEOUT = 15000 * 2;
	public static final int BUFFER_SIZE = 8192;
	public static final int CONNECT_TIMEOUT = 1000 * 20;

	public static HttpClient createHttpClient(Context context) {
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, CONNECT_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, SO_TIMEOUT);
		HttpConnectionParams.setSocketBufferSize(httpParams, BUFFER_SIZE);
		HttpClientParams.setRedirecting(httpParams, true);
		httpParams.setParameter("Connection", "closed");
		/*
		 * String proxyHost = Proxy.getHost(context); int proxyPort =
		 * Proxy.getPort(context); boolean isWifiConnected =
		 * NetworkManager.isWifiConnected(context); if (!isWifiConnected &&
		 * !TextUtils.isEmpty(proxyHost)) { HttpHost proxy = new
		 * HttpHost(proxyHost, proxyPort);
		 * httpParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy); }
		 */
		HttpClient httpClient = new DefaultHttpClient(httpParams);
		return httpClient;
	}
}

final class Stream {
	public static String convertStreamToString(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}
}
