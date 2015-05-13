package lv.chernishenko.igor.drawmemo.model;

import java.util.Date;

/**
 * (C) Copyright 2015 - Present day by Igor Chernishenko.
 * All rights reserved.
 */
public class Memo {

    public static final int ALARM_OFF = 0;
    public static final int ALARM_ON = 1;

    public static final int PRIORITY_HIGH = 2;
    public static final int PRIORITY_MEDIUM = 1;
    public static final int PRIORITY_LOW = 0;

    private int id;
    private Date creationDate;
    private String imgPath;
    // 0 turned off, 1 - turned on
    private int alarmState;
    private int priority;
    private int alarmId;

    public Memo() {
    }

    public Memo(int id, Date creationDate, String imgPath,
                int alarmState, int alarmId, int priority) {
        this.id = id;
        this.creationDate = creationDate;
        this.imgPath = imgPath;
        this.alarmState = alarmState;
        this.alarmId = alarmId;
        this.priority = priority;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public int getAlarmState() {
        return alarmState;
    }

    public void setAlarmState(int alarmState) {
        this.alarmState = alarmState;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(int alarmId) {
        this.alarmId = alarmId;
    }
}