
package com.androidesk.camera;

import android.app.WallpaperManager;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class DisplayManager {
	private static DisplayManager instance = null;
	private int displayWidth = 0;
	private int displayHeight = 0;
	private int densityDpi = 0;

	private int desiredWidth = 0;
	private int desiredHeight = 0;

	private DisplayMetrics dm = null;

	public static DisplayManager instance() {
		if (instance == null) {
			synchronized (DisplayManager.class) {
				if (instance == null) {
					instance = new DisplayManager();
				}
			}
		}
		return instance;
	}

	private DisplayManager() {
	}

	public void initialize(Context context) {
		if (dm != null) return;
		try {
			dm = new DisplayMetrics();
			WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
			wm.getDefaultDisplay().getMetrics(dm);
			displayWidth = dm.widthPixels;
			displayHeight = dm.heightPixels;
			densityDpi = dm.densityDpi;
		} catch (Exception e) {
		}
		try {
			desiredWidth = WallpaperManager.getInstance(context).getDesiredMinimumWidth();
			desiredHeight = WallpaperManager.getInstance(context).getDesiredMinimumHeight();
		} catch (Exception e) {
		}
	}

	private void checkInitialized() {
		if (dm == null) throw new IllegalArgumentException("displaymanager not being initialized");
	}

	public int getDisplayWidth() {
		checkInitialized();
		return displayWidth;
	}

	public int getDisplayHeight() {
		checkInitialized();
		return displayHeight;
	}

	public int getDesiredWidth() {
		checkInitialized();
		return desiredWidth;
	}

	public int getDesiredHeight() {
		checkInitialized();
		return desiredHeight;
	}

	public DisplayMetrics getDisplayMetrics() {
		checkInitialized();
		return dm;
	}

	public int getDensityDpi() {
		checkInitialized();
		return densityDpi;
	}
}
