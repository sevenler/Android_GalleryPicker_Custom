
package com.androidesk.camera.network.source;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class BaseCatalog extends FileSerialize {
	public String id;// 分类id
	public String name_rCN;// 分类中文名称
	public String name;// 分类名称
	public String cover;// 封面图片的id
	public int rank = -1;// 级别

	public BasePicture[] mPictures;
	public int mCount = 0;

	public void set(int index, BasePicture cls) {
		mPictures[index] = cls;
	}

	public BasePicture get(int index) {
		return mPictures[index];
	}

	public int size() {
		return mCount;
	}

	@Override
	public void write(DataOutputStream ds) throws IOException {
		super.write(ds);

		ds.writeUTF(id);
		ds.writeUTF(name_rCN);
		ds.writeUTF(name);
		ds.writeUTF(cover);
		ds.writeInt(rank);

		int length = mCount;
		ds.writeInt(length);
		for (int i = 0; i < length; i++) {
			mPictures[i].write(ds);
		}
	}

	@Override
	public void read(DataInputStream ds) throws IOException {
		super.read(ds);

		id = ds.readUTF();
		name_rCN = ds.readUTF();
		name = ds.readUTF();
		cover = ds.readUTF();
		rank = ds.readInt();

		int length = ds.readInt();
		mPictures = new AndroideskPicture[length];
		AndroideskPicture temp;
		for (int i = 0; i < length; i++) {
			temp = new AndroideskPicture();
			temp.read(ds);
			mPictures[i] = temp;
		}
	}

	protected abstract void parseJosn(JSONObject category) throws JSONException;

	protected abstract void parsePictureJosn(String json) throws JSONException;
	
	@Override
	public String toString() {
		return "ImgClass [id=" + id + ", name_rCN=" + name_rCN + ", name=" + name + ", cover="
				+ cover + ", rank=" + rank + ", CatalogList=" + "CatalogList [mPictures = "
				+ Arrays.toString(mPictures) + "]" + "]";
	}
}
