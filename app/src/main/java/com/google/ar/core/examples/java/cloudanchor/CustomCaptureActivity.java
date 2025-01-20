package com.google.ar.core.examples.java.cloudanchor;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.google.ar.core.examples.java.common.helpers.DisplayRotationHelper;
import com.journeyapps.barcodescanner.CaptureActivity;

public class CustomCaptureActivity extends CaptureActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        displayRotationHelper = new DisplayRotationHelper(this);

//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}