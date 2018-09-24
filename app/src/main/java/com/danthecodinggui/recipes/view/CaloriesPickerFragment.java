package com.danthecodinggui.recipes.view;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.AlertDialog;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.danthecodinggui.recipes.R;

public class CaloriesPickerFragment extends DialogFragment {

    private onCaloriesSetListener callback;

    private EditText editKcal;

    public void SetCaloriesListener(onCaloriesSetListener callback) {
        this.callback = callback;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle("Calories Per Person")
                .setView(R.layout.fragment_kcal_picker)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        callback.onCaloriesSet(Integer.parseInt(editKcal.getText().toString()));
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
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
