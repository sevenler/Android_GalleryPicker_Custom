
package com.androidesk.camera.network.source;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class BasePicture extends FileSerialize {
	public String id;// id
	public String uid;// 所有者用户id
	public String cid;// 分类id
	public String views;// 查看次数
	public String rank;// 等级
	public String faverate;// 喜欢次数
	public String url;// 图片连接地址

	@Override
	public void write(DataOutputStream ds) throws IOException {
		super.write(ds);

		ds.writeUTF(id);
		ds.writeUTF(uid);
		ds.writeUTF(cid);
		ds.writeUTF(views);
		ds.writeUTF(rank);
		ds.writeUTF(faverate);
		ds.writeUTF(url);
	}

	@Override
	public void read(DataInputStream ds) throws IOException {
		super.read(ds);

		id = ds.readUTF();
		uid = ds.readUTF();
		cid = ds.readUTF();
		views = ds.readUTF();
		rank = ds.readUTF();
		faverate = ds.readUTF();
		url = ds.readUTF();
	}

	protected abstract void parseJosn(JSONObject json) throws JSONException;
	
	@Override
	public String toString() {
		return "Picture [id=" + id + ", uid=" + uid + ", cid=" + cid + ", views=" + views
				+ ", rank=" + rank + ", faverate=" + faverate + ", url=" + url + "]";
	}
}
