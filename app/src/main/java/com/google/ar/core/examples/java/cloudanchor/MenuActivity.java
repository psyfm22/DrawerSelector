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

public class MenuActivity extends AppCompatActivity {
    private DisplayRotationHelper displayRotationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        displayRotationHelper = new DisplayRotationHelper(this);

        //Assign and initialise the buttons
        Button confirmLocationB = findViewById(R.id.mainConfirmLocationB),
                closeB = findViewById(R.id.mainCloseB);

        confirmLocationB.setOnClickListener(view -> {
            startQRCodeScan();
        });


        closeB.setOnClickListener(view -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayRotationHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        displayRotationHelper.onPause();
    }
    private void startQRCodeScan() {
        ScanOptions scanOptions = new ScanOptions();
        scanOptions.setPrompt("Scan the QR Code");
        scanOptions.setBeepEnabled(true);
        scanOptions.setOrientationLocked(true);
        scanOptions.setCaptureActivity(CustomCaptureActivity.class);
        launcher.launch(scanOptions);
    }

    ActivityResultLauncher<ScanOptions> launcher = registerForActivityResult(new ScanContract(), result->{
        if(result.getContents() != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
            if(result.getContents().equals("Starting Location")){
                builder.setTitle("Correct QR Code");
                builder.setMessage("Press Okay to Start AR activity");
                builder.setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    Intent intent = SelectActivity.newIntent(MenuActivity.this);
                    startActivity(intent);
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();

            }else{
                builder.setTitle("Incorrect QR Code");
                builder.setMessage("Please Scan the Starting QR Code");
                builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                AlertDialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
            }
        }
    });
}