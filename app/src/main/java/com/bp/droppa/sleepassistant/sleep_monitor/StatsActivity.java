package com.bp.droppa.sleepassistant.sleep_monitor;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.bp.droppa.sleepassistant.R;
import com.bp.droppa.sleepassistant.database.StampStorage;
import com.bp.droppa.sleepassistant.database.StampStorageHelper;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Calendar;
import java.util.Date;

/** Zobrazuje graf a ostatne udaje o zazname */
public class StatsActivity extends ActionBarActivity {

    private StampStorageHelper mStampStorageHelper;
    private long dateToSelect;
    private MyTask myTask;
    private LineChart chart;
    private YAxis yAxisRight;
    private YAxis yAxisLeft;
    private XAxis xAxis;
    private Legend legend;
    private TextView twInBed;
    private TextView twQualty;
    private TextView twTime;
    private TextView twDeficit;
    private Toolbar toolbar;
    private long sleepLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sleepLength = sharedPreferences.getLong("lengthPreference",8);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            dateToSelect = extras.getLong("date");

        }

        //nastavenie vzhladu toolbaru/actionbaru
        myTask = new MyTask();
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        twInBed = (TextView) findViewById(R.id.tw_in_bed_value);
        twQualty = (TextView) findViewById(R.id.tw_quality_value);
        twTime = (TextView) findViewById(R.id.tw_hours_value);
        twDeficit = (TextView) findViewById(R.id.tw_deficit_val);


        //nastavenie vzhladu grafu
        mStampStorageHelper = new StampStorageHelper(this);
        chart = (LineChart) findViewById(R.id.chart);
        yAxisRight = chart.getAxisRight();
        yAxisRight.setEnabled(false);
        yAxisLeft = chart.getAxisLeft();
        yAxisLeft.setEnabled(false);
        xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        legend = chart.getLegend();
        legend.setEnabled(false);
        chart.setNoDataTextDescription(getString(R.string.chart_loading));
        chart.setDrawBorders(true);
        chart.setDescription("");
        chart.setScaleYEnabled(false);

        myTask.execute();
        //
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_stats, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }

        return super.onOptionsItemSelected(item);
    }

    private class MyTask extends AsyncTask {


        private static final long MILIS_IN_HOUR = 3600000;
        private static final long MILIS_IN_MINUTE = 60000;

        private String inBed;
        private double quality;
       // private long hours;
        long elapsedHours;
        long elapsedMin;
        long deficitHours;
        long deficitMin;
        long deficit;
        @Override
        protected Object doInBackground(Object[] params) {
            // List hodnot pre os X
            ArrayList<String> xVals = new ArrayList<>();
            // List hodnot pre os Y
            ArrayList<Entry> yVals = new ArrayList<>();

            SimpleDateFormat formater = new SimpleDateFormat("hh:mm");

            Calendar calendar = Calendar.getInstance();

            SQLiteDatabase db = mStampStorageHelper.getReadableDatabase();

            String[] projection = {StampStorage.Stamps.COLUMN_NAME_TIME, StampStorage.Stamps.COLUMN_NAME_THRES_COUNT};
            String selection = StampStorage.Days.COLUMN_NAME_DATE + "=?";
            String[] selectionArgs = {String.valueOf(dateToSelect)};
            Cursor c = db.query(
                    StampStorage.Stamps.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            String[] projectionD = {StampStorage.Days.COLUMN_NAME_QUALITY, StampStorage.Days.COLUMN_NAME_DURATION};
            Cursor cD = db.query(
                    StampStorage.Days.TABLE_NAME,
                    projectionD,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );


            c.moveToFirst();
            long startTime = c.getLong(c.getColumnIndexOrThrow(StampStorage.Stamps.COLUMN_NAME_TIME));
            inBed = formater.format(new Date(startTime));
            c.moveToLast();
            long endTime = c.getLong(c.getColumnIndexOrThrow(StampStorage.Stamps.COLUMN_NAME_TIME));
            inBed += " - " + formater.format(new Date(endTime));

            //vypocet uplinuteho casu pocas spanku | deficitu
            long diference = endTime - startTime;

            deficit = diference-sleepLength;


            elapsedHours = diference / MILIS_IN_HOUR;
            diference = diference % MILIS_IN_HOUR;
            elapsedMin = diference / MILIS_IN_MINUTE;

            deficitHours = deficit/MILIS_IN_HOUR;
            deficit = deficit % MILIS_IN_HOUR;
            deficitMin = Math.abs(deficit/MILIS_IN_MINUTE);

            //cyklus pre naplnenie osi Y|X
            int sleepCount = 0;
            int totalCount = 1;
            int i = 0;
            int tempVals;
            c.moveToFirst();
            do {
                xVals.add(formater.format(c.getLong(c.getColumnIndexOrThrow(StampStorage.Stamps.COLUMN_NAME_TIME))));
                tempVals = c.getInt(c.getColumnIndexOrThrow(StampStorage.Stamps.COLUMN_NAME_THRES_COUNT));
                totalCount += tempVals;
                //obmedzenie poctu zaznamov pohybu na 90
                if (tempVals > 90) {
                    yVals.add(new Entry(90, i));
                } else yVals.add(new Entry(tempVals, i));
                //ak je pocet pohybov pod danu hodnotu uzivate spi
                if (tempVals < 40) {
                    sleepCount += tempVals;
                }
                i++;
            } while (c.moveToNext());



            //vypocet kvality spanku
            quality = (double) sleepCount / totalCount * 100;

            //inicializacia dat grafu
            LineDataSet lineDataSet = new LineDataSet(yVals, "");
            lineDataSet.setDrawCircles(false);
            lineDataSet.setLineWidth(4f);
            ArrayList<LineDataSet> lineDataSets = new ArrayList<>();
            lineDataSets.add(lineDataSet);
            LineData lineData = new LineData(xVals, lineDataSets);
            lineData.setDrawValues(false);
            chart.setData(lineData);


            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            //vypis hodnot v textovych poliach
            if(deficit<0){
                twDeficit.setTextColor(Color.RED);
            }else twDeficit.setTextColor(Color.GREEN);
            twInBed.setText(inBed);
            twDeficit.setText(String.format("%d h:%d m",deficitHours,deficitMin));
            twQualty.setText(String.format("%.2f %%", quality));
            twTime.setText(String.format("%d h:%d m", elapsedHours, elapsedMin));
            chart.invalidate();
        }
    }
}
