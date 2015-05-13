package lv.chernishenko.igor.drawmemo.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import lv.chernishenko.igor.drawmemo.AlarmActivity;

/**
 * (C) Copyright 2015 - Present day by Igor Chernishenko.
 * All rights reserved.
 */
public class AlarmReceiver extends BroadcastReceiver {

    public static final String MEMO_ID = "memo_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        int memoId = intent.getIntExtra(MEMO_ID, -1);
        Intent alertIntent = new Intent(context.getApplicationContext(), AlarmActivity.class);
        alertIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alertIntent.putExtra(MEMO_ID, memoId);
        context.getApplicationContext().startActivity(alertIntent);
    }
}