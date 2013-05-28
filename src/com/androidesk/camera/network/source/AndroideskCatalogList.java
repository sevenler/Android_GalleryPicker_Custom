
package com.androidesk.camera.network.source;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AndroideskCatalogList extends BaseCatalogList {
	public AndroideskCatalogList(String json) throws JSONException {
		super();
		parseJosn(json);
		mCount = (mCatalogs == null ? 0 : mCatalogs.length);
	}

	public AndroideskCatalogList(int size) {
		super();
		this.mCatalogs = new AndroideskCatalog[size];
		mCount = size;
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