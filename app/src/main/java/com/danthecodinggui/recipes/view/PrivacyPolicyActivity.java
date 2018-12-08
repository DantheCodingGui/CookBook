package com.danthecodinggui.recipes.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.danthecodinggui.recipes.R;

/**
 * Simple activity housing a webview pointing to the Google Play required privacy policy
 */
public class PrivacyPolicyActivity extends AppCompatActivity {

    private static final String PRIVACY_POLICY_URL = "https://sites.google.com/view/cookbook-privacy-policy";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        WebView webview = findViewById(R.id.wvw_privacy_policy);

        webview.setWebViewClient(new WebViewClient());
        webview.getSettings().setDomStorageEnabled(true);
        webview.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        webview.loadUrl(PRIVACY_POLICY_URL);

        setSupportActionBar(findViewById(R.id.tbar_privacy_policy));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}
