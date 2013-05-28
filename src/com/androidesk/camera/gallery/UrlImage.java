
package com.androidesk.camera.gallery;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.androidesk.camera.BitmapManager;
import com.androidesk.camera.DisplayManager;
import com.androidesk.camera.network.ImageDownloader;
import com.androidesk.camera.network.MyHttpClientDownloader;
import com.androidesk.camera.network.source.SourceManager;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;

public class UrlImage extends BaseImage implements IImage {
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private final Context mContext;
	private IImageList mContainer;

	private BitmapCallback mCallback;
	private final String mUrl;

	private static int FULL_PIXELS_WIDTH = DisplayManager.instance().getDesiredWidth();
	private static int FULL_PIXELS_HEIGHT = DisplayManager.instance().getDesiredHeight();

	private ImageDownloader imageDownloader;

	private BitmapFactory.Options mOptions = new BitmapFactory.Options();

	public UrlImage(Context ctx, IImageList container, int index, String id, String url) {
		super(null, null, index, index, Uri.parse(toPath(url)), toPath(url).toString(),
				"image/jpeg", 0, id);
		mContext = ctx;
		mContainer = container;
		mUrl = url;

		imageDownloader = new MyHttpClientDownloader(mContext);
		mOptions.inPreferredConfig = Bitmap.Config.RGB_565;
	}
	
	@Override
	public String getTitle() {
		return mTitle;
	}

	private static String toPath(String url) {
		return toPath(url, FULL_PIXELS_WIDTH, FULL_PIXELS_HEIGHT);
	}

	private static String toPath(String url, int width, int height) {
		return SourceManager.instance().generateDefaultGetPictureUrl(url, width, height);
	}

	public IImageList getContainer() {
		return mContainer;
	}

	@Override
	public boolean isReadonly() {
		return true;
	}

	@Override
	public boolean isDrm() {
		return false;
	}

	@Override
	public boolean rotateImageBy(int degrees) {
		return false;
	}

	@Override
	public Bitmap thumbBitmap(boolean rotateAsNeeded) {
		return decode(THUMBNAIL_MAX_NUM_PIXELS_WIDTH, THUMBNAIL_MAX_NUM_PIXELS_HEIGHT,
				getBitmapLoadedCallback());
	}

	@Override
	public Bitmap miniThumbBitmap() {
		Bitmap bit = decode(MINI_THUMB_MAX_NUM_PIXELS_WIDTH, MINI_THUMB_MAX_NUM_PIXELS_HEIGHT,
				getBitmapLoadedCallback());
		return bit;
	}

	@Override
	public Bitmap thumbBitmap(int width, int height) {
		return decode(width, height, getBitmapLoadedCallback());
	}

	private Bitmap decode(int width, int height, BitmapCallback callback) {
		URI uri = URI.create(SourceManager.instance().generateDefaultGetPictureUrl(mUrl, width, height));
		return BitmapManager.instance().decodeBitmap(uri, new ImageSize(width, height), mOptions,
				imageDownloader, callback);
	}

	@Override
	public BitmapCallback getBitmapLoadedCallback() {
		return mCallback;
	}

	@Override
	public void setBitmapLoadedCallback(BitmapCallback callback) {
		mCallback = callback;
	}

	@Override
	public InputStream fullSizeImageData() {
		try {
			InputStream input = imageDownloader.getStream(URI.create(mUri.toString()), mOptions);
			return input;
		} catch (IOException ex) {
			return null;
		}
	}

	public Bitmap fullSizeBitmap(int minSideLength, int maxNumberOfPixels, boolean rotateAsNeeded,
			boolean useNative) {
		return decode(1240, 1080, getBitmapLoadedCallback());
	}

	public Bitmap fullSizeBitmap(int minSideLength, int maxNumberOfPixels, boolean rotateAsNeeded) {
		return fullSizeBitmap(minSideLength, maxNumberOfPixels, rotateAsNeeded, IImage.NO_NATIVE);
	}

	public int getWidth() {
		if (mWidth == UNKNOWN_LENGTH) setupDimension();
		return mWidth;
	}

	public int getHeight() {
		if (mHeight == UNKNOWN_LENGTH) setupDimension();
		return mHeight;
	}

	private void setupDimension() {
		mWidth = mOptions.outWidth;
		mHeight = mOptions.outHeight;
	}
}
