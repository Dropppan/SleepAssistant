package com.bp.droppa.sleepassistant.settings;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bp.droppa.sleepassistant.R;

import java.io.IOException;

/**
 * Created by Johnny on 25.3.2015.
 */
public class MicTestActivity extends Activity  implements SeekBar.OnSeekBarChangeListener{

    private SeekBar seekBar;
    private SharedPreferences sharedPreferences;
    private MyTask myTask;
    private MediaRecorder mRecorder;
    private boolean taskRunning;
    private Button btOK;
    private Button btCancel;
    private int tempProgres;
    private TextView textViewCur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mic_test);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setTitle("Sensitivity Set");

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        //nahravanie bez ukladania
        mRecorder.setOutputFile("/dev/null");

        textViewCur = (TextView) findViewById(R.id.textView_Current);


        try {
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        btOK = (Button) findViewById(R.id.buttonOK);
        btCancel = (Button) findViewById(R.id.buttonCanc);




        seekBar = (SeekBar) findViewById(R.id.seekBar_sens);
        seekBar.setOnSeekBarChangeListener(this);

        seekBar.setProgress(sharedPreferences.getInt("mic_sens",0));

        taskRunning = true;
        myTask = new MyTask();
        myTask.execute();

        btOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("mic_sens", tempProgres);

                editor.apply();
                taskRunning = false;
                finish();
            }
        });

        btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskRunning = false;
                finish();

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        taskRunning = false;
        mRecorder.release();
    }




    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        textViewCur.setText(String.valueOf(progress));
        tempProgres = progress;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }



    class MyTask extends AsyncTask<Void,Integer,Boolean> {

        @Override
        protected void onProgressUpdate(Integer[] values) {
            super.onProgressUpdate(values);
            seekBar.setSecondaryProgress(values[0]);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            mRecorder.start();

            while (taskRunning) {
                publishProgress(mRecorder.getMaxAmplitude());

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}
