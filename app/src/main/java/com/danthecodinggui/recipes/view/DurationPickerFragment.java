package com.danthecodinggui.recipes.view;

import mobi.upod.timedurationpicker.TimeDurationPicker;
import mobi.upod.timedurationpicker.TimeDurationPickerDialogFragment;
import mobi.upod.timedurationpicker.TimeDurationUtil;

/**
 * Dialog for picking the time to make a recipe
 */
public class DurationPickerFragment extends TimeDurationPickerDialogFragment {

    private onDurationSetListener callback;

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

    public interface onDurationSetListener {
        void onDurationSet(int minutes);
    }
}
