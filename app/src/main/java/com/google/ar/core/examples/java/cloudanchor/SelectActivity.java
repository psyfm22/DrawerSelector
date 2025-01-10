package com.google.ar.core.examples.java.cloudanchor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import com.google.ar.core.examples.java.common.helpers.DisplayRotationHelper;

import org.mindrot.jbcrypt.BCrypt;

public class SelectActivity extends AppCompatActivity {

    private FirebaseManager firebaseManager;

    private DisplayRotationHelper displayRotationHelper;
    private AlertDialog alertDialogue;
    private static final String PASSWORD_CODE = "PASSWORD";


    static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, SelectActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);

        //Assign the rotation helper
        displayRotationHelper = new DisplayRotationHelper(this);

        Button viewB = findViewById(R.id.selectBeginViewingB);
        ImageButton backIB = findViewById(R.id.selectReturnIB);
        ImageView settingsIV = findViewById(R.id.selectSettingsIV);

        firebaseManager = new FirebaseManager(this);


        viewB.setOnClickListener(v -> {
            Intent intent = ResolveAnchorsLobbyActivity.newIntent(SelectActivity.this);
            startActivity(intent);
        });

        settingsIV.setOnClickListener(v -> showAlertDialogue());

        backIB.setOnClickListener(v -> finish());
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
        if(alertDialogue != null){
            alertDialogue.dismiss();
        }
    }

    /**
     * showAlertDialogue, Shows the success of adding the alert dialogue
     */
    private void showAlertDialogue() {
        //Initialise the layouts and views
        View view = LayoutInflater.from(SelectActivity.this).inflate(R.layout.password_alert_dialogue, null, false);

        StringBuilder enteredCode = new StringBuilder();
        EditText enterPasswordET = view.findViewById(R.id.passwordAlertET);
        TextView alertTitleTV = view.findViewById(R.id.passwordAlertTitleTV);

        Button[] keyPadButtons = new Button[10];

        keyPadButtons[0] = view.findViewById(R.id.passwordAlertZeroB);
        keyPadButtons[1] = view.findViewById(R.id.passwordAlertOneB);
        keyPadButtons[2] = view.findViewById(R.id.passwordAlertTwoB);
        keyPadButtons[3] = view.findViewById(R.id.passwordAlertThreeB);
        keyPadButtons[4] = view.findViewById(R.id.passwordAlertFourB);
        keyPadButtons[5] = view.findViewById(R.id.passwordAlertFiveB);
        keyPadButtons[6] = view.findViewById(R.id.passwordAlertSixB);
        keyPadButtons[7] = view.findViewById(R.id.passwordAlertSevenB);
        keyPadButtons[8] = view.findViewById(R.id.passwordAlertEightB);
        keyPadButtons[9] = view.findViewById(R.id.passwordAlertNineB);

        Button enterB = view.findViewById(R.id.passwordAlertEnterB);
        Button deleteB = view.findViewById(R.id.passwordAlertDeleteB);

        GridLayout gridLayout = view.findViewById(R.id.passwordAlertGL);
        Button doneB = view.findViewById(R.id.passwordAlertDoneB);
        TextView descriptionTV = view.findViewById(R.id.passwordAlertDescriptionTV);
        ImageView logoIV = view.findViewById(R.id.passwordAlertLogoIV);


        for(int i=0;i<keyPadButtons.length;i++){
            int finalI = i;
            keyPadButtons[finalI].setOnClickListener(v -> {
                if(enteredCode.length()<4){
                    String number = String.valueOf(finalI);
                    enteredCode.append(number);
                    enterPasswordET.setText(enteredCode.toString());
                }
            });
        }

        deleteB.setOnClickListener(v -> {
            if(enteredCode.length()>0){
                enteredCode.deleteCharAt(enteredCode.length() - 1);
                enterPasswordET.setText(enteredCode.toString());
            }
        });

        //Initialise the builder and the alertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(SelectActivity.this);
        builder.setView(view);
        alertDialogue = builder.create();

        enterB.setOnClickListener(view1 -> {
                    enterB.setEnabled(false);
                    String passwordEntered = enterPasswordET.getText().toString();


                    firebaseManager.checkPasscode(passwordEntered, new FirebaseManager.PasscodeCallback() {
                        @Override
                        public void onSuccess() {

                            Log.d("COMP3018", "On Success");
                            alertDialogue.dismiss();
                            Intent intent = SettingsActivity.newIntent(SelectActivity.this);

                            startActivity(intent);
                        }

                        @Override
                        public void onPasswordUploadFailure(String errorMessage) {
                            Log.d("COMP3018", "Upload Failed");

                            alertTitleTV.setText("Error Uploading");
                            enterPasswordET.setVisibility(View.GONE);
                            gridLayout.setVisibility(View.GONE);

                            doneB.setVisibility(View.VISIBLE);
                            logoIV.setVisibility(View.VISIBLE);
                            descriptionTV.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onPasswordsDiffer() {

                            Log.d("COMP3018", "Passwords Differ");
                            alertTitleTV.setText("Incorrect Password");
                            enterPasswordET.setVisibility(View.GONE);
                            gridLayout.setVisibility(View.GONE);

                            doneB.setVisibility(View.VISIBLE);
                            logoIV.setVisibility(View.VISIBLE);
                            descriptionTV.setVisibility(View.VISIBLE);
                        }
                    });
                });

        doneB.setOnClickListener(view1 -> {
            alertDialogue.dismiss();
        });

        if (alertDialogue.getWindow() != null) {
            alertDialogue.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        //Show the actual alert
        alertDialogue.show();
    }
}