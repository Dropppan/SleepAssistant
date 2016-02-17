package com.bp.droppa.sleepassistant.baby_monitor;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.bp.droppa.sleepassistant.R;
import com.bp.droppa.sleepassistant.settings.SettingsActivity;
import com.bp.droppa.sleepassistant.sleep_monitor.MainActivity;

/** Zobrazuje a ovlada detsku vysielacku */
public class RadioActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener {


    private ImageButton btStartL;
    private ImageButton btStopL;
    private Intent mServiceIntent;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio);
        mServiceIntent = new Intent(this, NoiseDecService.class);


        toolbar =(Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Spinner spinner = (Spinner) findViewById(R.id.spinner_nav);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.action_list_reversed, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);



        btStartL = (ImageButton) findViewById(R.id.imageButton);
        btStopL = (ImageButton) findViewById(R.id.imageButton2);

        if(isMyServiceRunning(NoiseDecService.class)) btStartL.setPressed(true);

            btStartL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                if (!isMyServiceRunning(NoiseDecService.class)) {
                    btStartL.setPressed(true);
                   startService(mServiceIntent);
                } else Toast.makeText(getApplicationContext(),"UZ POCUVAM", Toast.LENGTH_SHORT).show();


            }
        });

        btStopL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(mServiceIntent);


            }
        });

    }
    /** Kontrola behu detekcie placu */
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_radio, menu);
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
            Intent delIntent = new Intent(this, SettingsActivity.class);
            startActivity(delIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // inicializacia prepinaca medzi spankovym monitorom a detskou vysielackou
        Spinner spinner = (Spinner) findViewById(R.id.spinner_nav);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.action_list_reversed, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if(position == 1){
            startActivity(new Intent(this,MainActivity.class));
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
