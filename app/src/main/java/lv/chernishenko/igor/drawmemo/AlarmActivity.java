package lv.chernishenko.igor.drawmemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import lv.chernishenko.igor.drawmemo.model.Alarm;
import lv.chernishenko.igor.drawmemo.model.Memo;
import lv.chernishenko.igor.drawmemo.receivers.AlarmReceiver;
import lv.chernishenko.igor.drawmemo.utils.MemoApp;
import lv.chernishenko.igor.drawmemo.utils.Utils;

/**
 * (C) Copyright 2015 - Present day by Igor Chernishenko.
 * All rights reserved.
 */
public class AlarmActivity extends ActionBarActivity {

    private static final int MAX_VOLUME = 15;

    private MediaPlayer mp;
    private Vibrator vibrator;
    private Memo memo;
    private Alarm alarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);

        int itemSize = displaySize.x;

        ImageView image = (ImageView) findViewById(R.id.alarm_image);

        ViewGroup.LayoutParams lp = image.getLayoutParams();
        lp.height = itemSize - 2 * getResources().getDimensionPixelSize(R.dimen.default_margin);
        lp.width = itemSize - 2 * getResources().getDimensionPixelSize(R.dimen.default_margin);
        image.setLayoutParams(lp);

        TextView time = (TextView) findViewById(R.id.alarm_time);
        TextView date = (TextView) findViewById(R.id.alarm_date);

        findViewById(R.id.alarm_disable).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.stop();
                vibrator.cancel();
                if (memo != null && alarm != null) {
                    if (alarm.isRepeat()) {
                        Calendar calendar = new GregorianCalendar();
                        calendar.setTime(alarm.getDate());
                        int frequency = alarm.getRepeatFrequency();
                        if (alarm.getRepeatPeriod() == Alarm.HOUR) {
                            calendar.add(Calendar.HOUR, frequency);
                        } else if (alarm.getRepeatPeriod() == Alarm.DAY) {
                            calendar.add(Calendar.DAY_OF_MONTH, frequency);
                        } else if (alarm.getRepeatPeriod() == Alarm.WEEK) {
                            calendar.add(Calendar.WEEK_OF_MONTH, frequency);
                        } else if (alarm.getRepeatPeriod() == Alarm.MONTH) {
                            calendar.add(Calendar.MONTH, frequency);
                        }
                        alarm.setDate(calendar.getTime());
                        MemoApp.getAppInstance().getDbHelper().updateAlarm(alarm);
                        Utils.getInstance().createAlarm(AlarmActivity.this, memo.getId(),
                                alarm.getDate().getTime());
                    } else {
                        memo.setAlarmState(Memo.ALARM_OFF);
                        memo.setAlarmId(-1);
                        MemoApp.getAppInstance().getDbHelper().updateMemo(memo);
                        Utils.getInstance().removeAlarm(memo.getId());
                    }
                }
                finish();
            }
        });

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MMM/yy");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        int memoId = getIntent().getIntExtra(AlarmReceiver.MEMO_ID, -1);
        if (memoId != -1) {
            memo = MemoApp.getAppInstance().getDbHelper().getMemoById(memoId);
            if (memo != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(memo.getImgPath());
                image.setImageBitmap(bitmap);
                if (memo.getAlarmId() != -1) {
                    alarm = MemoApp.getAppInstance().getDbHelper().getAlarmById(memo.getAlarmId());
                    if (alarm != null) {
                        time.setText(timeFormat.format(alarm.getDate()));
                        date.setText(dateFormat.format(alarm.getDate()));
                        if (alarm.isVibrate()) {
                            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(new long[]{300, 500}, 0);
                        }
                        mp = new MediaPlayer();
                        try {
                            File soundFile = new File(alarm.getAlarmSoundUri().getPath());
                            mp.setDataSource(soundFile.getAbsolutePath());
                            float log1 = (float)
                                  (Math.log(MAX_VOLUME - alarm.getVolume()) / Math.log(MAX_VOLUME));
                            mp.setVolume(log1, log1);
                            mp.prepare();
                            mp.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}