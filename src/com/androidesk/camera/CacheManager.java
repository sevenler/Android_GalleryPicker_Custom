
package com.androidesk.camera;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;

import com.nostra13.universalimageloader.cache.disc.DiscCacheAware;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class CacheManager {
	private static CacheManager instance = null;

	public static CacheManager instance() {
		if (instance == null) {
			synchronized (CacheManager.class) {
				if (instance == null) {
					instance = new CacheManager();
				}
			}
		}
		return instance;
	}

	private CacheManager() {
	}

	private DiscCacheAware mDiscCache;
	private File mDiscCacheDir;

	public static final int DEFAULT_THREAD_POOL_SIZE = 5;
	public static final int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY - 1;
	
	private static final String DEFAULT_THUMB_DIRACTROY = "thumb";

	private static class DefaultThreadFactory implements ThreadFactory {
		private static final AtomicInteger poolNumber = new AtomicInteger(1);

		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;
		private final int threadPriority;

		DefaultThreadFactory(int threadPriority) {
			this.threadPriority = threadPriority;
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			namePrefix = "pool-" + poolNumber.getAndIncrement() + "-thread-";
		}

		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
			if (t.isDaemon()) t.setDaemon(false);
			t.setPriority(threadPriority);
			return t;
		}
	}

	public void initialize(Context context) {
		if (mDiscCache != null) return;
		mDiscCacheDir = new File(StorageUtils.getCacheDirectory(context), DEFAULT_THUMB_DIRACTROY);
		if(!mDiscCacheDir.exists()) mDiscCacheDir.mkdirs();
		mDiscCache = new UnlimitedDiscCache(mDiscCacheDir, new HashCodeFileNameGenerator());
	}

	private void checkInitialized() {
		if (mDiscCache == null)
			throw new IllegalArgumentException("CacheManager not being initialized");
	}

	public DiscCacheAware getDiscCache() {
		checkInitialized();
		return mDiscCache;
	}
	
	public File getDiscCacheDir() {
		checkInitialized();
		return mDiscCacheDir;
	}

	public Executor getDecodeExecutor() {
		return new ThreadPoolExecutor(DEFAULT_THREAD_POOL_SIZE, DEFAULT_THREAD_POOL_SIZE, 0L,
				TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
				new DefaultThreadFactory(DEFAULT_THREAD_PRIORITY));
	}
}
