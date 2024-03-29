package lv.chernishenko.igor.drawmemo.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import lv.chernishenko.igor.drawmemo.R;
import lv.chernishenko.igor.drawmemo.receivers.AlarmReceiver;

/**
 * (C) Copyright 2015 - Present day by Igor Chernishenko.
 * All rights reserved.
 */
public class Utils {

    private static final String TAG = Utils.class.getCanonicalName();

    private static Utils instance;

    private AlarmManager alarmMgr;

    // Used to store cropped bitmaps for background
    private HashMap<String, Bitmap> bitmapStorage = new HashMap<>();

    private HashMap<Integer, PendingIntent> intentStorage = new HashMap<>();

    // We make it private so no one can create an instance of this class
    private Utils() {
    }

    /**
     * _MUST_ be called to get instance of this class.
     *
     * @return instance of this class.
     */
    public static Utils getInstance() {
        if (instance == null) {
            instance = new Utils();
        }
        return instance;
    }

    /**
     * Stores bitmap into bitmap storage (HashMap).
     *
     * @param key - String key for bitmap, that we want to store.
     * @param bitmap - bitmap, that we want to store.
     */
    public void storeBitmap(String key, Bitmap bitmap) {
        bitmapStorage.put(key, bitmap);
    }

    /**
     * Get bitmap from storage (HashMap) by key.
     *
     * @param key - String key, to bitmap.
     * @return if storage contains the @param key - bitmap, else - null.
     */
    public Bitmap getBitmapFromStorage(String key) {
        if (bitmapStorage.containsKey(key)) {
            return bitmapStorage.get(key);
        }
        return null;
    }

    /**
     * Creates unique name for files/keys.
     *
     * @param context - used to get some resources.
     * @return unique String name.
     *
     * Format of name: prefix_ddMMyy_hhmmssmmm.
     * Uniqueness is achieved by using precise current time, 'till milliseconds.
     */
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

    /**
     * Get images store folder.
     *
     * @return String folder path.
     */
    public String getImagesFolder() {
        String dirPath = Environment.getExternalStorageDirectory() + File.separator
                + "DrawAndMemoImages" + File.separator;
        return dirPath;
    }

    public void createAlarm(Context context, int memoId, long time) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.MEMO_ID, memoId);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        if (alarmMgr == null) {
            alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        }
        if (intentStorage.containsKey(memoId)) {
            intentStorage.remove(memoId);
        }
        intentStorage.put(memoId, alarmIntent);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, time, alarmIntent);
    }

    public void removeAlarm(int memoId) {
        if (intentStorage.containsKey(memoId)) {
            intentStorage.get(memoId).cancel();
            intentStorage.remove(memoId);
        }
    }

}