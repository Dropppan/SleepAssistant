package com.bp.droppa.sleepassistant.database;

import android.provider.BaseColumns;

/**
 * Created by Roman Droppa on 14.4.2015.
 */
/** Sluzi ako kontajner nazvov tabuliek a stlpcov  */
public final class StampStorage {


    public StampStorage() {
    }


    public static abstract class Stamps implements BaseColumns {

        public static final String TABLE_NAME = "stamps";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_X = "x_axis";
        public static final String COLUMN_NAME_Y = "y_axis";
        public static final String COLUMN_NAME_Z = "z_axis";
        public static final String COLUMN_NAME_THRES_COUNT = "thres_count";


    }

    public static abstract class Days implements BaseColumns {

        public static final String TABLE_NAME = "days";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_S_THRESHOLD = "s_threshold";
        public static final String COLUMN_NAME_DURATION = "duration";
        public static final String COLUMN_NAME_QUALITY = "quality";


    }
}
