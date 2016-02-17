package com.bp.droppa.sleepassistant.sleep_monitor;




import android.content.Intent;
import android.os.Bundle;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.bp.droppa.sleepassistant.R;
import com.bp.droppa.sleepassistant.baby_monitor.RadioActivity;
import com.bp.droppa.sleepassistant.settings.SettingsActivity;
import com.bp.droppa.sleepassistant.sleep_monitor.tabs.SlidingTabLayout;

/** Zobrazuje fragmenty kalendar a budik */
public class MainActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener {

    private ViewPager mPager;
    private SlidingTabLayout mTabs;

    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        toolbar =(Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Spinner spinner = (Spinner) findViewById(R.id.spinner_nav);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.action_list, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        if(MoveDecService.running){
            startActivity(new Intent(this,SleepActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_NO_HISTORY));
            finish();
        }
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
        mTabs = (SlidingTabLayout) findViewById(R.id.tabs);
        mTabs.setDistributeEvenly(true);
        mTabs.setViewPager(mPager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent delIntent = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivity(delIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if(position == 1){

            startActivity(new Intent(this,RadioActivity.class));
        }


    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

        class MyAdapter extends FragmentPagerAdapter {


        private String[] tabs;

        public MyAdapter(FragmentManager fm) {
            super(fm);
            tabs = getResources().getStringArray(R.array.tabs);

        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = null;
            if (i == 0) {
                 fragment = new FragmetTimePicker();
            }
            if (i == 1) {

                fragment = new FragmentCalendar();
            }

            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {

        return tabs[position];

        }
    }



}

