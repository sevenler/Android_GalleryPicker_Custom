
package com.androidesk.camera.gallery;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.androidesk.camera.network.MyHttpClientDownloader;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

public class UrlImage extends BaseImage implements IImage {
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private final Context mContext;
	private IImageList mContainer;

	private ImageDownloader imageDownloader;
	private static final String URL_WALLPAPER = "http://static.androidesk.com/wallpaper?imgid=%s&reso=%sx%s";
	protected DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory()
			.cacheOnDisc().build();

	public UrlImage(Context ctx, IImageList container, int index, String title) {
		super(null, null, index, index, Uri.parse(toPath(title)), toPath(title).toString(),
				"image/jpeg", 0, title);
		mContext = ctx;
		mContainer = container;

		imageDownloader = new MyHttpClientDownloader(mContext);
	}

	private static String toPath(String id) {
		return toPath(id, THUMBNAIL_MAX_NUM_PIXELS_WIDTH, THUMBNAIL_MAX_NUM_PIXELS_HEIGHT);
	}

	private static String toPath(String id, int width, int height) {
		return String.format(URL_WALLPAPER, id, width, height);
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
		return decode(new ImageSize(THUMBNAIL_MAX_NUM_PIXELS_WIDTH, THUMBNAIL_MAX_NUM_PIXELS_HEIGHT));
	}

	private Bitmap decode(ImageSize imageSize) {
		Bitmap result = null;
		ImageDecoder decoder = new ImageDecoder(URI.create(toPath(getTitle(), imageSize.getWidth(),
				imageSize.getHeight())), imageDownloader);
		try {
			result = decoder.decode(imageSize, ImageScaleType.IN_SAMPLE_POWER_OF_2);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public Bitmap miniThumbBitmap() {
		Bitmap bit = decode(new ImageSize(MINI_THUMB_MAX_NUM_PIXELS_WIDTH,
				MINI_THUMB_MAX_NUM_PIXELS_HEIGHT));
		return bit;
	}

	@Override
	public InputStream fullSizeImageData() {
		try {
			InputStream input = imageDownloader.getStream(URI.create(mUri.toString()));
			return input;
		} catch (IOException ex) {
			return null;
		}
	}

	public Bitmap fullSizeBitmap(int minSideLength, int maxNumberOfPixels, boolean rotateAsNeeded,
			boolean useNative) {
		return decode(new ImageSize(1240, 1080));
	}

	public Bitmap fullSizeBitmap(int minSideLength, int maxNumberOfPixels, boolean rotateAsNeeded) {
		return fullSizeBitmap(minSideLength, maxNumberOfPixels, rotateAsNeeded, IImage.NO_NATIVE);
	}
}
