package com.bp.droppa.sleepassistant.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bp.droppa.sleepassistant.R;

/** Zobrazuje a vykonava kalibraciu */
public class CalibrationActivity extends Activity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private int count;
    private float threshold;
    private float vector;
    private ProgressBar progressBar;
    private TextView textView;
    private SharedPreferences sharedPreferences;
    private Button btCalib;
    private Button btCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setTitle("Sensor Calibration");



        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        threshold = sharedPreferences.getFloat("threshold",0f);
        count = 0;

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        textView = (TextView) findViewById(R.id.tw_calibration);


        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        btCancel = (Button) findViewById(R.id.bt_cancel);
        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(count< 500){
                    Toast.makeText(getApplicationContext(), "Calibration not finished",Toast.LENGTH_LONG).show();
                }
                finish();
            }
        });
        btCalib = (Button) findViewById(R.id.bt_calibrate);
        btCalib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("Calibrating...");
                progressBar.setVisibility(View.VISIBLE);
                btCalib.setVisibility(View.GONE);

//              oneskorenie aby nebolo zaznamenane kliknutie na displej
//                final Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
                        mSensorManager.registerListener(CalibrationActivity.this, mSensor, SensorManager.SENSOR_DELAY_UI);
//                    }
//                }, 1000);
//
            }
        });


    }


    @Override
    protected void onPause() {
        super.onPause();

        mSensorManager.unregisterListener(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("threshold", threshold);
        editor.apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_calibration, menu);
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

            vector = x * x + y * y + z * z;
            count++;
            if (vector > threshold) threshold = vector;
            // vykonanie 500 pokusov
            if (count >= 500) {
                Toast.makeText(getApplicationContext(), "Calibration successful",Toast.LENGTH_LONG).show();
                mSensorManager.unregisterListener(CalibrationActivity.this);
                finish();

            }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
