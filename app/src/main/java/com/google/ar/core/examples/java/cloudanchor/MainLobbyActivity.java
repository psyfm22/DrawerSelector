package com.google.ar.core.examples.java.cloudanchor;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.examples.java.common.helpers.DisplayRotationHelper;

public class MainLobbyActivity extends AppCompatActivity {

    static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, MainLobbyActivity.class);
    }
    private AlertDialog alertDialogue;
    private DisplayRotationHelper displayRotationHelper;
    private FirebaseManager firebaseManager;
    private ActivityResultLauncher<Intent> startActivityForResultLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_lobby);

        ImageButton returnIB = findViewById(R.id.mainLobbyReturnIB);
        ImageView settingsIV = findViewById(R.id.mainLobbySettingsIV);

        displayRotationHelper = new DisplayRotationHelper(this);

        Button placeComponentB = findViewById(R.id.mainLobbyPlaceComponentB);
        Button findComponentB = findViewById(R.id.mainLobbyFindComponentB);


        placeComponentB.setOnClickListener(v ->{
            Intent intent = ComponentAssignActivity.newIntent(MainLobbyActivity.this);
            startActivity(intent);
        });

        findComponentB.setOnClickListener(v ->{
            Intent intent = ResolveLobbyActivity.newIntent(MainLobbyActivity.this);
            startActivity(intent);
        });

        //When the settings button is clicked we need to show the alert dialogue with the password
        //for the user to enter
        settingsIV.setOnClickListener(v -> showAlertDialogue(getString(R.string.password_alert_title), "", true));

        firebaseManager = new FirebaseManager(this);

        //Set on click listener to return from this activity
        returnIB.setOnClickListener(v -> finish());

        startActivityForResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if(data != null){
                            String activityType = data.getStringExtra("ACTIVITY_TYPE");
                            if (activityType != null && activityType.equals("SETTINGS_ACTIVITY")){

                            }
                        }

                    }
                }
        );
    }

    /**
     * onResume,
     * Get the display rotation helper to resume
     */
    @Override
    protected void onResume() {
        super.onResume();
        displayRotationHelper.onResume();
    }

    /**
     * onPause,
     * pause the display rotation helper.
     */
    @Override
    public void onPause() {
        super.onPause();
        displayRotationHelper.onPause();
        if (alertDialogue != null) {
            alertDialogue.dismiss();
        }
    }

    /**
     * showAlertDialogue,
     * Builds the alert dialogue and shows it to the user
     *
     * @param title string with the title of the alert
     * @param description string with the description of the alert
     * @param passwordEnter boolean value that if true we show the password alert to the user and if
     *                      not true we show the user a generic alert
     */
    private void showAlertDialogue(String title, String description, boolean passwordEnter) {
        View view;
        TextView alertTitleTV;

        //If passwordEnter is true the user need to use the password enter alert
        if(passwordEnter){
            view = LayoutInflater.from(MainLobbyActivity.this).inflate(R.layout.password_alert_dialogue, null, false);
            StringBuilder enteredCode = new StringBuilder();

            //Initialise and fine the UI Elements for the alert
            alertTitleTV = view.findViewById(R.id.passwordAlertTitleTV);
            EditText enterPasswordET = view.findViewById(R.id.passwordAlertET);

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

            //Add an on click listener for each key pad button, they will add a number to the string
            //and set that set text into the edittext. Cap the length at 4.
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

            //delete click listener, as long as the string isn't empty then delete the last character
            //in the string
            deleteB.setOnClickListener(v -> {
                if(enteredCode.length()>0){
                    enteredCode.deleteCharAt(enteredCode.length() - 1);
                    enterPasswordET.setText(enteredCode.toString());
                }
            });

            //enter b on click listener, first disable the button and then try check the passcode
            enterB.setOnClickListener(view1 -> {
                enterB.setEnabled(false);
                String passwordEntered = enterPasswordET.getText().toString();

                firebaseManager.checkPasscode(passwordEntered, new FirebaseManager.PasscodeCallback() {
                    @Override
                    public void onSuccess() {
                        alertDialogue.dismiss();
                        Intent intent = SettingsActivity.newIntent(MainLobbyActivity.this);
                        startActivityForResultLauncher.launch(intent);
                    }

                    @Override
                    public void onPasswordUploadFailure(String errorMessage) {
                        showAlertDialogue(getString(R.string.alert_error_title), getString(R.string.resolve_check_connection_description), false);
                    }

                    @Override
                    public void onPasswordsDiffer() {
                        alertDialogue.dismiss();
                        showAlertDialogue(getString(R.string.alert_error_title), getString(R.string.resolve_incorrect_password_description), false);
                    }
                });
            });

        }else{
            //Initialise the layouts and views
            view = LayoutInflater.from(MainLobbyActivity.this).inflate(R.layout.alert_dialogue, null, false);
            alertTitleTV = view.findViewById(R.id.alertTitleTV);
            Button okayB = view.findViewById(R.id.alertDoneB);
            TextView descriptionTV = view.findViewById(R.id.alertDescriptionTV);

            descriptionTV.setText(description);

            okayB.setOnClickListener(view2 -> alertDialogue.dismiss());
        }

        alertTitleTV.setText(title);

        //Initialise the builder and the alertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MainLobbyActivity.this);
        builder.setView(view);
        alertDialogue = builder.create();

        if (alertDialogue.getWindow() != null) {
            alertDialogue.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        //Show the actual alert
        alertDialogue.show();
    }
}