package com.google.ar.core.examples.java.cloudanchor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.examples.java.common.helpers.DisplayRotationHelper;

public class SelectActivity extends AppCompatActivity {

    private DisplayRotationHelper displayRotationHelper;
    private AlertDialog alertDialogue;
    private EditText nameET;
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

        Button placeB = findViewById(R.id.selectPlaceB);
        Button viewB = findViewById(R.id.selectBeginViewingB);
        nameET = findViewById(R.id.selectNameET);
        ImageButton backIB = findViewById(R.id.selectReturnIB);

        placeB.setOnClickListener(v -> {
            showAlertDialogue();
        });

        viewB.setOnClickListener(v -> {
            Intent intent = ResolveAnchorsLobbyActivity.newIntent(SelectActivity.this);
            startActivity(intent);
        });

        backIB.setOnClickListener(v -> {
            finish();
        });
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

    /**
     * showAlertDialogue, Shows the success of adding the alert dialogue
     */
    private void showAlertDialogue() {
        //Initialise the layouts and views
        View view = LayoutInflater.from(SelectActivity.this).inflate(R.layout.password_alert_dialogue, null, false);

        StringBuilder enteredCode = new StringBuilder();
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
            SharedPreferences sharedPreferences = getSharedPreferences("SHARED_PREFERENCES",
                    Context.MODE_PRIVATE);
            String passwordAnswer = sharedPreferences.getString(PASSWORD_CODE,"1234");
            String passwordEntered = enterPasswordET.getText().toString();

            if(passwordEntered.equals(passwordAnswer)){
                alertDialogue.dismiss();
                String name = nameET.getText().toString();

                if(name.trim().isEmpty()){
                    name = "DEFAULT";
                }

                Intent intent = CloudAnchorActivity.newIntent(SelectActivity.this);
                intent.putExtra("PLACING_ANCHOR", true);
                intent.putExtra("ANCHOR_NAME", name);
                startActivity(intent);
            }else{
                alertDialogue.dismiss();
            }
        });

        if (alertDialogue.getWindow() != null) {
            alertDialogue.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        //Show the actual alert
        alertDialogue.show();
    }
}