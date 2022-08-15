package com.bignerdranch.android.moviegallery.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

public abstract class PictureUtils {

    public static Bitmap getScaledBitmap(String path, float destWidth, float destHeight) {

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);
        int inSampleSize = Math.max(1, Math.round(Math.max(opts.outWidth / destWidth, opts.outHeight / destHeight)));

        opts = new BitmapFactory.Options();
        opts.inSampleSize=inSampleSize;
        return BitmapFactory.decodeFile(path, opts);
    }

    public static Bitmap getScaledBitmap(String path, Activity activity) {
        Point outSize = new Point();
        activity.getWindowManager().getDefaultDisplay()
                .getSize(outSize);

        return getScaledBitmap(path, outSize.x, outSize.y);
    }
}
