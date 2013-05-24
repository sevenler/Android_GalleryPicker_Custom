package com.androidesk.camera;

import android.app.Application;

public class GPApplication extends Application{

	@Override
	public void onCreate() {
		super.onCreate();
		
		DisplayManager.instance().initialize(getApplicationContext());
	}

}
