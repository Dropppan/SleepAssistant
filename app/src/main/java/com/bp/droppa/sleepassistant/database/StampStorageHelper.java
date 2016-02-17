package com.bp.droppa.sleepassistant.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Roman Droppa on 8.4.2015.
 *
 *
 */
/**Vytvara a udrziava databazu */
public class StampStorageHelper extends SQLiteOpenHelper {



    private static final String SQL_CREATE_STAMPS =
            "CREATE TABLE " + StampStorage.Stamps.TABLE_NAME + " (" +
                    StampStorage.Stamps._ID + " INTEGER PRIMARY KEY," +
                    StampStorage.Days.COLUMN_NAME_DATE+" INTEGER "+"NOT NULL,"+
                    StampStorage.Stamps.COLUMN_NAME_TIME + " INTEGER " +"NOT NULL,"+
                    StampStorage.Stamps.COLUMN_NAME_X +" REAL,"+
                    StampStorage.Stamps.COLUMN_NAME_Y +" REAL,"+
                    StampStorage.Stamps.COLUMN_NAME_Z +" REAL,"+
                    StampStorage.Stamps.COLUMN_NAME_THRES_COUNT +" INTEGER ,"+
                    "FOREIGN KEY("+StampStorage.Days.COLUMN_NAME_DATE+") REFERENCES "+
                    StampStorage.Days.TABLE_NAME+"("+StampStorage.Days.COLUMN_NAME_DATE+"))";

    private static final String SQL_DELETE_STAMPS =
            "DROP TABLE IF EXISTS " + StampStorage.Stamps.TABLE_NAME;

    private static final String SQL_CREATE_DAYS =
            "CREATE TABLE "+StampStorage.Days.TABLE_NAME+" ("+
                    StampStorage.Days.COLUMN_NAME_DATE+ " NUMERIC PRIMARY KEY, "+
                    StampStorage.Days.COLUMN_NAME_DURATION + " INTEGER,"+
                    StampStorage.Days.COLUMN_NAME_QUALITY + " REAL,"+
                    StampStorage.Days.COLUMN_NAME_S_THRESHOLD + " REAL NOT NULL)";

    private static final String SQL_DELETE_DAYS=
            "DROP TABLE IF EXISTS " + StampStorage.Days.TABLE_NAME;


    public static final int DATABASE_VERSION = 8;
    public static final String DATABASE_NAME = "StampStorage.db";

    public StampStorageHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {

        db.execSQL(SQL_CREATE_DAYS);
        db.execSQL(SQL_CREATE_STAMPS);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_STAMPS);
        db.execSQL(SQL_DELETE_DAYS);

        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }



}