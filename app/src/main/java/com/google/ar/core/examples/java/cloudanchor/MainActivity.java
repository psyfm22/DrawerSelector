package com.google.ar.core.examples.java.cloudanchor;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.examples.java.common.helpers.DisplayRotationHelper;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class MainActivity extends AppCompatActivity {
    private DisplayRotationHelper displayRotationHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        displayRotationHelper = new DisplayRotationHelper(this);

        //Assign and initialise the buttons
        Button confirmLocationB = findViewById(R.id.mainConfirmLocationB),
                closeB = findViewById(R.id.mainCloseB);

        //Add the listeners
        confirmLocationB.setOnClickListener(view -> startQRCodeScan());
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
        scanOptions.setPrompt(getString(R.string.scan));
        scanOptions.setBeepEnabled(true);
        scanOptions.setOrientationLocked(false);
        scanOptions.setCaptureActivity(CustomCaptureActivity.class);
        launcher.launch(scanOptions);
    }

    ActivityResultLauncher<ScanOptions> launcher = registerForActivityResult(new ScanContract(), result->{
        if(result.getContents() != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            if(result.getContents().equals(getString(R.string.main_qr_code_value))){
                builder.setTitle(getString(R.string.main_correct_qr_title));
                builder.setMessage(getString(R.string.main_correct_qr_description));
                builder.setPositiveButton(getString(R.string.okay), (dialog, which) -> {
                    dialog.dismiss();
                    Intent intent = ResolveLobbyActivity.newIntent(MainActivity.this);
                    startActivity(intent);
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
            }else{
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