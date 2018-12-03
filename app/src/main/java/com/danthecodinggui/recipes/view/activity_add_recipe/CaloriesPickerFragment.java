package com.danthecodinggui.recipes.view.activity_add_recipe;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.danthecodinggui.recipes.R;
import com.danthecodinggui.recipes.msc.utility.Utility;

/**
 * Shows simple dialog to enter number of kcal per person for a recipe
 */
public class CaloriesPickerFragment extends DialogFragment {

    private onCaloriesSetListener callback;

    private EditText editKcal;

    /**
     * Sets the callback method to be called when the dialog has a result
     */
    public void SetCaloriesListener(onCaloriesSetListener callback) {
        this.callback = callback;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_kcal_title)
                .setView(R.layout.fragment_kcal_picker)
                .setPositiveButton(R.string.dialog_ok, (dialogInterface, i) -> {
                        String kcal = editKcal.getText().toString();
                        callback.onCaloriesSet(Integer.parseInt(kcal));
                        Utility.setKeyboardVisibility(getActivity(), editKcal, false);
                    }
                )
                .setNegativeButton(R.string.dialog_cancel, (dialogInterface, i) ->
                        Utility.setKeyboardVisibility(getActivity(), editKcal, false)
                )
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();

        editKcal = getDialog().findViewById(R.id.etxt_add_kcal);
        Utility.setKeyboardVisibility(getActivity(), editKcal, true);
        editKcal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Utility.CheckButtonEnabled(((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE),
                        charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        Utility.CheckButtonEnabled(((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE),
                editKcal.getText().toString());
    }

    @Override
    public void onStop() {
        super.onStop();
        Utility.setKeyboardVisibility(getActivity(), editKcal, false);
    }

    /**
     * Callback interface to alert implementors when the CaloriesPickerFragment has a result.<br/>
     * IMPORTANT: implementors must call {@link #SetCaloriesListener(onCaloriesSetListener) SetCaloriesListener}
     * when instantiating the fragment
     */
    public interface onCaloriesSetListener {

        /**
         * Called when the DialogFragment's 'OK' is clicked, ie. it has a result
         * @param kcal  The quantity of calories entered in the dialog ie. the result of the dialog
         */
        void onCaloriesSet(int kcal);
    }
}
