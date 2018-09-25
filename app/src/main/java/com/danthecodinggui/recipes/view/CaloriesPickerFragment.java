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
                        callback.onCaloriesSet(Integer.parseInt(editKcal.getText().toString()));
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

    public interface onCaloriesSetListener {
        void onCaloriesSet(int kcal);
    }
}
