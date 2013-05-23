
package com.androidesk.camera.network;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.ByteArrayBuffer;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;


public class MyHttpClientDownloader {
	private HttpClient httpClient;

	public MyHttpClientDownloader(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	public MyHttpClientDownloader(Context ctx) {
		this.httpClient = Http.createHttpClient(ctx);
	}

	public String getStringFromNetwork(URI imageUri) throws IOException {
		InputStream is = getStreamFromNetwork(imageUri, new BitmapFactory.Options());
		return Stream.convertStreamToString(is);
	}

	public InputStream getStreamFromNetwork(URI imageUri, Options options) throws IOException {
		if(options.mCancel) return null;
		HttpGet httpRequest = new HttpGet(imageUri.toString());
		HttpResponse response = httpClient.execute(httpRequest);
		
		HttpEntity entity = response.getEntity();
		if(options.mCancel){
			httpRequest.abort();
			return null;
		}
		long begin = System.currentTimeMillis();
		byte[] bytes = Stream.toByteArray(entity, options);
		if(bytes == null) return null;
		System.out.println(String.format(" ======== get stream begin %s %s %s", imageUri,options, options.mCancel));
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		long end = System.currentTimeMillis();
		System.out.println(String.format("  ========= get stream begin  %s %s  %s %s============", imageUri, (end - begin),options, options.mCancel));
		return bis;
	}
	
	public byte[] getByteArrayFromNetwork(URI imageUri, Options options) throws IOException {
		if(options.mCancel) return null;
		HttpGet httpRequest = new HttpGet(imageUri.toString());
		HttpResponse response = httpClient.execute(httpRequest);
		
		HttpEntity entity = response.getEntity();
		if(options.mCancel){
			httpRequest.abort();
			return null;
		}
		byte[] bytes = Stream.toByteArray(entity, options);
		return bytes;
	}

	public void readStreamFromEntity(HttpEntity entity) {

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

	public static byte[] toByteArray(final HttpEntity entity, Options options) throws IOException {
		if (entity == null) {
			throw new IllegalArgumentException("HTTP entity may not be null");
		}
		if(options.mCancel) return null;
		InputStream instream = entity.getContent();
		if (instream == null) {
			return null;
		}
		try {
			if (entity.getContentLength() > Integer.MAX_VALUE) {
				throw new IllegalArgumentException("HTTP entity too large to be buffered in memory");
			}
			if(options.mCancel) return null;
			int i = (int)entity.getContentLength();
			if (i < 0) {
				i = 4096;
			}
			if(options.mCancel) return null;
			ByteArrayBuffer buffer = new ByteArrayBuffer(i);
			byte[] tmp = new byte[4096];
			int l;
			while ((l = instream.read(tmp)) != -1) {
				if(options.mCancel) return null;
				buffer.append(tmp, 0, l);
			}
			return buffer.toByteArray();
		} finally {
			instream.close();
		}
	}
}
