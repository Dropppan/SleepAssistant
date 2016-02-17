package com.bp.droppa.sleepassistant.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import android.preference.Preference;


import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;


//import android.support.v4.preference.PreferenceFragment;

import android.widget.Toast;

import com.bp.droppa.sleepassistant.R;


/**
 * Created by Johnny on 17.3.2015.
 */
/** Zobrazuje ponuku nastaveni */
public class FragmentSettings extends PreferenceFragment {

    private Preference conPickPreference;
    private Preference calibrationPreference;
    private SharedPreferences sharedPreferences;
    public FragmentSettings() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //nacita preference z xml
        addPreferencesFromResource(R.xml.preferences);


        conPickPreference = findPreference("numberPreference");

        conPickPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(contactPickerIntent, 22);
                return false;
            }
        });


        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        conPickPreference.setSummary("Current number: "+sharedPreferences.getString("ParentsName","not set"));

        Preference sensSetPreference = (Preference) findPreference("microphonePreference");

        sensSetPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent sensitivityTest = new Intent(getActivity(),MicTestActivity.class);
                startActivity(sensitivityTest);


                return false;
            }
        });


        calibrationPreference = (Preference) findPreference("sensorPreference");

        calibrationPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent calibration = new Intent(getActivity(),CalibrationActivity.class);
                startActivity(calibration);

                return false;
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        calibrationPreference.setSummary("Current sensitivity: "+ (sharedPreferences.getFloat("threshold",0f)==0f ? "Not calibratetd" : String.valueOf(sharedPreferences.getFloat("threshold",0f))));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 22) {

//          ziskanie udajov o zvolenom kontakte
            if (resultCode == getActivity().RESULT_OK) {
                Uri contentUri = data.getData();
                String contactId = contentUri.getLastPathSegment();
                Cursor cursor = getActivity().getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone._ID + "=?",
                        new String[]{contactId}, null);
                getActivity().startManagingCursor(cursor);
                Boolean numbersExist = cursor.moveToFirst();
                int phoneNumberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int contactNameColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                String phoneNumber = "";
                String contactName = "";
                while (numbersExist) {
                    phoneNumber = cursor.getString(phoneNumberColumnIndex);
                    contactName = cursor.getString(contactNameColumnIndex);
                    conPickPreference.setSummary("Current number: "+contactName);
                    phoneNumber = phoneNumber.trim();
                    numbersExist = cursor.moveToNext();
                }
                getActivity().stopManagingCursor(cursor);
                if (!phoneNumber.equals("")) {
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("ParentsName",contactName);
                    editor.putString("ParentsNum", phoneNumber);
                    editor.apply();
                    Toast.makeText(getActivity(), phoneNumber, Toast.LENGTH_LONG).show();
                }
            }
        }

    }




}
