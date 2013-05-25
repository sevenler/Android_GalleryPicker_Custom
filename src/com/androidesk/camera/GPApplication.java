package com.androidesk.camera;

import android.app.Application;
import android.content.Context;

public class GPApplication extends Application{
	public boolean mDataChanged = false;//set true after data changed,such as delete images
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		Context ctx = getApplicationContext();
		DisplayManager.instance().initialize(ctx);
		CacheManager.instance().initialize(ctx);
	}

}
