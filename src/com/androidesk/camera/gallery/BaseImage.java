/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.androidesk.camera.gallery;

import com.androidesk.camera.BitmapManager;
import com.androidesk.camera.Util;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;

import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Represents a particular image and provides access to the underlying bitmap
 * and two thumbnail bitmaps as well as other information such as the id, and
 * the path to the actual image data.
 */
public abstract class BaseImage implements IImage {
    private static final String TAG = "BaseImage";
    private static final int UNKNOWN_LENGTH = -1;
    protected ContentResolver mContentResolver;

    // Database field
    protected Uri mUri;
    protected long mId;
    protected String mDataPath;
    protected final int mIndex;
    protected String mMimeType;
    private final long mDateTaken;
    private String mTitle;

    protected BaseImageList mContainer;

    private int mWidth = UNKNOWN_LENGTH;
    private int mHeight = UNKNOWN_LENGTH;

    protected BaseImage(BaseImageList container, ContentResolver cr,
            long id, int index, Uri uri, String dataPath, String mimeType,
            long dateTaken, String title) {
        mContainer = container;
        mContentResolver = cr;
        mId = id;
        mIndex = index;
        mUri = uri;
        mDataPath = dataPath;
        mMimeType = mimeType;
        mDateTaken = dateTaken;
        mTitle = title;
    }

    public String getDataPath() {
        return mDataPath;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof Image)) return false;
        return mUri.equals(((Image) other).mUri);
    }

    @Override
    public int hashCode() {
        return mUri.hashCode();
    }

    public Bitmap fullSizeBitmap(int minSideLength, int maxNumberOfPixels) {
        return fullSizeBitmap(minSideLength, maxNumberOfPixels,
                IImage.ROTATE_AS_NEEDED, IImage.NO_NATIVE);
    }

    public Bitmap fullSizeBitmap(int minSideLength, int maxNumberOfPixels,
            boolean rotateAsNeeded, boolean useNative) {
        Uri url = mContainer.contentUri(mId);
        if (url == null) return null;

        Bitmap b = Util.makeBitmap(minSideLength, maxNumberOfPixels,
                url, mContentResolver, useNative);

        if (b != null && rotateAsNeeded) {
            b = Util.rotate(b, getDegreesRotated());
        }

        return b;
    }

    public InputStream fullSizeImageData() {
        try {
            InputStream input = mContentResolver.openInputStream(mUri);
            return input;
        } catch (IOException ex) {
            return null;
        }
    }

    public Uri fullSizeImageUri() {
        return mUri;
    }

    public IImageList getContainer() {
        return mContainer;
    }

    public long getDateTaken() {
        return mDateTaken;
    }

    public int getDegreesRotated() {
        return 0;
    }

    public String getMimeType() {
        return mMimeType;
    }

    public String getTitle() {
        return mTitle;
    }

    private void setupDimension() {
        ParcelFileDescriptor input = null;
        try {
            input = mContentResolver.openFileDescriptor(mUri, "r");
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapManager.instance().decodeFileDescriptor(
                    input.getFileDescriptor(), options);
            mWidth = options.outWidth;
            mHeight = options.outHeight;
        } catch (FileNotFoundException ex) {
            mWidth = 0;
            mHeight = 0;
        } finally {
            Util.closeSilently(input);
        }
    }

    public int getWidth() {
        if (mWidth == UNKNOWN_LENGTH) setupDimension();
        return mWidth;
    }

    public int getHeight() {
        if (mHeight == UNKNOWN_LENGTH) setupDimension();
        return mHeight;
    }

    public Bitmap miniThumbBitmap() {
        Bitmap b = null;
        try {
			long id = mId;
			b = BitmapManager.instance().getThumbnail(mContentResolver, id,
					Images.Thumbnails.MINI_KIND, null, false);
			
			//如果系统的缩略图有问题，可以执行下面的decode
            /*BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            b = requestDecode(mDataPath, options, 300);*/
        } catch (Throwable ex) {
            Log.e(TAG, "miniThumbBitmap got exception", ex);
            return null;
        }
        if (b != null) {
            b = Util.rotate(b, getDegreesRotated());
        }
        return b;
    }

    protected void onRemove() {
    }

    @Override
    public String toString() {
        return mUri.toString();
    }
    
    
    public static Bitmap ensureGLCompatibleBitmap(Bitmap bitmap) {
        if (bitmap == null || bitmap.getConfig() != null) return bitmap;
        Bitmap newBitmap = bitmap.copy(Config.ARGB_8888, false);
        bitmap.recycle();
        return newBitmap;
    }
	
	public static int computeSampleSizeLarger(int w, int h,
            int minSideLength) {
        int initialSize = Math.max(w / minSideLength, h / minSideLength);
        if (initialSize <= 1) return 1;

        return initialSize <= 8
                ? prevPowerOf2(initialSize)
                : initialSize / 8 * 8;
    }
	
	public static int prevPowerOf2(int n) {
		if (n <= 0)
			throw new IllegalArgumentException();
		return Integer.highestOneBit(n);
	}
	
	public static Bitmap requestDecode(final String filePath,
            Options options, int targetSize) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
            FileDescriptor fd = fis.getFD();
            return requestDecode(fd, options, targetSize);
        } catch (Exception ex) {
            Log.w(TAG, ex);
            return null;
        } finally {
            closeSilently(fis);
        }
    }
	
	public static void closeSilently(Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (Throwable t) {
            Log.w(TAG, "close fail", t);
        }
    }

    public static Bitmap requestDecode(FileDescriptor fd,
            Options options, int targetSize) {
        if (options == null) options = new Options();

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, null, options);

        options.inSampleSize = computeSampleSizeLarger(
                options.outWidth, options.outHeight, targetSize);
        options.inJustDecodeBounds = false;

        Bitmap result = BitmapFactory.decodeFileDescriptor(fd, null, options);
        return ensureGLCompatibleBitmap(result);
    }
}
