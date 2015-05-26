package lv.chernishenko.igor.drawmemo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import lv.chernishenko.igor.drawmemo.model.Alarm;
import lv.chernishenko.igor.drawmemo.model.Memo;

/**
 * (C) Copyright 2015 - Present day by Igor Chernishenko.
 * All rights reserved.
 */
public class AppDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = AppDatabaseHelper.class.getCanonicalName();

    private static final String DATABASE_NAME = "draw_and_memo_db";
    private static final int VERSION = 4;

    private static final String MEMOS_TABLE_NAME = "memos";
    private static final String ALARM_TABLE_NAME = "alarm";

    private static final String ID = "_id";
    private static final String CREATION_DATE = "creation_date";
    private static final String IMG_PATH = "img_path";
    private static final String ALARM_STATE = "alarm_state";
    private static final String PRIORITY = "priority";
    private static final String ALARM_ID = "alarm_id";
    private static final String ALARM_DATE = "alarm_date";
    private static final String VIBRATION = "vibration";
    private static final String ALARM_SOUND_URI = "alarm_sound_uri";
    private static final String ALARM_VOLUME = "alarm_volume";
    private static final String REPEAT = "repeat";
    private static final String REPEAT_FREQUENCY = "repeat_frequency";
    private static final String REPEAT_PERIOD = "repeat_period";

    private static final String CREATE_MEMO_TABLE =
            "CREATE TABLE " + MEMOS_TABLE_NAME + "(" +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            CREATION_DATE + " DATETIME NOT NULL," +
            IMG_PATH + " TEXT NOT NULL," +
            ALARM_STATE + " INTEGER NOT NULL," +
            PRIORITY + " INTEGER NOT NULL," +
            ALARM_ID + " INTEGER," +
            "FOREIGN KEY (" + ALARM_ID + ") REFERENCES " + ALARM_TABLE_NAME + "(" + ID +
            "));";

    private static final String CREATE_ALARM_TABLE =
            "CREATE TABLE " + ALARM_TABLE_NAME + "(" +
            ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            ALARM_DATE + " DATETIME NOT NULL," +
            VIBRATION + " INTEGER NOT NULL," +
            ALARM_SOUND_URI + " TEXT," +
            ALARM_VOLUME + " INTEGER NOT NULL," +
            REPEAT + " INTEGER NOT NULL," +
            REPEAT_FREQUENCY + " INTEGER," +
            REPEAT_PERIOD + " INTEGER" +
            ");";

    private static final String SELECT_ALL = "SELECT * FROM ";

    private static final String SELECT_BY_ID = ID + "=?";

    private static final String LAST_ALARM_ID = "SELECT " + ID + " FROM "
            + ALARM_TABLE_NAME + " ORDER BY " + ID + " DESC LIMIT 1";

    private SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public AppDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ALARM_TABLE);
        db.execSQL(CREATE_MEMO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MEMOS_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ALARM_TABLE_NAME);

        onCreate(db);
    }

    public void insertMemo(Memo memo) {
        ContentValues values = new ContentValues();

        values.put(CREATION_DATE, iso8601Format.format(memo.getCreationDate()));
        values.put(IMG_PATH, memo.getImgPath());
        values.put(ALARM_STATE, memo.getAlarmState());
        if (memo.getAlarmState() == 1) {
            values.put(ALARM_ID, memo.getAlarmId());
        }
        values.put(PRIORITY, memo.getPriority());

        SQLiteDatabase db = getWritableDatabase();
        db.insert(MEMOS_TABLE_NAME, null, values);
        db.close();
    }

    public void updateMemo(Memo memo) {
        ContentValues values = new ContentValues();

        values.put(CREATION_DATE, iso8601Format.format(memo.getCreationDate()));
        values.put(IMG_PATH, memo.getImgPath());
        values.put(ALARM_STATE, memo.getAlarmState());
        if (memo.getAlarmState() == Memo.ALARM_ON) {
            values.put(ALARM_ID, memo.getAlarmId());
        }
        values.put(PRIORITY, memo.getPriority());

        SQLiteDatabase db = getWritableDatabase();
        db.update(MEMOS_TABLE_NAME, values, ID + "=?", new String[]{
                String.valueOf(memo.getId())
        });
        db.close();
    }

    public void removeMemo(Memo memo) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(ALARM_TABLE_NAME, ID + "=?", new String[]{
                String.valueOf(memo.getAlarmId())
        });
        db.delete(MEMOS_TABLE_NAME, ID + "=?", new String[]{
                String.valueOf(memo.getId())
        });
        db.close();
    }

    public int insertAlarm(Alarm alarm) {
        ContentValues values = new ContentValues();

        values.put(ALARM_DATE, iso8601Format.format(alarm.getDate()));
        values.put(VIBRATION, alarm.isVibrate());
        values.put(ALARM_SOUND_URI, alarm.getAlarmSoundUri().getPath());
        values.put(ALARM_VOLUME, alarm.getVolume());
        values.put(REPEAT, alarm.isRepeat());
        if (alarm.isRepeat()) {
            values.put(REPEAT_FREQUENCY, alarm.getRepeatFrequency());
            values.put(REPEAT_PERIOD, alarm.getRepeatPeriod());
        }

        SQLiteDatabase db = getWritableDatabase();
        db.insert(ALARM_TABLE_NAME, null, values);
        db.close();
        SQLiteDatabase rdb = getReadableDatabase();
        Cursor cursor = rdb.rawQuery(LAST_ALARM_ID, null);
        int alarmId = -1;
        if (cursor.moveToFirst()) {
            alarmId = cursor.getInt(cursor.getColumnIndex(ID));
        }
        cursor.close();
        return alarmId;
    }

    public void updateAlarm(Alarm alarm) {
        ContentValues values = new ContentValues();

        values.put(ALARM_DATE, iso8601Format.format(alarm.getDate()));
        values.put(VIBRATION, alarm.isVibrate());
        values.put(ALARM_SOUND_URI, alarm.getAlarmSoundUri().getPath());
        values.put(ALARM_VOLUME, alarm.getVolume());
        values.put(REPEAT, alarm.isRepeat());
        if (alarm.isRepeat()) {
            values.put(REPEAT_FREQUENCY, alarm.getRepeatFrequency());
            values.put(REPEAT_PERIOD, alarm.getRepeatPeriod());
        }

        SQLiteDatabase db = getWritableDatabase();
        db.update(ALARM_TABLE_NAME, values, ID + "=?", new String[]{
                String.valueOf(alarm.getId())
        });
        db.close();
    }

    public Memo[] getAllMemos() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_ALL + MEMOS_TABLE_NAME, null);
        Memo[] allMemos = new Memo[cursor.getCount()];
        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
                Memo memo = getMemoFromCursor(cursor);
                allMemos[i] = memo;
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return allMemos;
    }

    private Memo getMemoFromCursor(Cursor cursor) {
        Memo memo = new Memo();
        memo.setId(cursor.getInt(cursor.getColumnIndex(ID)));
        String creationDateString = cursor.getString(cursor.getColumnIndex(CREATION_DATE));
        try {
            memo.setCreationDate(iso8601Format.parse(creationDateString));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        memo.setImgPath(cursor.getString(cursor.getColumnIndex(IMG_PATH)));
        memo.setAlarmState(cursor.getInt(cursor.getColumnIndex(ALARM_STATE)));
        if (memo.getAlarmState() == Memo.ALARM_ON) {
            memo.setAlarmId(cursor.getInt(cursor.getColumnIndex(ALARM_ID)));
        }
        memo.setPriority(cursor.getInt(cursor.getColumnIndex(PRIORITY)));
        return memo;
    }

    public Memo getMemoById(int id) {
        Memo memo = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(MEMOS_TABLE_NAME, new String[]{
                ID, CREATION_DATE, IMG_PATH, ALARM_STATE, PRIORITY, ALARM_ID
        }, SELECT_BY_ID, new String[]{
                String.valueOf(id)
        }, null, null, null);
        if (cursor.moveToFirst()) {
            memo = getMemoFromCursor(cursor);
        }
        cursor.close();
        db.close();
        return memo;
    }

    public Alarm getAlarmById(int id) {
        Alarm alarm = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(ALARM_TABLE_NAME, new String[] {
                ID, ALARM_DATE, VIBRATION, ALARM_SOUND_URI, ALARM_VOLUME,
                REPEAT, REPEAT_FREQUENCY, REPEAT_PERIOD
        }, SELECT_BY_ID, new String[] {
                String.valueOf(id)
        }, null, null, null);
        if (cursor.moveToFirst()) {
            alarm = new Alarm();
            alarm.setId(cursor.getInt(cursor.getColumnIndex(ID)));
            String alarmDate = cursor.getString(cursor.getColumnIndex(ALARM_DATE));
            try {
                alarm.setDate(iso8601Format.parse(alarmDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            alarm.setVibrate(cursor.getInt(cursor.getColumnIndex(VIBRATION)) != 0);
            alarm.setAlarmSoundUri(
                    Uri.parse(cursor.getString(cursor.getColumnIndex(ALARM_SOUND_URI))));
            alarm.setVolume(cursor.getInt(cursor.getColumnIndex(ALARM_VOLUME)));
            alarm.setRepeat(cursor.getInt(cursor.getColumnIndex(REPEAT)) != 0);
            alarm.setRepeatFrequency(cursor.getInt(cursor.getColumnIndex(REPEAT_FREQUENCY)));
            alarm.setRepeatPeriod(cursor.getInt(cursor.getColumnIndex(REPEAT_PERIOD)));
        }
        cursor.close();
        db.close();
        return alarm;
    }
}
