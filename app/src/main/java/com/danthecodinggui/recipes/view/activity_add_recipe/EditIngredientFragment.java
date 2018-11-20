package com.danthecodinggui.recipes.view.activity_add_recipe;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.danthecodinggui.recipes.R;
import com.danthecodinggui.recipes.model.object_models.Ingredient;
import com.danthecodinggui.recipes.msc.utility.Utility;

import static com.danthecodinggui.recipes.msc.GlobalConstants.INGREDIENT_OBJECT;

/**
 * Shows simple dialog to edit an ingredient
 */
public class EditIngredientFragment extends DialogFragment {

    private onIngredientEditedListener callback;
    private int ingredientPosision;

    private EditText editQuantity;
    private Spinner editMeasurement;
    private EditText editIngredient;

    private boolean isQuantityEmpty = false;
    private boolean isIngredientEmpty = false;

    public void SetIngredientsListener(onIngredientEditedListener callback, int position) {
        this.callback = callback;
        this.ingredientPosision = position;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(R.layout.fragment_edit_ingredient)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Ingredient ingredient = new Ingredient(
                                editIngredient.getText().toString(),
                                Integer.parseInt(editQuantity.getText().toString()),
                                editMeasurement.getSelectedItem().toString()
                        );
                        callback.onIngredientEdited(ingredient, ingredientPosision);
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Utility.setKeyboardVisibility(getActivity(), editIngredient, false);
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
        Ingredient i = args.getParcelable(INGREDIENT_OBJECT);

        editQuantity = getDialog().findViewById(R.id.etxt_edit_ingredient_quantity);
        editQuantity.setText(Integer.toString(i.getQuantity()));
        editQuantity.setSelection(editQuantity.getText().length());

        editMeasurement = getDialog().findViewById(R.id.spn_edit_ingredient_measurement);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.default_ingredient_measurements, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        editMeasurement.setAdapter(spinnerAdapter);
        editMeasurement.setSelection(spinnerAdapter.getPosition(i.getMeasurement()));

        editIngredient = getDialog().findViewById(R.id.etxt_edit_ingredient_name);
        editIngredient.setText(i.getIngredientText());
        editIngredient.setSelection(editIngredient.getText().length());

        editIngredient.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isIngredientEmpty = charSequence.length() == 0;

                CheckButtonEnabled();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        editQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isQuantityEmpty = charSequence.length() == 0;

                CheckButtonEnabled();
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    /**
     * Check is the ingredient data is empty, if so it will disable the dialog's OK button
     */
    private void CheckButtonEnabled() {
        Button positiveButton = ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
        if (isIngredientEmpty || isQuantityEmpty)
            positiveButton.setEnabled(false);
        else
            positiveButton.setEnabled(true);
    }


    /**
     * Callback interface to alert implementors when the EditIngredientFragment has a result.<br/>
     * IMPORTANT: implementors must call {@link #SetIngredientsListener(onIngredientEditedListener, int) SetIngredientsListener}
     * when instantiating the fragment
     */
    public interface onIngredientEditedListener {

        /**
         * Called when the DialogFragment's 'OK' is clicked, ie. it has a result
         * @param editedIngredient The edited ingredient
         * @param position  The position of the step edited (for view)
         */
        void onIngredientEdited(Ingredient editedIngredient, int position);
    }
}
