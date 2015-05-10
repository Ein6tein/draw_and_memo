package lv.chernishenko.igor.drawmemo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import lv.chernishenko.igor.drawmemo.model.Memo;

/**
 * (C) Copyright 2015 - Present day by Igor Chernishenko.
 * All rights reserved.
 */
public class AppDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "draw_and_memo_db";
    private static final int VERSION = 1;

    private static final String TABLE_NAME = "memos";

    private static final String ID = "_id";
    private static final String CREATION_DATE = "creation_date";
    private static final String IMG_PATH = "img_path";
    private static final String ALARM_STATE = "alarm_state";
    private static final String ALARM_TIME = "alarm_time";
    private static final String PRIORITY = "priority";

    private static final String CREATE_TABLE =
                    "create table " + TABLE_NAME + "(" +
                    ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    CREATION_DATE + " DATETIME NOT NULL," +
                    IMG_PATH + " TEXT NOT NULL," +
                    ALARM_STATE + " INTEGER," +
                    ALARM_TIME + " DATETIME," +
                    PRIORITY + " INTEGER NOT NULL" +
                    ");";

    private static final String SELECT_ALL = "SELECT * FROM ";

    private SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public AppDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        onCreate(db);
    }

    public void insert(Memo memo) {
        ContentValues values = new ContentValues();

        values.put(CREATION_DATE, iso8601Format.format(memo.getCreationDate()));
        values.put(IMG_PATH, memo.getImgPath());
        values.put(ALARM_STATE, memo.getAlarmState());
        if (memo.getAlarmState() == 1) {
            values.put(ALARM_TIME, iso8601Format.format(memo.getAlarmDate()));
        }
        values.put(PRIORITY, memo.getPriority());

        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public void update(Memo memo) {
        ContentValues values = new ContentValues();

        values.put(CREATION_DATE, iso8601Format.format(memo.getCreationDate()));
        values.put(IMG_PATH, memo.getImgPath());
        values.put(ALARM_STATE, memo.getAlarmState());
        if (memo.getAlarmState() == 1) {
            values.put(ALARM_TIME, iso8601Format.format(memo.getAlarmDate()));
        }
        values.put(PRIORITY, memo.getPriority());

        SQLiteDatabase db = getWritableDatabase();
        db.update(TABLE_NAME, values, ID + "=?", new String[] {
                String.valueOf(memo.getId())
        });
        db.close();
    }

    public Memo[] getAllMemos() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(SELECT_ALL + TABLE_NAME, null);
        Memo[] allMemos = new Memo[cursor.getCount()];
        if (cursor.moveToFirst()) {
            for (int i = 0; i < cursor.getCount(); i++) {
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
                    String alarmDate = cursor.getString(cursor.getColumnIndex(ALARM_TIME));
                    try {
                        memo.setAlarmDate(iso8601Format.parse(alarmDate));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                memo.setPriority(cursor.getInt(cursor.getColumnIndex(PRIORITY)));
                allMemos[i] = memo;
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return allMemos;
    }
}
