
package com.androidesk.camera.gallery;

import android.graphics.Bitmap;
import android.os.Handler;

import com.androidesk.camera.RotateBitmap;

public class BitmapCallback implements Runnable {

	RotateBitmap mRotateBitmap;
	final Handler mHandler;

	@Override
	public void run() {
	}
	
	public void submit() {
		mHandler.post(this);
	}

	public BitmapCallback(RotateBitmap bitmap, Handler handler) {
		this.mRotateBitmap = bitmap;
		mHandler = handler;
	}

	public BitmapCallback(Handler handler) {
		this(null, handler);
	}

	public Bitmap getBitmap() {
		return mRotateBitmap.getBitmap();
	}

	public void setBitmap(Bitmap bitmap) {
		this.mRotateBitmap.setBitmap(bitmap);
	}

	public RotateBitmap getRotateBitmap() {
		return mRotateBitmap;
	}

	public void setRotateBitmap(RotateBitmap bitmap) {
		this.mRotateBitmap = bitmap;
	}

}
