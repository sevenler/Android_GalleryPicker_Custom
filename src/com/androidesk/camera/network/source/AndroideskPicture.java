
package com.androidesk.camera.network.source;

import org.json.JSONException;
import org.json.JSONObject;


public class AndroideskPicture extends BasePicture {
	
	public AndroideskPicture(String id, String uid, String cid, String views, String rank, String faverate,
			String url) {
		super();
		this.id = id;
		this.uid = uid;
		this.cid = cid;
		this.views = views;
		this.rank = rank;
		this.faverate = faverate;
		this.url = url;
	}

	public AndroideskPicture(JSONObject picture) throws JSONException {
		super();
		parseJosn(picture);
	}

	public AndroideskPicture() {
		super();
	}

	@Override
	protected void parseJosn(JSONObject picture) throws JSONException {
		id = picture.getString("_id");
		uid = picture.getString("uid");
		cid = picture.getString("cid");
		views = picture.getString("views");
		rank = picture.getString("rank");
		faverate = picture.getString("favs");
		url = picture.getJSONObject("fobjs").getString("_1440x1280");
	}
}
