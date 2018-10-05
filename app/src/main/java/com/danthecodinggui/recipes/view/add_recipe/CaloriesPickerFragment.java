package com.danthecodinggui.recipes.view.add_recipe;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
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
                Utility.CheckButtonEnabled(((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE),
                        charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        Utility.CheckButtonEnabled(((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE),
                editKcal.getText().toString());
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
