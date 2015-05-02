package lv.chernishenko.igor.drawmemo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import lv.chernishenko.igor.drawmemo.R;

/**
 * (C) Copyright 2015 - Present day by Igor Chernishenko.
 * All rights reserved.
 */
public class Utils {

    private static final String TAG = Utils.class.getCanonicalName();
    private static Utils instance;

    private HashMap<String, Bitmap> bitmapStorage = new HashMap<>();

    private Utils() {
    }

    public static Utils getInstance() {
        if (instance == null) {
            instance = new Utils();
        }
        return instance;
    }

    public void storeBitmap(String key, Bitmap bitmap) {
        bitmapStorage.put(key, bitmap);
    }

    public Bitmap getBitmapFromStorage(String key) {
        if (bitmapStorage.containsKey(key)) {
            return bitmapStorage.get(key);
        }
        return null;
    }

    public String getUniqueImageFilename(Context context) {
        Calendar calendar = new GregorianCalendar();
        StringBuilder fileName = new StringBuilder()
                .append(context.getString(R.string.file_prefix))
                .append(calendar.get(Calendar.DAY_OF_MONTH))
                .append(calendar.get(Calendar.MONTH))
                .append(calendar.get(Calendar.YEAR))
                .append("_")
                .append(calendar.get(Calendar.HOUR_OF_DAY))
                .append(calendar.get(Calendar.MINUTE))
                .append(calendar.get(Calendar.SECOND))
                .append(calendar.get(Calendar.MILLISECOND));
        return fileName.toString();
    }

    public String getImagesFolder() {
        String dirPath = Environment.getExternalStorageDirectory() + File.separator
                + "DrawAndMemoImages" + File.separator;
        return dirPath;
    }
}