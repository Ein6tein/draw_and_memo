package lv.chernishenko.igor.drawmemo.utils;

import android.app.Application;

import lv.chernishenko.igor.drawmemo.database.AppDatabaseHelper;

/**
 * (C) Copyright 2015 - Present day by Igor Chernishenko.
 * All rights reserved.
 */
public class DrawAndMemoApplication extends Application {

    private static DrawAndMemoApplication appInstance;

    private static AppDatabaseHelper dbHelper;

    public static DrawAndMemoApplication getAppInstance() {
        return appInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appInstance = this;
        dbHelper = new AppDatabaseHelper(this);
    }

    @Override
    public void onTerminate() {
        dbHelper.close();
        super.onTerminate();
    }

    public AppDatabaseHelper getDbHelper() {
        return dbHelper;
    }
}