package com.androidesk.camera.network.source;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BaiduCatalogList extends BaseCatalogList{

	public BaiduCatalogList(String json) throws JSONException {
		super();
		parseJosn(json);
		mCount = (mCatalogs == null ? 0 : mCatalogs.length);
	}

	public BaiduCatalogList(int size) {
		super();
		this.mCatalogs = new AndroideskCatalog[size];
		mCount = size;
	}
	
	@Override
	protected void parseJosn(String json) throws JSONException {
		JSONObject jsonObject = new JSONObject(json);
		JSONArray categoryJSONArray = jsonObject.getJSONArray("data");
		if (categoryJSONArray == null) {
			return;
		}
		JSONObject category;
		// 设置默认的选项
		mCatalogs = new BaiduCatalog[categoryJSONArray.length()];
		for (int i = 0; i < categoryJSONArray.length(); i++) {
			category = categoryJSONArray.getJSONObject(i);
			BaiduCatalog ca = new BaiduCatalog(category);
			mCatalogs[i] = ca;
		}
	}
}
