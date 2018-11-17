package com.danthecodinggui.recipes.view.activity_add_recipe;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import com.danthecodinggui.recipes.R;
import com.danthecodinggui.recipes.msc.utility.Utility;

/**
 * Shows simple dialog to enter number of kcal per person for a recipe
 */
public class AddImageURLFragment extends DialogFragment {

    private onURLSetListener callback;

    private EditText editURL;

    /**
     * Sets the callback method to be called when the dialog has a result
     */
    public void SetURLListener(onURLSetListener callback) {
        this.callback = callback;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_url_title)
                .setView(R.layout.fragment_imageurl_picker)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String URL = editURL.getText().toString();
                        boolean isValidURL = Patterns.WEB_URL.matcher(URL).matches();
                        boolean isImage = URL.matches("(?:([^:/?#]+):)?(?://([^/?#]*))?([^?#]*\\.(?:jpg|gif|png))(?:\\?([^#]*))?(?:#(.*))?");
                        if (isValidURL && isImage)
                            callback.onURLSet(URL);
                        else if (isValidURL)
                            Toast.makeText(getActivity(), R.string.url_not_image, Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(getActivity(), R.string.url_invalid, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Utility.setKeyboardVisibility(getActivity(), editURL, false);
                    }
                })
                .create();

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();

        editURL = getDialog().findViewById(R.id.etxt_add_image_url);
        Utility.setKeyboardVisibility(getActivity(), editURL, true);
        editURL.addTextChangedListener(new TextWatcher() {
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
                editURL.getText().toString());
    }

    @Override
    public void onStop() {
        super.onStop();
        Utility.setKeyboardVisibility(getActivity(), editURL, false);
    }

    /**
     * Callback interface to alert implementors when the ImageURLFragment has a result.<br/>
     * IMPORTANT: implementors must call {@link #SetURLListener(onURLSetListener) SetURLListener}
     * when instantiating the fragment
     */
    public interface onURLSetListener {

        /**
         * Called when the DialogFragment's 'OK' is clicked, ie. it has a result
         * @param url  The URL of the image to be added
         */
        void onURLSet(String url);
    }
}
