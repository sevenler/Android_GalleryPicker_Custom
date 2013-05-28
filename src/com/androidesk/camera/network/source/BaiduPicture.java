package com.androidesk.camera.network.source;

import org.json.JSONException;
import org.json.JSONObject;

public class BaiduPicture extends BasePicture {

	@Override
	protected void parseJosn(JSONObject picture) throws JSONException {
		id = picture.getString("contsign");
		uid = null;
		cid = picture.getString("tag");
		views = null;
		rank = picture.getString("eroticlevel");
		faverate = null;
		url = picture.getString("objurl");
	}

	public BaiduPicture(JSONObject picture) throws JSONException {
		super();
		parseJosn(picture);
	}
	
	public BaiduPicture() {
		super();
	}
}
