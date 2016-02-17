package com.bp.droppa.sleepassistant.sleep_monitor;


import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TimePicker;

import com.bp.droppa.sleepassistant.R;
import com.bp.droppa.sleepassistant.settings.CalibrationActivity;

import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */

/**
 * Umoznuje nastavenie casu budenia a spustenie zaznamu
 */
public class FragmetTimePicker extends Fragment {

    private Button btSleep;
    private Intent mServiceIntent;
    private long wakeUpTime;
    private TimePicker timePicker;
    private Calendar c;

    public FragmetTimePicker() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_fragmet_cycle, container, false);


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        textView = (TextView) getActivity().findViewById(R.id.textView);


        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor editor = sharedPref.edit();
        // ak este neprebehla kalibracia zobrazi sa upozornenie
        if (sharedPref.getFloat("threshold", 0f) == 0f) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("App started for the first time. Sensor calibration is needed");
            builder.setTitle("First run");
            builder.setPositiveButton(R.string.btOK, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                    startActivity(new Intent(getActivity(), CalibrationActivity.class));
                }
            });

            builder.create().show();

        }

        mServiceIntent = new Intent(getActivity(), MoveDecService.class);

        timePicker = (TimePicker) getActivity().findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        c = Calendar.getInstance();

        c.setTimeInMillis(sharedPref.getLong("lastTime", System.currentTimeMillis()));
        timePicker.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
        timePicker.setCurrentMinute(c.get(Calendar.MINUTE));


        btSleep = (Button) getActivity().findViewById(R.id.bt_sleep);
        btSleep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isMyServiceRunning(MoveDecService.class)) {

                    c.setTimeInMillis(System.currentTimeMillis());
                    c.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
                    c.set(Calendar.MINUTE, timePicker.getCurrentMinute());
                    c.set(Calendar.SECOND, 0);

                    // pridanie dna v pripade ze je nastaveny cas mensi ako aktualny
                    if (c.getTimeInMillis() < System.currentTimeMillis()) {
                        c.add(Calendar.DAY_OF_MONTH, 1);

                        wakeUpTime = c.getTimeInMillis();
                    } else wakeUpTime = c.getTimeInMillis();

                    //ulozenie posledne nastaveneho casu
                    editor.putLong("lastTime", wakeUpTime);
                    editor.apply();
                    mServiceIntent.putExtra("wakeUpTime", wakeUpTime);

                    getActivity().startService(mServiceIntent);

                    //spusti aktivitu a vymaze back stack, cize po stlaceni back sa applikacia vypne
                    startActivity(new Intent(getActivity(), SleepActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY));
                }

            }
        });

    }
    /** Overuje chod zadanej sluzby */
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
