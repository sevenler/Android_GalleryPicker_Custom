
package com.androidesk.camera.gallery;

import java.util.HashMap;

import android.net.Uri;

public class CachedImageList implements IImageList {
	private static final int CACHE_CAPACITY = 512;
	private final LruCache<Integer, IImage> mCache = new LruCache<Integer, IImage>(CACHE_CAPACITY);

	public CachedImageList() {
		// TODO: We need to clear the cache because we may "reopen" the image
		// list. After we implement the image list state, we can remove this
		// kind of usage.
		mCache.clear();
	}

	protected void cache(int i, IImage img) {
		mCache.put(i, img);
	}

	protected IImage getCache(int i) {
		return mCache.get(i);
	}

	protected void invalidateCache() {
		mCache.clear();
	}

	@Override
	public HashMap<String, String> getBucketIds() {
		return null;
	}

	@Override
	public int getCount() {
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public IImage getImageAt(int i) {
		return getCache(i);
	}

	@Override
	public IImage getImageForUri(Uri uri) {
		return null;
	}

	@Override
	public boolean removeImage(IImage image) {
		return false;
	}

	@Override
	public boolean removeImageAt(int i) {
		return false;
	}

	@Override
	public int getImageIndex(IImage image) {
		return 0;
	}

	@Override
	public void close() {

	}
}
