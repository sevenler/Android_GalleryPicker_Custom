
package com.androidesk.camera.network;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

/**
 * Provides retrieving of {@link InputStream} of image by URI.
 * 
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public abstract class ImageDownloader {

	protected static final String PROTOCOL_FILE = "file";

	protected static final String PROTOCOL_HTTP = "http";
	protected static final String PROTOCOL_HTTPS = "https";
	protected static final String PROTOCOL_FTP = "ftp";

	protected static final int BUFFER_SIZE = 8 * 1024; // 8 Kb

	public byte[] getByteArray(URI imageUri, Options options) throws IOException {
		InputStream in = getStream(imageUri, options);
		if (in == null) return null;

		byte[] bytes = Stream.toByteArray(in, options);
		return bytes;
	}

	/**
	 * Retrieves {@link InputStream} of image by URI. Image can be located as in
	 * the network and on local file system.
	 */
	public InputStream getStream(URI imageUri, Options options) throws IOException {
		String scheme = imageUri.getScheme();
		if (PROTOCOL_HTTP.equals(scheme) || PROTOCOL_HTTPS.equals(scheme)
				|| PROTOCOL_FTP.equals(scheme)) {
			return getStreamFromNetwork(imageUri, options);
		} else if (PROTOCOL_FILE.equals(scheme)) {
			return getStreamFromFile(imageUri, options);
		} else {
			return getStreamFromOtherSource(imageUri, options);
		}
	}

	public String getString(URI imageUri) throws IOException {
		InputStream is = getStream(imageUri, new BitmapFactory.Options());
		return Stream.convertStreamToString(is);
	}

	/**
	 * Retrieves {@link InputStream} of image by URI from other source. Should
	 * be overriden by successors to implement image downloading from special
	 * sources (not local file and not web URL).
	 */
	protected InputStream getStreamFromOtherSource(URI imageUri, Options options)
			throws IOException {
		return null;
	}

	/**
	 * Retrieves {@link InputStream} of image by URI (image is located in the
	 * network)
	 */
	protected abstract InputStream getStreamFromNetwork(URI imageUri, Options options)
			throws IOException;

	/**
	 * Retrieves {@link InputStream} of image by URI (image is located on the
	 * local file system or SD card)
	 */
	protected InputStream getStreamFromFile(URI imageUri, Options options) throws IOException {
		return new BufferedInputStream(imageUri.toURL().openStream(), BUFFER_SIZE);
	}
}
