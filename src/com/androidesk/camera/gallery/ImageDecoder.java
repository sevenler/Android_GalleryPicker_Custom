
package com.androidesk.camera.gallery;

import java.io.IOException;
import java.net.URI;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

import com.androidesk.camera.network.ImageDownloader;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.utils.L;

/**
 * Decodes images to {@link Bitmap}, scales them to needed size
 * 
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @see ImageScaleType
 * @see ViewScaleType
 * @see ImageDownloader
 * @see DisplayImageOptions
 */
public class ImageDecoder {

	private static final String LOG_IMAGE_SUBSAMPLED = "Original image (%1$dx%2$d) is going to be subsampled to %3$dx%4$d view. Computed scale size - %5$d";
	private static final String LOG_IMAGE_SCALED = "Subsampled image (%1$dx%2$d) was scaled to %3$dx%4$d";

	private final URI imageUri;
	private final ImageDownloader imageDownloader;

	private boolean loggingEnabled;

	private byte[] mBytes;// decode bounds的时候将图片的byte缓存起来
							// 正式decode的时候直接使用这里的byte缓存来decode

	/**
	 * @param imageUri Image URI (<b>i.e.:</b> "http://site.com/image.png",
	 *            "file:///mnt/sdcard/image.png")
	 * @param imageDownloader Image downloader
	 */
	public ImageDecoder(URI imageUri, ImageDownloader imageDownloader) {
		this.imageUri = imageUri;
		this.imageDownloader = imageDownloader;
	}

	/**
	 * Decodes image from URI into {@link Bitmap}. Image is scaled close to
	 * incoming {@link ImageSize image size} during decoding (depend on incoming
	 * image scale type).
	 * 
	 * @param targetSize Image size to scale to during decoding
	 * @param scaleType {@link ImageScaleType Image scale type}
	 * @return Decoded bitmap
	 * @throws IOException
	 */
	public Bitmap decode(ImageSize targetSize, Options decodeOptions, ImageScaleType scaleType)
			throws IOException {
		return decode(targetSize, decodeOptions, scaleType, ViewScaleType.FIT_INSIDE);
	}

	/**
	 * Decodes image from URI into {@link Bitmap}. Image is scaled close to
	 * incoming {@link ImageSize image size} during decoding (depend on incoming
	 * image scale type).
	 * 
	 * @param targetSize Image size to scale to during decoding
	 * @param scaleType {@link ImageScaleType Image scale type}
	 * @param viewScaleType {@link ViewScaleType View scale type}
	 * @return Decoded bitmap
	 * @throws IOException
	 */
	public Bitmap decode(ImageSize targetSize, Options decodeOptions, ImageScaleType scaleType,
			ViewScaleType viewScaleType) throws IOException {
		if (decodeOptions.mCancel) return null;
		decodeOptions = getBitmapOptionsForImageDecoding(targetSize, scaleType, viewScaleType,
				decodeOptions);
		if(targetSize == null) targetSize = new ImageSize(decodeOptions.outWidth, decodeOptions.outHeight);

		if (decodeOptions.mCancel) return null;

		if (mBytes == null) return null;
		decodeOptions.inJustDecodeBounds = false;
		Bitmap subsampledBitmap = BitmapFactory.decodeByteArray(mBytes, 0, mBytes.length,
				decodeOptions);

		if (subsampledBitmap == null) {
			return null;
		}

		// Scale to exact size if need
		if (scaleType == ImageScaleType.EXACTLY || scaleType == ImageScaleType.EXACTLY_STRETCHED) {
			subsampledBitmap = scaleImageExactly(subsampledBitmap, targetSize, scaleType,
					viewScaleType);
		}

		return subsampledBitmap;
	}

	private Options getBitmapOptionsForImageDecoding(ImageSize targetSize,
			ImageScaleType scaleType, ViewScaleType viewScaleType, Options decodeOptions)
			throws IOException {
		if (decodeOptions == null) decodeOptions = new Options();
		decodeOptions.inSampleSize = computeImageScale(targetSize, scaleType, viewScaleType,
				decodeOptions);
		return decodeOptions;
	}

	@SuppressWarnings("deprecation")
	private int computeImageScale(ImageSize targetSize, ImageScaleType scaleType,
			ViewScaleType viewScaleType, Options options) throws IOException {

		// decode image size
		options.inJustDecodeBounds = true;
		mBytes = imageDownloader.getByteArray(imageUri, options);
		if (mBytes == null) return options.inSampleSize;
		BitmapFactory.decodeByteArray(mBytes, 0, mBytes.length, options);

		int scale = 1;
		int imageWidth = options.outWidth;
		int imageHeight = options.outHeight;
		int targetWidth = ((targetSize == null) ? imageWidth : targetSize.getWidth());
		int targetHeight = ((targetSize == null) ? imageHeight : targetSize.getHeight());
		int widthScale = imageWidth / targetWidth;
		int heightScale = imageHeight / targetHeight;

		if (viewScaleType == ViewScaleType.FIT_INSIDE) {
			if (scaleType == ImageScaleType.IN_SAMPLE_POWER_OF_2
					|| scaleType == ImageScaleType.POWER_OF_2) {
				while (imageWidth / 2 >= targetWidth || imageHeight / 2 >= targetHeight) { // ||
					imageWidth /= 2;
					imageHeight /= 2;
					scale *= 2;
				}
			} else {
				scale = Math.max(widthScale, heightScale); // max
			}
		} else { // ViewScaleType.CROP
			if (scaleType == ImageScaleType.IN_SAMPLE_POWER_OF_2
					|| scaleType == ImageScaleType.POWER_OF_2) {
				while (imageWidth / 2 >= targetWidth && imageHeight / 2 >= targetHeight) { // &&
					imageWidth /= 2;
					imageHeight /= 2;
					scale *= 2;
				}
			} else {
				scale = Math.min(widthScale, heightScale); // min
			}
		}

		if (scale < 1) {
			scale = 1;
		}

		if (loggingEnabled)
			L.d(LOG_IMAGE_SUBSAMPLED, imageWidth, imageHeight, targetWidth, targetHeight, scale);
		return scale;
	}

	private Bitmap scaleImageExactly(Bitmap subsampledBitmap, ImageSize targetSize,
			ImageScaleType scaleType, ViewScaleType viewScaleType) {
		float srcWidth = subsampledBitmap.getWidth();
		float srcHeight = subsampledBitmap.getHeight();

		float widthScale = srcWidth / targetSize.getWidth();
		float heightScale = srcHeight / targetSize.getHeight();

		int destWidth;
		int destHeight;
		if ((viewScaleType == ViewScaleType.FIT_INSIDE && widthScale >= heightScale)
				|| (viewScaleType == ViewScaleType.CROP && widthScale < heightScale)) {
			destWidth = targetSize.getWidth();
			destHeight = (int)(srcHeight / widthScale);
		} else {
			destWidth = (int)(srcWidth / heightScale);
			destHeight = targetSize.getHeight();
		}

		Bitmap scaledBitmap;
		if ((scaleType == ImageScaleType.EXACTLY && destWidth < srcWidth && destHeight < srcHeight)
				|| (scaleType == ImageScaleType.EXACTLY_STRETCHED && destWidth != srcWidth && destHeight != srcHeight)) {
			scaledBitmap = Bitmap.createScaledBitmap(subsampledBitmap, destWidth, destHeight, true);
			if (scaledBitmap != subsampledBitmap) {
				subsampledBitmap.recycle();
			}
			if (loggingEnabled)
				L.d(LOG_IMAGE_SCALED, (int)srcWidth, (int)srcHeight, destWidth, destHeight);
		} else {
			scaledBitmap = subsampledBitmap;
		}

		return scaledBitmap;
	}

	void setLoggingEnabled(boolean loggingEnabled) {
		this.loggingEnabled = loggingEnabled;
	}
}
