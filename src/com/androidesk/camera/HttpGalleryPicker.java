
package com.androidesk.camera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;

import com.androidesk.camera.gallery.IImage;
import com.androidesk.camera.gallery.IImageList;
import com.androidesk.camera.gallery.UrlImageList;

public class HttpGalleryPicker extends GalleryPicker {
	public static final String TAG = "HttpGalleryPicker";
	public static final String MESSAGE_LOAD_THUMB = "need thumb size %sx%s, request thumb size %sx%s and response size %sx%s";

	@Override
	protected void workerRun() {
		ArrayList<Item> allItems = new ArrayList<Item>();

		checkBucketIds(allItems);
		if (mAbort) return;

		checkLowStorage();
	}

	// This is run in the worker thread.
	private void checkBucketIds(ArrayList<Item> allItems) {
		final IImageList allImages = new UrlImageList(this, UrlImageList.LOCATION_CATALOG_LIST,
				null);

		if (mAbort) {
			allImages.close();
			return;
		}

		HashMap<String, String> hashMap = allImages.getBucketIds();
		allImages.close();
		if (mAbort) return;

		for (Map.Entry<String, String> entry : hashMap.entrySet()) {
			String key = entry.getKey();
			if (key == null) {
				continue;
			}
			IImageList list = new UrlImageList(this, UrlImageList.LOCATION_CATALOG, key);
			if (mAbort) return;

			Item item = new Item(Item.TYPE_NORMAL_HTTP, key, entry.getValue(), list);

			allItems.add(item);
			checkThumbBitmap(item);

			if (mAbort) return;
			final Item finalItem = item;
			mHandler.post(new Runnable() {
				public void run() {
					updateItem(finalItem);
				}
			});
		}

		mHandler.post(new Runnable() {
			public void run() {
				checkBucketIdsFinished();
			}
		});
	}

	private Bitmap makeMiniThumbBitmapWithFourCell(int width, int height, IImageList images) {
		int count = images.getCount();
		// We draw three different version of the folder image depending on the
		// number of images in the folder.
		// For a single image, that image draws over the whole folder.
		// For two or three images, we draw the two most recent photos.
		// For four or more images, we draw four photos.
		final int padding = 4;
		int imageWidth = width;
		int imageHeight = height;
		int offsetWidth = 0;
		int offsetHeight = 0;

		imageWidth = (imageWidth - padding) / 2; // 2 here because we show two
													// images
		imageHeight = (imageHeight - padding) / 2; // per row and column

		final Paint p = new Paint();
		final Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		final Canvas c = new Canvas(b);
		final Matrix m = new Matrix();

		// draw the whole canvas as transparent
		p.setColor(0x00000000);
		c.drawPaint(p);

		// load the drawables
		loadDrawableIfNeeded();

		// draw the mask normally
		p.setColor(0xFFFFFFFF);
		mFrameGalleryMask.setBounds(0, 0, width, height);
		mFrameGalleryMask.draw(c);

		Paint pdpaint = new Paint();
		pdpaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

		pdpaint.setStyle(Paint.Style.FILL);
		c.drawRect(0, 0, width, height, pdpaint);
		
		for (int i = 0; i < 4; i++) {
			if (mAbort) {
				return null;
			}

			Bitmap temp = null;
			IImage image = i < count ? images.getImageAt(i) : null;

			final int requestWidth = (int)(imageWidth * 1.3);
			final int requestHeight = (int)(imageHeight * 1.3);
			if (image != null) {
				temp = image.thumbBitmap(requestWidth, requestHeight);
			}

			if (temp != null) {
				Log.i(TAG, String.format(MESSAGE_LOAD_THUMB, imageWidth, imageHeight, requestWidth,
						requestHeight, temp.getWidth(), temp.getHeight()));
				temp = Util.transform(m, temp, imageWidth, imageHeight, true, Util.RECYCLE_INPUT);
				placeImage(temp, c, pdpaint, imageWidth, padding, imageHeight, padding, offsetWidth,
						offsetHeight, i);
			}

			if (temp != null) {
				temp.recycle();
			}
		}

		return b;
	}

	// This is run in worker thread.
	protected Bitmap makeMiniThumbBitmap(int width, int height, IImageList images) {
		return makeMiniThumbBitmapWithFourCell(width, height, images);
	}
}
