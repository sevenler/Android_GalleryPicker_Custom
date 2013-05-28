
package com.androidesk.camera.network.source;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BaiduCatalog extends BaseCatalog {
	public BaiduCatalog(JSONObject category) throws JSONException {
		super();
		parseJosn(category);
	}

	public BaiduCatalog(String json) throws JSONException {
		super();
		parsePictureJosn(json);
		mCount = (mPictures == null ? 0 : mPictures.length);
	}

	public BaiduCatalog(int size) {
		super();
		this.mPictures = new BaiduPicture[size];
		mCount = size;
	}
	
	@Override
	protected void parseJosn(JSONObject category) throws JSONException {
		id = category.getString("tagno");
		name_rCN = category.getString("tagname");
		name = category.getString("tagname");
		rank = category.getInt("seq");
		cover = category.optString("objurl");
	}

	@Override
	protected void parsePictureJosn(String json) throws JSONException {
		JSONObject jsonObject = new JSONObject(json);
		JSONArray categoryJSONArray = jsonObject.getJSONArray("data");
		if (categoryJSONArray == null) {
			return;
		}
		// 设置默认的选项
		mPictures = new BaiduPicture[categoryJSONArray.length()];
		for (int i = 0; i < categoryJSONArray.length(); i++) {
			BaiduPicture imgClass = new BaiduPicture(categoryJSONArray.getJSONObject(i));
			mPictures[i] = imgClass;
		}
	}

}
