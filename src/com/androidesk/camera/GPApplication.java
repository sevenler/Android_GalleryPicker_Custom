package com.androidesk.camera;

import android.app.Application;
import android.content.Context;

public class GPApplication extends Application{

	@Override
	public void onCreate() {
		super.onCreate();
		
		Context ctx = getApplicationContext();
		DisplayManager.instance().initialize(ctx);
		CacheManager.instance().initialize(ctx);
	}

}
