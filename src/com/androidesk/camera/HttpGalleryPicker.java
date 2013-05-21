
package com.androidesk.camera;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.androidesk.camera.gallery.IImageList;
import com.androidesk.camera.gallery.UrlImageList;

public class HttpGalleryPicker extends GalleryPicker {

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
}
