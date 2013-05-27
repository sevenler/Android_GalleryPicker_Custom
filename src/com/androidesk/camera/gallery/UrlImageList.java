
package com.androidesk.camera.gallery;

import java.io.IOException;
import java.util.HashMap;

import android.content.Context;
import android.net.Uri;

import com.androidesk.camera.network.MyHttpClientDownloader;
import com.androidesk.camera.network.source.BaseCatalog;
import com.androidesk.camera.network.source.BaseCatalogList;
import com.androidesk.camera.network.source.BasePicture;
import com.androidesk.camera.network.source.SourceManager;

public class UrlImageList extends CachedImageList {
	private final int mLocation;
	private BaseCatalogList mCatalogs;

	private BaseCatalog mPictures;
	private final Context mContext;

	private final String mCatalog;
	private int mSize;
	private final SourceManager mSourceManager;

	public static final int LOCATION_CATALOG_LIST = 1;
	public static final int LOCATION_CATALOG = 2;

	public UrlImageList(Context ctx, int location, String catalog) {
		this(ctx, location, catalog, 30);
	}

	public UrlImageList(Context ctx, int location, String catalog, int size) {
		super();
		mContext = ctx;
		mLocation = location;

		mCatalog = catalog;
		mSize = size;

		mSourceManager = SourceManager.instance().initialize(new MyHttpClientDownloader(ctx));
	}

	private void initPictureIfNeed() {
		if (mPictures == null) {
			getPictureList();
		}
	}

	public Uri getUri() {
		if (mLocation == LOCATION_CATALOG_LIST) {
			String uri = mSourceManager.getDefaultCatalogListURI().toString();
			return Uri.parse(uri);
		} else if (mLocation == LOCATION_CATALOG) {
			String uri = mSourceManager.genarateGetCatalogUrl(mCatalog, 0, mSize).toString();
			return Uri.parse(uri);
		} else return null;
	}

	@Override
	public HashMap<String, String> getBucketIds() {
		super.getBucketIds();
		if (mLocation == LOCATION_CATALOG_LIST) {
			return getCatalogList();
		} else if (mLocation == LOCATION_CATALOG) {
			return getPictureList();
		}
		return null;
	}

	private HashMap<String, String> getCatalogList() {
		try {
			mCatalogs = mSourceManager.getDefaultCatalogList();

			int size = mCatalogs.size();
			BaseCatalog catalog;
			HashMap<String, String> hash = new HashMap<String, String>(size);
			for (int i = 0; i < size; i++) {
				catalog = mCatalogs.get(i);
				hash.put(catalog.id, catalog.name);
			}
			return hash;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private HashMap<String, String> getPictureList() {
		try {
			mPictures = mSourceManager.getDefaultCatalog(mCatalog, 0, mSize);

			int size = mPictures.size();
			BasePicture picture;
			HashMap<String, String> hash = new HashMap<String, String>(size);
			for (int i = 0; i < size; i++) {
				picture = mPictures.get(i);
				hash.put(picture.id, picture.views);
			}
			return hash;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int getCount() {
		int size = 0;
		switch (mLocation) {
			case LOCATION_CATALOG_LIST:
				size = mCatalogs.size();
				break;
			case LOCATION_CATALOG:
				initPictureIfNeed();
				size = mPictures.size();
				break;
			default:
				break;
		}
		return size;
	}

	public boolean isEmpty() {
		boolean isEmpty = false;
		switch (mLocation) {
			case LOCATION_CATALOG_LIST:
				isEmpty = ((mCatalogs == null) || (mCatalogs.size() == 0));
				break;
			case LOCATION_CATALOG:
				isEmpty = ((mPictures == null) || (mPictures.size() == 0));
				break;
			default:
				break;
		}
		return isEmpty;
	}

	public IImage getImageAt(int i) {
		BasePicture pic = mPictures.get(i);
		return new UrlImage(mContext, this, i, pic.id);
	}

	public IImage getImageForUri(Uri uri) {
		initPictureIfNeed();
		BasePicture pic;
		for (int i = 0; i < mPictures.size(); i++) {
			pic = mPictures.get(i);
			if (pic.id.equals(mSourceManager.getPictureId(uri.toString()))) {
				return new UrlImage(mContext, this, i, pic.id);
			}
		}
		return null;
	}

	public int getImageIndex(IImage image) {
		BasePicture pic;
		for (int i = 0; i <= mPictures.size(); i++) {
			pic = mPictures.get(i);
			if (pic.id.equals(image.getTitle())) {
				return i;
			}
		}
		return 0;
	}

	@Override
	public boolean refreshAble() {
		return true;
	}
}
