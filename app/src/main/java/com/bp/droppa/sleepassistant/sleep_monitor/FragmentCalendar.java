package com.bp.droppa.sleepassistant.sleep_monitor;


import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bp.droppa.sleepassistant.R;
import com.bp.droppa.sleepassistant.database.StampStorage;
import com.bp.droppa.sleepassistant.database.StampStorageHelper;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/** Zobrazuje kalendar zaznamov, celkove a mesacne udaje */
public class FragmentCalendar extends Fragment {

    private CaldroidFragment caldroidFragment;
    private StampStorageHelper mStampStorageHelper;
    private MyTask myTask;
    private Calendar cal;
    private ArrayList<Date> disDates;
    private TextView twTNights;
    private TextView twTHours;
    private TextView twTQual;
    private TextView twTDefic;
    private TextView twMHours;
    private TextView twMQual;
    private TextView twMDefic;
    private TextView twMNights;

    private long sleepLength;

    public FragmentCalendar() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fragment_calendar, container, false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mStampStorageHelper = new StampStorageHelper(getActivity());

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sleepLength = sharedPreferences.getLong("lengthPreference",8);

        disDates = new ArrayList<>();

        Bundle args = new Bundle();
        cal = Calendar.getInstance();

        twTHours = (TextView) getActivity().findViewById(R.id.tw_ttib_val);
        twTNights = (TextView) getActivity().findViewById(R.id.tw_tn_val);
        twTDefic = (TextView) getActivity().findViewById(R.id.tw_td_val);
        twTQual = (TextView) getActivity().findViewById(R.id.tw_tq_val);

        twMHours = (TextView) getActivity().findViewById(R.id.tw_mtib_val);
        twMDefic = (TextView) getActivity().findViewById(R.id.tw_md_val);
        twMNights = (TextView) getActivity().findViewById(R.id.tw_mn_val);
        twMQual = (TextView) getActivity().findViewById(R.id.tw_mq_val);

        caldroidFragment = new CaldroidFragment();

        // Pociatocne nastavenie Caldroid kalendara
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        args.putInt(CaldroidFragment.START_DAY_OF_WEEK, CaldroidFragment.MONDAY);
        args.putBoolean(CaldroidFragment.ENABLE_SWIPE, false);


        caldroidFragment.setArguments(args);

        FragmentTransaction t = getChildFragmentManager().beginTransaction();
        t.replace(R.id.calendar, caldroidFragment).commit();

        // Listener pre zmenu mesiaca v kalendari a volbu datumu
        final CaldroidListener listener = new CaldroidListener() {

            @Override
            public void onChangeMonth(int month, int year) {
                super.onChangeMonth(month, year);
                cal.setTime(new Date());
                cal.set(Calendar.MONTH, month-1);

                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                // inicializacia vypnutych datumov
                for (int i = 1; i <= cal.getActualMaximum(Calendar.DAY_OF_MONTH); i++) {
                    cal.set(Calendar.DAY_OF_MONTH, i);
                    disDates.add(cal.getTime());
                }
                myTask = new MyTask();
                myTask.execute();

            }

            @Override
            public void onSelectDate(Date date, View view) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                //test ci je vybrany datu v aktualnom mesiaci
                if(calendar.get(Calendar.MONTH)==cal.get(Calendar.MONTH)) {
                    Intent intent = new Intent(getActivity(), StatsActivity.class);
                    intent.putExtra("date", date.getTime());
                    startActivity(intent);
                }
            }
        };

        caldroidFragment.setCaldroidListener(listener);

    }

    @Override
    public void onResume() {
        super.onResume();

    }

/** Nacitava potrebne udaje z DB a naplna nimi textove polia */
    private class MyTask extends AsyncTask {

        private int mNights;
        private long mTime;
        private long mDeficit;
        private double mQuality;

        private int tNights;
        private long tTime;
        private long tDeficit;
        private double tQuality;


        private static final long MILIS_IN_HOUR = 3600000;

        double elapsedMHours;
        double deficitMHours;
        double elapsedTHours;
        double deficitTHours;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            caldroidFragment.clearDisableDates();
        }

        @Override
        protected Object doInBackground(Object[] params) {

            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
            String firsDayOfMonth = String.valueOf(cal.getTimeInMillis());

            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            String lastDayOfMonth = String.valueOf(cal.getTimeInMillis());


            //inicializacia mesacnich hodnot
            SQLiteDatabase db = mStampStorageHelper.getReadableDatabase();
            String[] projection = {StampStorage.Days.COLUMN_NAME_DATE, StampStorage.Days.COLUMN_NAME_QUALITY, StampStorage.Days.COLUMN_NAME_DURATION};
            String selection = StampStorage.Days.COLUMN_NAME_DATE + " BETWEEN ? AND ? ";
            String[] selectionArg = {firsDayOfMonth, lastDayOfMonth};
            Cursor c = db.query(
                    StampStorage.Days.TABLE_NAME,
                    projection,
                    selection,
                    selectionArg,
                    null,
                    null,
                    null
            );
            c.moveToFirst();
            //vypnutie datumov bez merania
            if (c.getCount() > 0) {
                do {
                    mDeficit +=c.getInt(c.getColumnIndexOrThrow(StampStorage.Days.COLUMN_NAME_DURATION))-sleepLength;
                    mTime +=c.getInt(c.getColumnIndexOrThrow(StampStorage.Days.COLUMN_NAME_DURATION));
                    mQuality +=c.getDouble(c.getColumnIndexOrThrow(StampStorage.Days.COLUMN_NAME_QUALITY));

                    long lDate = c.getLong(c.getColumnIndexOrThrow(StampStorage.Days.COLUMN_NAME_DATE));
                    Date date = new Date(lDate);
                    disDates.remove(date);
                    caldroidFragment.setBackgroundResourceForDate(R.color.colorAccent, date);

                }
                while (c.moveToNext());

            }
            elapsedMHours = mTime/MILIS_IN_HOUR;
            deficitMHours = mDeficit/MILIS_IN_HOUR;
            mQuality = mQuality/c.getCount()*100;
            mNights = c.getCount();

            //inicializacia celkovych hodnot
            String selectionT = StampStorage.Days.COLUMN_NAME_DATE;
            c = db.query(
                    StampStorage.Days.TABLE_NAME,
                    projection,
                    null,
                    null,
                    null,
                    null,
                    null
            );
            c.moveToFirst();
            if (c.getCount() > 0) {
                do {

                    tDeficit +=c.getInt(c.getColumnIndexOrThrow(StampStorage.Days.COLUMN_NAME_DURATION))-sleepLength;
                    tTime +=c.getInt(c.getColumnIndexOrThrow(StampStorage.Days.COLUMN_NAME_DURATION));
                    tQuality +=c.getDouble(c.getColumnIndexOrThrow(StampStorage.Days.COLUMN_NAME_QUALITY));

                }
                while (c.moveToNext());

            }
            elapsedTHours = tTime/MILIS_IN_HOUR;
            deficitTHours = tDeficit/MILIS_IN_HOUR;
            tQuality = tQuality/c.getCount()*100;
            tNights = c.getCount();
                return null;


        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            twTHours.setText(String.format("%.1f h",elapsedTHours));
            twTQual.setText(String.format("%.1f %%",tQuality));
            twTDefic.setText(String.format("%.1f h",deficitTHours));
            twTNights.setText(String.valueOf(tNights));

            twMQual.setText(String.format("%.1f %%",mQuality));
            twMNights.setText(String.valueOf(mNights));
            twMHours.setText(String.format("%.1f h",elapsedMHours));
            twMDefic.setText(String.format("%.1f h",deficitMHours));

            caldroidFragment.setDisableDates(disDates);
            disDates.clear();
        }
    }
}
