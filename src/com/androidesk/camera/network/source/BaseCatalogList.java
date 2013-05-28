
package com.androidesk.camera.network.source;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.json.JSONException;

public abstract class BaseCatalogList extends FileSerialize {
	protected BaseCatalog[] mCatalogs;
	protected int mCount;

	public void set(int index, BaseCatalog cls){
		mCatalogs[index] = cls;
	}

	public BaseCatalog get(int index){
		return mCatalogs[index];
	}

	public int size(){
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
		mCatalogs = new AndroideskCatalog[length];
		AndroideskCatalog temp;
		for (int i = 0; i < length; i++) {
			temp = new AndroideskCatalog();
			temp.read(ds);
			mCatalogs[i] = temp;
		}
	}
	
	protected abstract void parseJosn(String json) throws JSONException;
}
