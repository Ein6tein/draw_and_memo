package lv.chernishenko.igor.drawmemo.model;

import android.net.Uri;

import java.util.Date;

public class Alarm {

    public static final int HOUR = 0;

    public static final int DAY = 1;

    public static final int WEEK = 2;

    public static final int MONTH = 3;

    private int id;

    private Date date;

    private boolean vibrate;

    private Uri alarmSoundUri;

    private int volume;

    private boolean repeat;

    private int repeatFrequency;

    private int repeatPeriod;

    public Alarm() {
    }

    public Alarm(int id, Date date, boolean vibrate, Uri alarmSoundUri,
                 int volume, boolean repeat, int repeatFrequency, int repeatPeriod) {
        this.id = id;
        this.date = date;
        this.vibrate = vibrate;
        this.alarmSoundUri = alarmSoundUri;
        this.volume = volume;
        this.repeat = repeat;
        this.repeatFrequency = repeatFrequency;
        this.repeatPeriod = repeatPeriod;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isVibrate() {
        return vibrate;
    }

    public void setVibrate(boolean vibrate) {
        this.vibrate = vibrate;
    }

    public Uri getAlarmSoundUri() {
        return alarmSoundUri;
    }

    public void setAlarmSoundUri(Uri alarmSoundUri) {
        this.alarmSoundUri = alarmSoundUri;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public int getRepeatFrequency() {
        return repeatFrequency;
    }

    public void setRepeatFrequency(int repeatFrequency) {
        this.repeatFrequency = repeatFrequency;
    }

    public int getRepeatPeriod() {
        return repeatPeriod;
    }

    public void setRepeatPeriod(int repeatPeriod) {
        this.repeatPeriod = repeatPeriod;
    }
}