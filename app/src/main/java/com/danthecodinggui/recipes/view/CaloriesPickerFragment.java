package com.danthecodinggui.recipes.view;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.AlertDialog;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.danthecodinggui.recipes.R;
import com.danthecodinggui.recipes.msc.Utility;

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

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_kcal_title)
                .setView(R.layout.fragment_kcal_picker)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String kcal = editKcal.getText().toString();
                        callback.onCaloriesSet(Integer.parseInt(kcal));
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Utility.setKeyboardVisibility(getActivity(), editKcal, false);
                    }
                })
                .create();

        return dialog;
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
                CheckButtonEnabled(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        CheckButtonEnabled(editKcal.getText().toString());
    }

    /**
     * Check is the edit text is empty, if so it will disable the dialog's OK button
     * @param currentText Current EditText value
     */
    private void CheckButtonEnabled(String currentText) {
        Button positiveButton = ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
        if (currentText.isEmpty())
            positiveButton.setEnabled(false);
        else
            positiveButton.setEnabled(true);
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
