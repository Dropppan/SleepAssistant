package com.bp.droppa.sleepassistant.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import com.bp.droppa.sleepassistant.R;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;


public class TimePreference extends DialogPreference {
    private long length;
    private Calendar calendar;
    private TimePicker picker = null;

    public TimePreference(Context ctxt) {
        this(ctxt, null);
    }

    public TimePreference(Context ctxt, AttributeSet attrs) {
        this(ctxt, attrs, android.R.attr.dialogPreferenceStyle);
    }

    public TimePreference(Context ctxt, AttributeSet attrs, int defStyle) {
        super(ctxt, attrs, defStyle);

        setPositiveButtonText(R.string.pref_time_ok);
        setNegativeButtonText(R.string.pref_time_cancel);

    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext());
        picker.setIs24HourView(true);
        return (picker);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        picker.setCurrentHour(8);
        picker.setCurrentMinute(0);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            length = picker.getCurrentHour() * 3600000 + picker.getCurrentMinute() * 60000;
//            calendar.set(Calendar.HOUR_OF_DAY, picker.getCurrentHour());
//            calendar.set(Calendar.MINUTE, picker.getCurrentMinute());

            setSummary(getSummary());
            if (callChangeListener(length)) {
                persistLong(length);
                notifyChanged();
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

        if (restoreValue) {
            length = getPersistedLong(TimeUnit.HOURS.toMillis(8));
        } else {
            length = Long.parseLong((String) defaultValue);
            persistLong(length);
        }
        setSummary(getSummary());
    }

//    @Override
//    public CharSequence getSummary() {
//        if (calendar == null) {
//            return null;
//        }
//        return DateFormat.getTimeFormat(getContext()).format(new Date(calendar.getTimeInMillis()));
//    }
}
