
package com.androidesk.camera.gallery;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;

import com.androidesk.camera.network.FileSerialize;
import com.androidesk.camera.network.MyHttpClientDownloader;

public class UrlImageList extends CachedImageList {
	private static final String URL_CATALOG_LIST = "http://service.androidesk.com/cate/list";

	private static final String URL_CATALOG = "http://service.androidesk.com/img/listcate?cid=%s&skip=%s&limit=%s&rank=date";

	private final int mLocation;
	private CatalogList mCatalogs;

	private PictureList mPictures;

	private final String mGetPicturesUrl;
	private final Context mContext;

	public final MyHttpClientDownloader downloader;

	public static final int LOCATION_CATALOG_LIST = 1;
	public static final int LOCATION_CATALOG = 2;

	public UrlImageList(Context ctx, int location, String catalog) {
		this(ctx, location, catalog, 30);
	}

	public UrlImageList(Context ctx, int location, String catalog, int size) {
		super();

		mContext = ctx;
		this.downloader = new MyHttpClientDownloader(ctx);
		mLocation = location;
		mGetPicturesUrl = String.format(URL_CATALOG, catalog, 0, size);
	}

	private void initPictureIfNeed() {
		if (mPictures == null) {
			getPictureList();
		}
	}

	public Uri getUri() {
		return Uri.parse(mGetPicturesUrl);
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
			String result = downloader.getStringFromNetwork(URI.create(URL_CATALOG_LIST));
			mCatalogs = new CatalogList(result);

			int size = mCatalogs.size();
			Catalog catalog;
			HashMap<String, String> hash = new HashMap<String, String>(size);
			for (int i = 0; i < size; i++) {
				catalog = mCatalogs.get(i);
				hash.put(catalog.id, catalog.name);
			}
			return hash;
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private HashMap<String, String> getPictureList() {
		try {
			String result = downloader.getStringFromNetwork(URI.create(mGetPicturesUrl));
			mPictures = new PictureList(result);

			int size = mPictures.size();
			Picture picture;
			HashMap<String, String> hash = new HashMap<String, String>(size);
			for (int i = 0; i < size; i++) {
				picture = mPictures.get(i);
				hash.put(picture.id, picture.views);
			}
			return hash;
		} catch (JSONException e) {
			e.printStackTrace();
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
		Picture pic = mPictures.get(i);
		return new UrlImage(mContext, this, i, pic.id);
	}

	public IImage getImageForUri(Uri uri) {
		initPictureIfNeed();

		Picture pic;
		for (int i = 0; i < mPictures.size(); i++) {
			pic = mPictures.get(i);
			if (pic.id.equals(getValue(uri.toString(), "imgid"))) {
				return new UrlImage(mContext, this, i, pic.id);
			}
		}
		return null;
	}

	// 在url中获取名称为name的参数值
	private static String getValue(String url, String name) {
		Pattern p = Pattern.compile("(\\?|&)" + name + "=([^&?]*)");
		Matcher m = p.matcher(url);
		while (m.find()) {
			return m.group(2);
		}
		return null;
	}

	public int getImageIndex(IImage image) {
		Picture pic;
		for (int i = 0; i <= mPictures.size(); i++) {
			pic = mPictures.get(i);
			if (pic.id.equals(image.getTitle())) {
				return i;
			}
		}
		return 0;
	}
}

class Catalog extends FileSerialize {
	public String id;
	public String name_rCN;
	public String name;
	public String cover;// 封面图片的id
	public int rank = -1;

	public Catalog() {
		super();
	}

	public Catalog(JSONObject category) throws JSONException {
		super();
		parseJosn(category);
	}

	public Catalog(String id, String name_rCN, String name, String cover, int rank) {
		super();
		this.id = id;
		this.name_rCN = name_rCN;
		this.name = name;
		this.cover = cover;
		this.rank = rank;
	}

	@Override
	public String toString() {
		return "ImgClass [id=" + id + ", name_rCN=" + name_rCN + ", name=" + name + ", cover="
				+ cover + ", rank=" + rank + "]";
	}

	@Override
	public void write(DataOutputStream ds) throws IOException {
		ds.writeUTF(id);
		ds.writeUTF(name_rCN);
		ds.writeUTF(name);
		ds.writeUTF(cover);
		ds.writeInt(rank);
	}

	@Override
	public void read(DataInputStream ds) throws IOException {
		id = ds.readUTF();
		name_rCN = ds.readUTF();
		name = ds.readUTF();
		cover = ds.readUTF();
		rank = ds.readInt();
	}

	private void parseJosn(JSONObject category) throws JSONException {
		id = category.getString("_id");
		name_rCN = category.getString("rname");
		name = category.getString("name");
		rank = category.getInt("rank");
		cover = category.getJSONObject("cover").getString("_300x225");
	}
}

class CatalogList extends FileSerialize {
	private Catalog[] mCatalogs;
	private final int mCount;

	public CatalogList(String json) throws JSONException {
		super();
		parseJosn(json);
		mCount = (mCatalogs == null ? 0 : mCatalogs.length);
	}

	public CatalogList(int size) {
		super();
		this.mCatalogs = new Catalog[size];
		mCount = size;
	}

	public void set(int index, Catalog cls) {
		mCatalogs[index] = cls;
	}

	public Catalog get(int index) {
		return mCatalogs[index];
	}

	public int size() {
		return mCount;
	}

	@Override
	public void write(DataOutputStream ds) throws IOException {
		super.write(ds);

		int length = mCatalogs.length;
		ds.writeInt(length);
		for (int i = 0; i < length; i++) {
			mCatalogs[i].write(ds);
		}
	}

	@Override
	public String toString() {
		return "CatalogList [mCatalogs = " + Arrays.toString(mCatalogs) + "]";
	}

	@Override
	public void read(DataInputStream ds) throws IOException {
		super.read(ds);

		int length = ds.readInt();
		mCatalogs = new Catalog[length];
		Catalog temp;
		for (int i = 0; i < length; i++) {
			temp = new Catalog();
			temp.read(ds);
			mCatalogs[i] = temp;
		}
	}

	private void parseJosn(String json) throws JSONException {
		JSONObject jsonObject = new JSONObject(json);
		JSONArray categoryJSONArray = jsonObject.getJSONArray("resp");
		if (categoryJSONArray == null) {
			return;
		}
		JSONObject category;
		// 设置默认的选项
		mCatalogs = new Catalog[categoryJSONArray.length()];
		for (int i = 0; i < categoryJSONArray.length(); i++) {
			category = categoryJSONArray.getJSONObject(i);
			Catalog ca = new Catalog(category);
			mCatalogs[i] = ca;
		}
	}
}

class Picture extends FileSerialize {
	public String id;
	public String uid;
	public String cid;
	public String views;
	public String rank;
	public String faverate;
	public String url;

	public Picture(String id, String uid, String cid, String views, String rank, String faverate,
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

	public Picture(JSONObject picture) throws JSONException {
		super();
		parseJosn(picture);
	}

	public Picture() {
		super();
	}

	@Override
	public String toString() {
		return "Picture [id=" + id + ", uid=" + uid + ", cid=" + cid + ", views=" + views
				+ ", rank=" + rank + ", faverate=" + faverate + ", url=" + url + "]";
	}

	@Override
	public void write(DataOutputStream ds) throws IOException {
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
		id = ds.readUTF();
		uid = ds.readUTF();
		cid = ds.readUTF();
		views = ds.readUTF();
		rank = ds.readUTF();
		faverate = ds.readUTF();
		url = ds.readUTF();
	}

	private void parseJosn(JSONObject picture) throws JSONException {
		id = picture.getString("_id");
		uid = picture.getString("uid");
		cid = picture.getString("cid");
		views = picture.getString("views");
		rank = picture.getString("rank");
		faverate = picture.getString("favs");
		url = picture.getJSONObject("fobjs").getString("_1440x1280");
	}
}

class PictureList extends FileSerialize {
	private Picture[] mPictures;
	private final int mCount;

	public PictureList(String json) throws JSONException {
		super();
		parseJosn(json);
		mCount = (mPictures == null ? 0 : mPictures.length);
	}

	public PictureList(int size) {
		super();
		this.mPictures = new Picture[size];
		mCount = size;
	}

	public void set(int index, Picture cls) {
		mPictures[index] = cls;
	}

	public Picture get(int index) {
		return mPictures[index];
	}

	public int size() {
		return mCount;
	}

	@Override
	public void write(DataOutputStream ds) throws IOException {
		super.write(ds);

		int length = mPictures.length;
		ds.writeInt(length);
		for (int i = 0; i < length; i++) {
			mPictures[i].write(ds);
		}
	}

	@Override
	public String toString() {
		return "CatalogList [mPictures = " + Arrays.toString(mPictures) + "]";
	}

	@Override
	public void read(DataInputStream ds) throws IOException {
		super.read(ds);

		int length = ds.readInt();
		mPictures = new Picture[length];
		Picture temp;
		for (int i = 0; i < length; i++) {
			temp = new Picture();
			temp.read(ds);
			mPictures[i] = temp;
		}
	}

	private void parseJosn(String json) throws JSONException {
		JSONObject jsonObject = new JSONObject(json);
		JSONArray categoryJSONArray = jsonObject.getJSONObject("resp").getJSONArray("images");
		if (categoryJSONArray == null) {
			return;
		}
		// 设置默认的选项
		mPictures = new Picture[categoryJSONArray.length()];
		for (int i = 0; i < categoryJSONArray.length(); i++) {
			Picture imgClass = new Picture(categoryJSONArray.getJSONObject(i));
			mPictures[i] = imgClass;
		}
	}
}
