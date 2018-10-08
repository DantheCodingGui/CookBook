package com.danthecodinggui.recipes.view.add_recipe;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.widget.EditText;

import com.danthecodinggui.recipes.R;
import com.danthecodinggui.recipes.msc.Utility;

import static com.danthecodinggui.recipes.msc.GlobalConstants.METHOD_STEP_OBJECT;

/**
 * Shows simple dialog to edit an method step
 */
public class EditMethodStepFragment extends DialogFragment {

    private onStepEditedListener callback;
    int stepPosition;

    private EditText editStep;

    public void SetStepListener(onStepEditedListener callback, int position) {
        this.callback = callback;
        this.stepPosition = position;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(R.layout.fragment_edit_step)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String editedStep = editStep.getText().toString();
                        callback.onStepEdited(editedStep, stepPosition);
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Utility.setKeyboardVisibility(getActivity(), editStep, false);
                    }
                })
                .create();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        Bundle args = getArguments();
        String stepText = args.getString(METHOD_STEP_OBJECT);

        editStep = getDialog().findViewById(R.id.etxt_edit_step);
        editStep.setText(stepText);
        editStep.setSelection(editStep.getText().length());

        editStep.addTextChangedListener(new TextWatcher() {
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
                editStep.getText().toString());
    }


    /**
     * Callback interface to alert implementors when the EditStepFragment has a result.<br/>
     * IMPORTANT: implementors must call {@link #SetStepListener(onStepEditedListener, int) SetStepListener}
     * when instantiating the fragment
     */
    public interface onStepEditedListener {

        /**
         * Called when the DialogFragment's 'OK' is clicked, ie. it has a result
         * @param stepText  The new text of the step
         * @param stepText  The new text of the step
         */
        void onStepEdited(String stepText, int position);
    }
}
