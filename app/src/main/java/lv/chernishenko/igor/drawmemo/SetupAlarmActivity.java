package lv.chernishenko.igor.drawmemo;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.rey.material.app.DatePickerDialog;
import com.rey.material.app.Dialog;
import com.rey.material.app.TimePickerDialog;
import com.rey.material.widget.EditText;
import com.rey.material.widget.RadioButton;
import com.rey.material.widget.Slider;
import com.rey.material.widget.Switch;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import lv.chernishenko.igor.drawmemo.model.Alarm;
import lv.chernishenko.igor.drawmemo.utils.MemoApp;
import lv.chernishenko.igor.drawmemo.utils.Utils;

/**
 * (C) Copyright 2015 - Present day by Igor Chernishenko.
 * All rights reserved.
 */
public class SetupAlarmActivity extends ActionBarActivity implements View.OnClickListener {

    public static final String MEMO_ID = "memo_id";

    public static final String ALARM_ID = "alarm_id";

    private static final int SELECT_ALARM_REQUEST = 7531;

    private Calendar alarmTime;

    private int memoId;
    private int alarmId;

    private TextView time;
    private TextView date;
    private Switch vibration;
    private View alarmBtn;
    private TextView alarmName;
    private Slider alarmVolume;
    private Switch repeat;
    private View repeatBtn;
    private TextView repeatText;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MMM/yy");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    private Alarm alarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_alarm);


        memoId = getIntent().getIntExtra(MEMO_ID, -1);
        alarmId = getIntent().getIntExtra(ALARM_ID, -1);
        if (alarmId == -1) {
            alarm = new Alarm();
        } else {
            alarm = MemoApp.getAppInstance().getDbHelper().getAlarmById(alarmId);
        }
        alarmTime = new GregorianCalendar();

        time = (TextView) findViewById(R.id.time);
        date = (TextView) findViewById(R.id.date);
        vibration = (Switch) findViewById(R.id.vibration);
        alarmBtn = findViewById(R.id.alarm_sound);
        alarmName = (TextView) findViewById(R.id.alarm_name);
        alarmVolume = (Slider) findViewById(R.id.alarm_volume);
        repeat = (Switch) findViewById(R.id.repeat);
        repeatBtn = findViewById(R.id.repeat_frequency);
        repeatText = (TextView) findViewById(R.id.repeat_frequency_text);

        time.setOnClickListener(this);
        date.setOnClickListener(this);
        alarmBtn.setOnClickListener(this);
        repeatBtn.setOnClickListener(this);
        findViewById(R.id.button_ok).setOnClickListener(this);
        findViewById(R.id.button_cancel).setOnClickListener(this);

        Date dateTime = alarmTime.getTime();
        time.setText(timeFormat.format(dateTime));
        date.setText(dateFormat.format(dateTime));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_ALARM_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                String realPathFromUri = getRealPathFromUri(uri);
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor.moveToFirst()) {
                    alarm.setAlarmSoundUri(Uri.parse(realPathFromUri));
                    alarmName.setText(cursor.getString(
                            cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)));
                }
                cursor.close();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        // TODO: show alert dialog about progress loss.
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.time) {
            displayAlarmTimeDialog();
        } else if (id == R.id.date) {
            displayAlarmDateDialog();
        } else if (id == R.id.alarm_sound) {
            startAlarmSoundSelection();
        } else if (id == R.id.repeat_frequency) {
            displayAlarmFrequencyDialog();
        } else if (id == R.id.button_ok) {
            saveAlarm();
        } else if (id == R.id.button_cancel) {
            onBackPressed();
        }
    }

    private void displayAlarmFrequencyDialog() {
        final Dialog dialog = new Dialog.Builder()
                .contentView(R.layout.dialog_alarm_frequency)
                .title(getString(R.string.repeat_frequency))
                .positiveAction(getString(R.string.ok))
                .negativeAction(getString(R.string.cancel))
                .build(this);
        dialog.setCancelable(false);
        final EditText period = (EditText) dialog.findViewById(R.id.period);
        final RadioButton hour = (RadioButton) dialog.findViewById(R.id.hour);
        final RadioButton day = (RadioButton) dialog.findViewById(R.id.day);
        final RadioButton week = (RadioButton) dialog.findViewById(R.id.week);
        final RadioButton month = (RadioButton) dialog.findViewById(R.id.month);
        hour.setText(String.format(getString(R.string.in_an_hour), "_"));
        day.setText(String.format(getString(R.string.in_a_day), "_"));
        week.setText(String.format(getString(R.string.in_a_week), "_"));
        month.setText(String.format(getString(R.string.in_a_month), "_"));
        CompoundButton.OnCheckedChangeListener listener =
                new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    hour.setChecked(hour == buttonView);
                    day.setChecked(day == buttonView);
                    week.setChecked(week == buttonView);
                    month.setChecked(month == buttonView);
                }
            }
        };
        hour.setOnCheckedChangeListener(listener);
        day.setOnCheckedChangeListener(listener);
        week.setOnCheckedChangeListener(listener);
        month.setOnCheckedChangeListener(listener);
        period.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && !s.toString().isEmpty()) {
                    hour.setText(String.format(getString(R.string.in_an_hour),
                            Integer.parseInt(s.toString())));
                    day.setText(String.format(getString(R.string.in_a_day),
                            Integer.parseInt(s.toString())));
                    week.setText(String.format(getString(R.string.in_a_week),
                            Integer.parseInt(s.toString())));
                    month.setText(String.format(getString(R.string.in_a_month),
                            Integer.parseInt(s.toString())));
                } else {
                    hour.setText(String.format(getString(R.string.in_an_hour), "_"));
                    day.setText(String.format(getString(R.string.in_a_day), "_"));
                    week.setText(String.format(getString(R.string.in_a_week), "_"));
                    month.setText(String.format(getString(R.string.in_a_month), "_"));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        dialog.positiveActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm.setRepeatFrequency(Integer.parseInt(period.getText().toString()));
                String alarmFreqText = null;
                if (hour.isChecked()) {
                    alarmFreqText = hour.getText().toString();
                    alarm.setRepeatPeriod(Alarm.HOUR);
                } else if (day.isChecked()) {
                    alarmFreqText = day.getText().toString();
                    alarm.setRepeatPeriod(Alarm.DAY);
                } else if (week.isChecked()) {
                    alarmFreqText = week.getText().toString();
                    alarm.setRepeatPeriod(Alarm.WEEK);
                } else if (month.isChecked()) {
                    alarmFreqText = month.getText().toString();
                    alarm.setRepeatPeriod(Alarm.MONTH);
                }
                repeatText.setText(alarmFreqText);
                dialog.dismiss();
            }
        });
        dialog.negativeActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void saveAlarm() {
        alarm.setDate(alarmTime.getTime());
        alarm.setVibrate(vibration.isChecked());
        alarm.setVolume(alarmVolume.getValue());
        alarm.setRepeat(repeat.isChecked());
        Utils.getInstance().createAlarm(this, memoId, alarm.getDate().getTime());
        int alarmId = MemoApp.getAppInstance().getDbHelper().insertAlarm(alarm);
        Intent data = new Intent();
        data.putExtra(ALARM_ID, alarmId);
        data.putExtra(MEMO_ID, memoId);
        setResult(RESULT_OK, data);
        onBackPressed();
    }

    private void displayAlarmDateDialog() {
        final DatePickerDialog dialog = (DatePickerDialog) new DatePickerDialog.Builder()
                .date(alarmTime.get(Calendar.DAY_OF_MONTH), alarmTime.get(Calendar.MONTH),
                        alarmTime.get(Calendar.YEAR))
                .positiveAction(getString(R.string.ok))
                .negativeAction(getString(R.string.cancel))
                .build(this);
        dialog.positiveActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarmTime.set(Calendar.DAY_OF_MONTH, dialog.getDay());
                alarmTime.set(Calendar.MONTH, dialog.getMonth());
                alarmTime.set(Calendar.YEAR, dialog.getYear());
                date.setText(dateFormat.format(alarmTime.getTime()));
                dialog.dismiss();
            }
        });
        dialog.negativeActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void displayAlarmTimeDialog() {
        final TimePickerDialog dialog = (TimePickerDialog) new TimePickerDialog.Builder()
                .hour(alarmTime.get(Calendar.HOUR_OF_DAY))
                .minute(alarmTime.get(Calendar.MINUTE))
                .positiveAction(getString(R.string.ok))
                .negativeAction(getString(R.string.cancel))
                .build(this);
        dialog.positiveActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarmTime.set(Calendar.HOUR_OF_DAY, dialog.getHour());
                alarmTime.set(Calendar.MINUTE, dialog.getMinute());
                alarmTime.set(Calendar.SECOND, 0);
                time.setText(timeFormat.format(alarmTime.getTime()));
                dialog.dismiss();
            }
        });
        dialog.negativeActionClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void startAlarmSoundSelection() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, SELECT_ALARM_REQUEST);
    }

    private String getRealPathFromUri(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = getContentResolver().query(contentUri, proj, null, null, null);
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(columnIndex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}