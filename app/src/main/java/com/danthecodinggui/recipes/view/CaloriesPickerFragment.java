package com.danthecodinggui.recipes.view;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.AlertDialog;
import android.view.WindowManager;
import android.widget.EditText;

import com.danthecodinggui.recipes.R;

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
                        if (!kcal.equals(""))
                            callback.onCaloriesSet(Integer.parseInt(kcal));
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                })
                .create();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        editKcal = getDialog().findViewById(R.id.etxt_add_kcal);
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
