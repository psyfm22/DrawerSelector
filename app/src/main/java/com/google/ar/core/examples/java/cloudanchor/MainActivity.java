package com.google.ar.core.examples.java.cloudanchor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.examples.java.common.helpers.DisplayRotationHelper;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

/**
 * MainActivity,
 * Initial Opening Activity for the application. Makes the user scan a qr code to progress to the
 * next activity
 */
public class MainActivity extends AppCompatActivity {
    private DisplayRotationHelper displayRotationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialise the display rotation helper
        displayRotationHelper = new DisplayRotationHelper(this);

        //Assign and initialise the buttons
        Button confirmLocationB = findViewById(R.id.mainConfirmLocationB),
                closeB = findViewById(R.id.mainCloseB);

        //Add the listeners
        confirmLocationB.setOnClickListener(view -> startQRCodeScan());
        closeB.setOnClickListener(view -> finish());
    }

    /**
     * onResume,
     * Resume the display rotation helper
     */
    @Override
    protected void onResume() {
        super.onResume();
        displayRotationHelper.onResume();
    }

    /**
     * onPause,
     * Pause the display rotation helper
     */
    @Override
    public void onPause() {
        super.onPause();
        displayRotationHelper.onPause();
    }

    /**
     * startQRCodeScan,
     * Start the scanning of the qr code using the Custom Capture Activity
     */
    private void startQRCodeScan() {
        //Create instance of scan options
        ScanOptions scanOptions = new ScanOptions();

        //Set the prompt at the bottom to the scan string
        scanOptions.setPrompt(getString(R.string.scan));

        //Means a beep will sound when a qr code is scanned
        scanOptions.setBeepEnabled(true);

        //Allow the user to rotate the activity
        scanOptions.setOrientationLocked(false);

        //Set activity to the custom capture activity which extends CaptureActivity and then start
        //activity using launcher
        scanOptions.setCaptureActivity(CustomCaptureActivity.class);
        launcher.launch(scanOptions);
    }

    //Define the launcher
    ActivityResultLauncher<ScanOptions> launcher = registerForActivityResult(new ScanContract(), result->{
        if(result.getContents() != null){

            //Initialise the alert dialog builder
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            //If the ar code scanned equals the string for the starting qr then we know the user is
            //in the correct location
            if(result.getContents().equals(getString(R.string.main_qr_code_value))){

                //Set the title, message and defined what happens on okay pressed
                builder.setTitle(getString(R.string.main_correct_qr_title));
                builder.setMessage(getString(R.string.main_correct_qr_description));
                builder.setPositiveButton(getString(R.string.okay), (dialog, which) -> {
                    //When the dialogue is dismissed the the intent is launched
                    dialog.dismiss();
                    Intent intent = ResolveLobbyActivity.newIntent(MainActivity.this);
                    startActivity(intent);
                });
                //Create the alert dialog and show it. We set canceled on the outside false so that
                //the user progress
                AlertDialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
            }else{
                //Set the alert up for when the user has scanned an incorrect qr code
                builder.setTitle(getString(R.string.incorrect_qr_title));
                builder.setMessage(getString(R.string.main_incorrect_qr_description));
                builder.setPositiveButton(getString(R.string.okay), (dialog, which) -> dialog.dismiss());
                AlertDialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
            }
        }
    });
}