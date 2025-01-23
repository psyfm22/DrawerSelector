package com.google.ar.core.examples.java.cloudanchor;

import android.os.Bundle;

import com.journeyapps.barcodescanner.CaptureActivity;

/**
 * CustomCaptureActivity,
 * Simple activity extending capture activity to be used by QR codes. Using this extension allows
 * for rotation within the activity
 */
public class CustomCaptureActivity extends CaptureActivity {

    /**
     * onCreate,
     * Standard
     *
     * @param savedInstanceState N/A
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}