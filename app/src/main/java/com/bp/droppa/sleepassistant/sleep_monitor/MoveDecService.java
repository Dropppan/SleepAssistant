package com.bp.droppa.sleepassistant.sleep_monitor;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.bp.droppa.sleepassistant.R;
import com.bp.droppa.sleepassistant.database.Stamp;
import com.bp.droppa.sleepassistant.database.StampStorage;
import com.bp.droppa.sleepassistant.database.StampStorageHelper;


import java.util.ArrayList;
import java.util.Calendar;


/**
 * Created by Roman Droppa on 8.4.2015.
 */
/** Zaznamenava udaje o pohybe pocas spanku a zapisuje ich do DB */
public class MoveDecService extends IntentService implements SensorEventListener {


    private SensorManager mSensorManager;
    private Sensor mSensor;
    //    private MyTask myTask;
    public static volatile boolean running;
    private long tempTime;
    private long currentTime;
    private long tempTime2;
    private ArrayList<Stamp> stamps;
    private ArrayList<Stamp> tempStamps;
    private long wakeUpTime;
    private long wakeUpPhase;
    private float threshold;

    private StampStorageHelper mStampStorageHelper;
    private long startDateStamp;
    private int crossCount;
    PowerManager.WakeLock wakeLock;


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public MoveDecService(String name) {
        super(name);
    }

    public MoveDecService() {
        super("MoveDecService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //ak je zaznam spusteny po polnoci, ulozi sa do predchadzajuceho dna
        Calendar date = Calendar.getInstance();
        if (date.get(Calendar.HOUR_OF_DAY) >= 0 && date.get(Calendar.HOUR_OF_DAY) < 8) {
            date.add(Calendar.DAY_OF_MONTH, -1);
        }
        // vynulovanie nepodstanych udajov
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        startDateStamp = date.getTimeInMillis();

        //ziskanie hrannicnej citlivosti
        SharedPreferences sharedPreferences;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        threshold = sharedPreferences.getFloat("threshold", 0);

        //
        currentTime = 0;
        tempTime2 = 0;

        //zabezpecenie funkcnosti aj s vypnutym displejom
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        wakeLock.acquire();

        running = true;
        stamps = new ArrayList<Stamp>();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        mStampStorageHelper = new StampStorageHelper(this);

        // tvorba notifikacie
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Sleep Assistant")
                        .setContentText("Sleep monitoring is running");
        // po kliknuti na notif. je spustena hlavna aktivita
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.putExtra("com.example.johnny.sleepassistant.fragmentIndex", 0);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        mBuilder.setContentIntent(pendingIntent);
        startForeground(2, mBuilder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
        running = false;
        wakeLock.release();

    }

    @Override
    protected void onHandleIntent(Intent intent) {

        wakeUpTime = intent.getLongExtra("wakeUpTime", 0);
        // vypocet polhodinoveho intervalu
        wakeUpPhase = wakeUpTime - 30 * 60 * 1000;

        SQLiteDatabase db = mStampStorageHelper.getWritableDatabase();

        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
        // zapis potrebnych uhdajov do tab. DAYS
        ContentValues c = new ContentValues();
        c.put(StampStorage.Days.COLUMN_NAME_DATE, startDateStamp);
        c.put(StampStorage.Days.COLUMN_NAME_S_THRESHOLD,threshold );
        db.replace(StampStorage.Days.TABLE_NAME, null, c);
        c.clear();

        while (running) {
            currentTime = System.currentTimeMillis();
            //zapnut budik ak nastal cas alebo sa pouzivatel pohol v 30 min useku pred poz. casom
            if (currentTime >= wakeUpTime || (crossCount > 2 && currentTime >= wakeUpPhase)) {
                mSensorManager.unregisterListener(this);
                Intent intent1 = new Intent(this, AlarmActivity.class);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent1);
                running = false;

                // ak pole znaciek nie je prazde znacka sa zapise do DB
            } else if (stamps.size() >= 1) {
                // kopirovanie znaciek
                tempStamps = new ArrayList<>(stamps);
                stamps.clear();

                for (Stamp st : tempStamps) {

                    //ukladanie do DB
                    //- len pre ucely testovania
                    c.put(StampStorage.Stamps.COLUMN_NAME_X, st.getX());
                    c.put(StampStorage.Stamps.COLUMN_NAME_Y, st.getY());
                    c.put(StampStorage.Stamps.COLUMN_NAME_Z, st.getZ());
                    //podstatne data
                    c.put(StampStorage.Stamps.COLUMN_NAME_THRES_COUNT, st.getCount());
                    c.put(StampStorage.Stamps.COLUMN_NAME_TIME, st.getDate());
                    c.put(StampStorage.Days.COLUMN_NAME_DATE, startDateStamp);
                    db.insert(StampStorage.Stamps.TABLE_NAME, null, c);
                    c.clear();

                }

                tempStamps.clear();
            }
            // kedze sa znacky tvoria kazdych 5 min nie je potrebne aby vlakno bezalo nepretrzite
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // po spusteni budika sa do databazy zapise cas zobudenia
        c.put(StampStorage.Stamps.COLUMN_NAME_X, 0);
        c.put(StampStorage.Stamps.COLUMN_NAME_Y, 0);
        c.put(StampStorage.Stamps.COLUMN_NAME_Z, 0);
        c.put(StampStorage.Stamps.COLUMN_NAME_THRES_COUNT, crossCount);
        c.put(StampStorage.Stamps.COLUMN_NAME_TIME, System.currentTimeMillis());
        c.put(StampStorage.Days.COLUMN_NAME_DATE, startDateStamp);
        db.insert(StampStorage.Stamps.TABLE_NAME, null, c);
        c.clear();

        // nacitanie zaznamenanych udajov merania z DB
        String[] projection = {StampStorage.Stamps.COLUMN_NAME_TIME, StampStorage.Stamps.COLUMN_NAME_THRES_COUNT};
        String selection = StampStorage.Days.COLUMN_NAME_DATE + "=?";
        String[] selectionArgs = {String.valueOf(startDateStamp)};
        Cursor cursor = db.query(
                StampStorage.Stamps.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        // ziskanie pociatocneho a koncoveho casu
        cursor.moveToFirst();
        long startTime = cursor.getLong(cursor.getColumnIndexOrThrow(StampStorage.Stamps.COLUMN_NAME_TIME));
        cursor.moveToLast();
        long endTime = cursor.getLong(cursor.getColumnIndexOrThrow(StampStorage.Stamps.COLUMN_NAME_TIME));


        //vypocet kvality spanku
        int sleepCount = 0;
        int tempVals;
        cursor.moveToFirst();
        do {
            tempVals = cursor.getInt(cursor.getColumnIndexOrThrow(StampStorage.Stamps.COLUMN_NAME_THRES_COUNT));
            //ak je pocet pohybov pod danu hodnotu uzivate spi
            if (tempVals <= 3) {
                sleepCount ++;
            }
        } while (cursor.moveToNext());

        // ulozenie kvality a dlzky spanku do databazy
        c.put(StampStorage.Days.COLUMN_NAME_DURATION, endTime - startTime);
        c.put(StampStorage.Days.COLUMN_NAME_QUALITY, (double) sleepCount / cursor.getCount());
        String selection1 = StampStorage.Days.COLUMN_NAME_DATE + " LIKE ?";
        String[] selectionArgs1 = {String.valueOf(startDateStamp)};
        db.update(StampStorage.Days.TABLE_NAME,
                c,
                selection1,
                selectionArgs1);
        c.clear();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        //overenie presiahnutia hranicnej hodnoty
            if (x * x + y * y + z * z >= threshold) {
                crossCount++;
            }
            tempTime = System.currentTimeMillis();
        // overenie ubehnutia piatich minut
        if (System.currentTimeMillis() - tempTime2 >= 300000) {
            tempTime2 = System.currentTimeMillis();
            stamps.add(new Stamp(x, y, z, tempTime2, crossCount));
            crossCount = 0;
        }


//          Pouzite pri zistovani hranicnej hodnoty
//        if (System.currentTimeMillis() - tempTime >= 250) {
//            tempTime = System.currentTimeMillis();
//            Log.e("SENSOR", String.valueOf(x * x + y * y + z * z));
////            stamps.add(new Stamp(x, y, z, tempTime, 0));
//        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}
