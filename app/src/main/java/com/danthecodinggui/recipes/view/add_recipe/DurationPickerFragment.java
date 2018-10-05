package com.danthecodinggui.recipes.view.add_recipe;

import mobi.upod.timedurationpicker.TimeDurationPicker;
import mobi.upod.timedurationpicker.TimeDurationPickerDialogFragment;
import mobi.upod.timedurationpicker.TimeDurationUtil;

/**
 * Dialog for picking the time to make a recipe
 */
public class DurationPickerFragment extends TimeDurationPickerDialogFragment {

    private onDurationSetListener callback;

    /**
     * Sets the callback method to be called when the dialog has a result
     */
    public void SetDurationListener(onDurationSetListener callback) {
        this.callback = callback;
    }

    @Override
    public void onDurationSet(TimeDurationPicker view, long duration) {
        if (callback != null && duration >= 60)
            callback.onDurationSet(TimeDurationUtil.minutesOf(duration));
    }

    @Override
    protected int setTimeUnits() {
        return TimeDurationPicker.HH_MM;
    }

    /**
     * Callback interface to alert implementors when the DurationPickerFragment has a result.<br/>
     * IMPORTANT: implementors must call {@link #SetDurationListener(onDurationSetListener) SetDurationListener}
     * when instantiating the fragment
     */
    public interface onDurationSetListener {

        /**
         * Called when the DialogFragment's 'OK' is clicked, ie. it has a result
         * @param minutes  The duration entered in minutes by the dialog ie. the result of the dialog
         */
        void onDurationSet(int minutes);
    }
}
