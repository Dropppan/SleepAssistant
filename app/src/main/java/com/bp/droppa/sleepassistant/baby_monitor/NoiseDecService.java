package com.bp.droppa.sleepassistant.baby_monitor;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.bp.droppa.sleepassistant.R;
import com.bp.droppa.sleepassistant.sleep_monitor.MainActivity;

import java.io.IOException;

/**
 * Created by Roman Droppa on 12.3.2015.
 * Monituruje urovnen okliteho hluku a uskotucnuje hovor.
 */
public class NoiseDecService extends IntentService {

    private MediaRecorder mRecorder;
    private boolean running;
    private String number;
    private int sensitivity;
    private int sum;
    private int avg;
    private TelephonyManager telephony;
    private PhoneStateChangeListener phoneListener;
    private boolean notInCall;


    @Override
    public void onCreate() {
        super.onCreate();

        // Vytvorenie notifikacie pre tuto sluzbu
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Sleep Assistant")
                        .setContentText("Odposluch je spusteny");
        Intent notificationIntentAL = new Intent(this, MainActivity.class);
        notificationIntentAL.putExtra("com.example.johnny.sleepassistant.fragmentIndex", 2);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntentAL, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);

        //Zabezpecuje funkcnost aj s vypnutou obrazovkou
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        wakeLock.acquire();

        // Nacitanie potrebnych nastavenie
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        number = sharedPref.getString("ParentsNum", "0000000000");
        sensitivity = sharedPref.getInt("mic_sens", 50);

        phoneListener = new PhoneStateChangeListener();
        telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        // Spustenie sluzby v popredi
        startForeground(2, mBuilder.build());

    }


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public NoiseDecService(String name, MediaRecorder mRecorder) {
        super(name);
    }

    public NoiseDecService() {
        super("NoiseDecService");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        running = false;
        sum = 0;
        // uvolnenie MediaRecorder
        mRecorder.stop();
        mRecorder.release();
        // Zrusenie kontroly hovorov
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_NONE);
    }

    /**
     * Spusta zaznam zvuku miktorfonom
     */
    public void startListening() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        //nahravanie bez ukladania
        mRecorder.setOutputFile("/dev/null");

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRecorder.start();
    }



    @Override
    protected void onHandleIntent(Intent intent) {

        telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

        startListening();

        int count = 0;
        notInCall = true;
        running = true;

        while (running) {

            if (notInCall) {

                int maxAmp = mRecorder.getMaxAmplitude();
                sum += maxAmp;
                count++;
                // hluk musi presahovat hranicu po dobu 3 sekund
                if (count >= 10) {
                    avg = sum / count;
                    count = 0;
                    sum = 0;
                }

                //  ak je hladina hluku vyssia ako prednastavena citlivost uskutocni sa hovor
                if (avg > sensitivity) {

                    mRecorder.stop();
                    mRecorder.release();

                    avg = 0;
                    // implicitny zamer uskutocny hovor
                    Intent callIntent = new Intent();
                    callIntent.setAction(Intent.ACTION_CALL);
                    callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    callIntent.setData(Uri.parse("tel: " + number));
                    startActivity(callIntent);
                    notInCall = false;
                }
                // kontrola prebehne kazdych 300 milisekund
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Kontroluje stav telefonu a znovu spusta odposluch
     */
    public class PhoneStateChangeListener extends PhoneStateListener {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {

                // Tato cast sa vykona az po uskutocneni hovoru
                case TelephonyManager.CALL_STATE_IDLE:

                    if (!notInCall) {
                        //  Problem s mikrofonom preto pauza.. cakanie kym sa vypne telefonna app
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        startListening();
                        notInCall = true;
                    }
                    break;
            }
        }
    }


}

