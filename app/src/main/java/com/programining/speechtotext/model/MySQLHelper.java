package com.programining.speechtotext.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class MySQLHelper extends SQLiteOpenHelper {

    private static final String KEY_DB_NAME = "app_db";
    private static final String KEY_TABLE_NAME = "audio_records";
    private static final int KEY_DB_VERSION = 1;


    /*
         private int fileId;
        private String firebaseId;
        private String localPath;
        private String displayName;
        private String localUri;
        private String firebaseUri;
        private boolean isUploaded;
        private double length;
        */
    private static final String KEY_COLUMN_ID = "id";
    private static final String KEY_COLUMN_FIREBASE_ID = "fid";
    private static final String KEY_COLUMN_PATH = "path";
    private static final String KEY_COLUMN_DISPLAY_NAME = "display_name";
    private static final String KEY_COLUMN_LOCAL_URI = "local_uri";
    private static final String KEY_COLUMN_FIREBASE_URI = "firebase_uri";
    private static final String KEY_COLUMN_IS_UPLOADED = "is_uploaded";
    private static final String KEY_COLUMN_LENGTH = "length";

    public MySQLHelper(@Nullable Context context) {
        super(context, KEY_DB_NAME, null, KEY_DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //write query to create table

        String query = "CREATE TABLE " + KEY_TABLE_NAME + "("
                + KEY_COLUMN_ID + " INTEGER PRIMARY KEY,"
                + KEY_COLUMN_FIREBASE_ID + " TEXT,"
                + KEY_COLUMN_PATH + " TEXT,"
                + KEY_COLUMN_DISPLAY_NAME + " TEXT,"
                + KEY_COLUMN_LOCAL_URI + " TEXT,"
                + KEY_COLUMN_FIREBASE_URI + " TEXT,"
                + KEY_COLUMN_IS_UPLOADED + " INTEGER,"
                + KEY_COLUMN_LENGTH + " decimal(2,2))";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void addAudioRecord(MyAudioRecord audioRecord) {

        // insert new audio record
        ContentValues cv = new ContentValues();
        cv.put(KEY_COLUMN_FIREBASE_ID, audioRecord.getFirebaseId());
        cv.put(KEY_COLUMN_PATH, audioRecord.getLocalPath());
        cv.put(KEY_COLUMN_DISPLAY_NAME, audioRecord.getDisplayName());
        cv.put(KEY_COLUMN_LOCAL_URI, audioRecord.getLocalUri());
        cv.put(KEY_COLUMN_FIREBASE_URI, audioRecord.getFirebaseUri());
        cv.put(KEY_COLUMN_IS_UPLOADED, audioRecord.isUploaded());
        cv.put(KEY_COLUMN_LENGTH, audioRecord.getLength());

        SQLiteDatabase db = getWritableDatabase();
        db.insert(KEY_TABLE_NAME, null, cv);
    }

    public ArrayList<MyAudioRecord> getAudioRecordsArrayList() {
        // select query

        String query = "SELECT * FROM " + KEY_TABLE_NAME;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        ArrayList<MyAudioRecord> audioRecords = new ArrayList<>();

        if (cursor.moveToFirst()) {

            do {
                MyAudioRecord a = new MyAudioRecord();

                a.setFileId(cursor.getInt(cursor.getColumnIndex(KEY_COLUMN_ID)));
                a.setFirebaseId(cursor.getString(cursor.getColumnIndex(KEY_COLUMN_FIREBASE_ID)));
                a.setLocalPath(cursor.getString(cursor.getColumnIndex(KEY_COLUMN_PATH)));
                a.setDisplayName(cursor.getString(cursor.getColumnIndex(KEY_COLUMN_DISPLAY_NAME)));
                a.setLocalUri(cursor.getString(cursor.getColumnIndex(KEY_COLUMN_LOCAL_URI)));
                a.setFirebaseUri(cursor.getString(cursor.getColumnIndex(KEY_COLUMN_FIREBASE_URI)));

                int i = cursor.getInt(cursor.getColumnIndex(KEY_COLUMN_IS_UPLOADED));
                boolean isUploaded;
                if (i == 0) {
                    isUploaded = false;
                } else {
                    isUploaded = true;
                }
                a.setUploaded(isUploaded);

                a.setLength(cursor.getDouble(cursor.getColumnIndex(KEY_COLUMN_LENGTH)));

                audioRecords.add(a);

            } while (cursor.moveToNext());
        }

        return audioRecords;
    }
}
