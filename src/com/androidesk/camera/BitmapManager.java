/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.androidesk.camera;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.util.Log;

import com.androidesk.camera.gallery.BitmapCallback;
import com.androidesk.camera.gallery.ImageDecoder;
import com.androidesk.camera.network.MyHttpClientDownloader;
import com.nostra13.universalimageloader.cache.disc.DiscCacheAware;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;

/**
 * This class provides several utilities to cancel bitmap decoding. The function
 * decodeFileDescriptor() is used to decode a bitmap. During decoding if another
 * thread wants to cancel it, it calls the function cancelThreadDecoding()
 * specifying the Thread which is in decoding. cancelThreadDecoding() is sticky
 * until allowThreadDecoding() is called.
 */
public class BitmapManager {
	private static final String TAG = "BitmapManager";

	private static enum State {
		CANCEL, ALLOW
	}

	private class ThreadStatus {
		public State mState = State.ALLOW;
		private HashMap<Integer, BitmapFactory.Options> mOptions = new LinkedHashMap<Integer, BitmapFactory.Options>();
		public boolean mThumbRequesting;

		@Override
		public String toString() {
			String s;
			if (mState == State.CANCEL) {
				s = "Cancel";
			} else if (mState == State.ALLOW) {
				s = "Allow";
			} else {
				s = "?";
			}
			s = "thread state = " + s + ", options = " + mOptions;
			return s;
		}

		private static final int DEFAULT_KEY = -1;

		public void request(int key, BitmapFactory.Options options) {
			mOptions.put(key, options);
		}

		public void request(BitmapFactory.Options options) {
			mOptions.put(DEFAULT_KEY, options);
		}

		public void cacelAllRequest() {
			Iterator<BitmapFactory.Options> it = mOptions.values().iterator();
			while (it.hasNext()) {
				it.next().requestCancelDecode();
			}
			mOptions.clear();
		}
	}

	private final WeakHashMap<Thread, ThreadStatus> mThreadStatus = new WeakHashMap<Thread, ThreadStatus>();

	private static BitmapManager sManager = null;

	private BitmapManager() {
	}

	/**
	 * Get thread status and create one if specified.
	 */
	private synchronized ThreadStatus getOrCreateThreadStatus(Thread t) {
		ThreadStatus status = mThreadStatus.get(t);
		if (status == null) {
			status = new ThreadStatus();
			mThreadStatus.put(t, status);
		}
		return status;
	}

	/**
	 * The following three methods are used to keep track of
	 * BitmapFaction.Options used for decoding and cancelling.
	 */
	private synchronized void setDecodingOptions(Thread t, BitmapFactory.Options options, int key) {
		ThreadStatus status = getOrCreateThreadStatus(t);
		status.request(key, options);
	}

	private synchronized void setDecodingOptions(Thread t, BitmapFactory.Options options) {
		getOrCreateThreadStatus(t).request(options);
	}

	synchronized void removeDecodingOptions(Thread t) {
		ThreadStatus status = mThreadStatus.get(t);
		status.mOptions.clear();
	}

	/**
	 * The following three methods are used to keep track of which thread is
	 * being disabled for bitmap decoding.
	 */
	public synchronized boolean canThreadDecoding(Thread t) {
		ThreadStatus status = mThreadStatus.get(t);
		if (status == null) {
			// allow decoding by default
			return true;
		}

		boolean result = (status.mState != State.CANCEL);
		return result;
	}

	public synchronized void allowThreadDecoding(Thread t) {
		getOrCreateThreadStatus(t).mState = State.ALLOW;
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	public synchronized void cancelThreadDecoding(Thread t, ContentResolver cr) {
		ThreadStatus status = getOrCreateThreadStatus(t);
		status.mState = State.CANCEL;

		status.cacelAllRequest();

		// Wake up threads in waiting list
		notifyAll();

		// Since our cancel request can arrive MediaProvider earlier than
		// getThumbnail request,
		// we use mThumbRequesting flag to make sure our request does cancel the
		// request.
		try {
			synchronized (status) {
				while (status.mThumbRequesting) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
						Images.Thumbnails.cancelThumbnailRequest(cr, -1, t.getId());
						Video.Thumbnails.cancelThumbnailRequest(cr, -1, t.getId());
					}
					status.wait(200);
				}
			}
		} catch (InterruptedException ex) {
			// ignore it.
		}
	}

	public Bitmap getThumbnail(ContentResolver cr, long origId, int kind,
			BitmapFactory.Options options, boolean isVideo) {
		Thread t = Thread.currentThread();
		ThreadStatus status = getOrCreateThreadStatus(t);

		if (!canThreadDecoding(t)) {
			Log.d(TAG, "Thread " + t + " is not allowed to decode.");
			return null;
		}

		try {
			synchronized (status) {
				status.mThumbRequesting = true;
			}
			if (isVideo) {
				return Video.Thumbnails.getThumbnail(cr, origId, t.getId(), kind, null);
			} else {
				return Images.Thumbnails.getThumbnail(cr, origId, t.getId(), kind, null);
			}
		} finally {
			synchronized (status) {
				status.mThumbRequesting = false;
				status.notifyAll();
			}
		}
	}

	public static synchronized BitmapManager instance() {
		if (sManager == null) {
			sManager = new BitmapManager();
		}
		return sManager;
	}

	/**
	 * The real place to delegate bitmap decoding to BitmapFactory.
	 */
	public Bitmap decodeFileDescriptor(FileDescriptor fd, BitmapFactory.Options options) {
		if (options.mCancel) {
			return null;
		}

		Thread thread = Thread.currentThread();
		if (!canThreadDecoding(thread)) {
			Log.d(TAG, "Thread " + thread + " is not allowed to decode.");
			return null;
		}

		setDecodingOptions(thread, options);
		Bitmap b = BitmapFactory.decodeFileDescriptor(fd, null, options);

		removeDecodingOptions(thread);
		return b;
	}

	private final Executor mDecodeExecutor = CacheManager.instance().getDecodeExecutor();
	private final DiscCacheAware mDiscCache = CacheManager.instance().getDiscCache();
	private final File mDiscCacheDir = CacheManager.instance().getDiscCacheDir();

	private static final String URI_FORMAT = "%s_%sx%s";
	private static final String LOAD_FROM_CACHE = "load %s from disc cache %s";
	private static final String LOAD_FROM_NETWORK = "load %s from with new url decode";
	private static final String CACHE_IT = "cache %s to disc %s";

	private static String generateLoadKey(URI uri, int width, int height) {
		return String.format(URI_FORMAT, uri.toString(), width, height);
	}

	public Bitmap decodeNetworkUri(final URI uri, final BitmapFactory.Options options,
			final MyHttpClientDownloader imageDownloader, final BitmapCallback callback) {
		return decodeNetworkUri(uri, null, options, imageDownloader, callback);
	}

	public Bitmap decodeNetworkUri(final URI uri, final ImageSize imageSize,
			final BitmapFactory.Options options, final MyHttpClientDownloader imageDownloader,
			final BitmapCallback callback) {
		if (options.mCancel) return null;

		final Thread thread = Thread.currentThread();
		if (!canThreadDecoding(thread)) {
			Log.d(TAG, "Thread " + thread + " is not allowed to decode.");
			return null;
		}

		final int width = ((imageSize == null) ? 0 : imageSize.getWidth());
		final int height = ((imageSize == null) ? 0 : imageSize.getHeight());
		final String cacheKey = generateLoadKey(uri, width, height);
		setDecodingOptions(thread, options, cacheKey.hashCode());

		if (options.mCancel) return null;
		final Bitmap[] result = new Bitmap[1];

		Runnable run = new Runnable() {
			@Override
			public void run() {
				ImageDecoder decoder;

				File discCache = mDiscCache.get(cacheKey);
				boolean isCached = discCache.exists();
				if (isCached) {// 从缓存中加载
					decoder = new ImageDecoder(URI.create("file://" + discCache.getAbsolutePath()),
							imageDownloader);
					Log.i(TAG, String.format(LOAD_FROM_CACHE, uri.toString(), discCache.getName()));
				} else {
					decoder = new ImageDecoder(uri, imageDownloader);
					Log.i(TAG, String.format(LOAD_FROM_NETWORK, uri.toString()));
				}

				try {
					result[0] = decoder.decode(imageSize, options,
							ImageScaleType.IN_SAMPLE_POWER_OF_2);
				} catch (IOException e) {
					e.printStackTrace();
				}

				final Bitmap bitmap = result[0];
				if (!isCached && (bitmap != null)) {
					File file = new File(mDiscCacheDir.getAbsolutePath(), discCache.getName());
					try {
						file.createNewFile();
						FileOutputStream outputStream = new FileOutputStream(file);
						bitmap.compress(CompressFormat.JPEG, 75, outputStream);
					} catch (IOException e) {
						e.printStackTrace();
					}
					mDiscCache.put(cacheKey, file);
					Log.i(TAG, String.format(CACHE_IT, uri.toString(), discCache.getName()));
				}

				removeDecodingOptions(thread);
				if (callback != null) {
					callback.setRotateBitmap(new RotateBitmap(bitmap));
					callback.submit();
				}
			}
		};

		if (callback != null) mDecodeExecutor.execute(run);// 异步加载
		else run.run();
		return result[0];
	}

}
