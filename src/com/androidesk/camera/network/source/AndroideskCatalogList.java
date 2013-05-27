
package com.androidesk.camera.network.source;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;


public class AndroideskCatalogList extends BaseCatalogList {
	public AndroideskCatalogList(String json, Uri uri) throws JSONException {
		super();
		parseJosn(json);
		mCount = (mCatalogs == null ? 0 : mCatalogs.length);
		mUri = uri;
	}

	public AndroideskCatalogList(int size, Uri uri) {
		super();
		this.mCatalogs = new AndroideskCatalog[size];
		mCount = size;
		mUri = uri;
	}

	@Override
	protected void parseJosn(String json) throws JSONException {
		JSONObject jsonObject = new JSONObject(json);
		JSONArray categoryJSONArray = jsonObject.getJSONArray("resp");
		if (categoryJSONArray == null) {
			return;
		}
		JSONObject category;
		// 设置默认的选项
		mCatalogs = new AndroideskCatalog[categoryJSONArray.length()];
		for (int i = 0; i < categoryJSONArray.length(); i++) {
			category = categoryJSONArray.getJSONObject(i);
			AndroideskCatalog ca = new AndroideskCatalog(category);
			mCatalogs[i] = ca;
		}
	}
}
