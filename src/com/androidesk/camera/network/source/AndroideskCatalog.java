
package com.androidesk.camera.network.source;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AndroideskCatalog extends BaseCatalog {
	public AndroideskCatalog() {
		super();
	}

	public AndroideskCatalog(JSONObject category) throws JSONException {
		super();
		parseJosn(category);
	}

	public AndroideskCatalog(String id, String name_rCN, String name, String cover, int rank) {
		super();
		this.id = id;
		this.name_rCN = name_rCN;
		this.name = name;
		this.cover = cover;
		this.rank = rank;
	}

	public AndroideskCatalog(String json) throws JSONException {
		super();
		parsePictureJosn(json);
		mCount = (mPictures == null ? 0 : mPictures.length);
	}

	public AndroideskCatalog(int size) {
		super();
		this.mPictures = new AndroideskPicture[size];
		mCount = size;
	}

	@Override
	protected void parseJosn(JSONObject category) throws JSONException {
		id = category.getString("_id");
		name_rCN = category.getString("rname");
		name = category.getString("name");
		rank = category.getInt("rank");
		cover = category.getJSONObject("cover").getString("_300x225");
	}

	@Override
	protected void parsePictureJosn(String json) throws JSONException {
		JSONObject jsonObject = new JSONObject(json);
		JSONArray categoryJSONArray = jsonObject.getJSONObject("resp").getJSONArray("images");
		if (categoryJSONArray == null) {
			return;
		}
		// 设置默认的选项
		mPictures = new AndroideskPicture[categoryJSONArray.length()];
		for (int i = 0; i < categoryJSONArray.length(); i++) {
			AndroideskPicture imgClass = new AndroideskPicture(categoryJSONArray.getJSONObject(i));
			mPictures[i] = imgClass;
		}
	}
}
