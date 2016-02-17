package com.bp.droppa.sleepassistant.sleep_monitor;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.bp.droppa.sleepassistant.R;

/** Zobrazuje sa v priebehu monitorovanie spanku */
public class SleepActivity extends ActionBarActivity {

    private Button btStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep);

        btStop = (Button) findViewById(R.id.bt_stop);
         // tlacidlo na prerusenie monitorovania
        btStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MoveDecService.running = false;
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });

    }

}
