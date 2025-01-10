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

import com.google.ar.core.examples.java.common.helpers.DisplayRotationHelper;

public class SettingsActivity extends AppCompatActivity {

    private DisplayRotationHelper displayRotationHelper;
    private TextView changePinTV, addTrayTV;
    private Button changePinB, addTrayB;
    private EditText enterPin1ET, enterPin2ET, enterNameET;

    private final StringBuilder finalPassword = new StringBuilder();
    private SharedPreferences.Editor editor;
    private static final String PASSWORD_CODE = "PASSWORD";


    static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, SettingsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ImageButton returnB = findViewById(R.id.settingsReturnIB);
        changePinTV = findViewById(R.id.settingsChangePinTV);
        addTrayTV = findViewById(R.id.settingsAddDrawTV);
        changePinB = findViewById(R.id.settingsChangePinB);
        addTrayB = findViewById(R.id.settingsAddDrawB);

        enterPin1ET = findViewById(R.id.settingsEnterPin1ET);
        enterPin2ET = findViewById(R.id.settingsEnterPin2ET);

        enterNameET = findViewById(R.id.settingsEnterNameET);

        SharedPreferences sharedPreferences = getSharedPreferences("SHARED_PREFERENCES",
                Context.MODE_PRIVATE);

        editor = sharedPreferences.edit();

        returnB.setOnClickListener(v -> {
            returnButtonPressed();
        });

        changePinB.setOnClickListener(v -> {
            changeAccessPin();
        });

        addTrayB.setOnClickListener(v -> {
            addNewTray();
        });

        displayRotationHelper = new DisplayRotationHelper(this);
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

    private void changeAccessPin(){

        if (addTrayTV.getVisibility() == View.VISIBLE){

            addTrayTV.setVisibility(View.GONE);
            addTrayB.setVisibility(View.GONE);

            enterPin1ET.setVisibility(View.VISIBLE);
            enterPin2ET.setVisibility(View.VISIBLE);
        }else{
            String password1 = enterPin1ET.getText().toString();
            String password2 = enterPin2ET.getText().toString();

            if(password1.equals(password2) && password1.length() == 4) {
                finalPassword.append(password1);

                editor.putString(PASSWORD_CODE, finalPassword.toString());
                editor.apply();
                finalPassword.setLength(0);

                showAlertDialogue("Password Changed", "Successfully Changed the Password", true);
                Log.d("COMP3018","Password Successfully Changed");
            }else{
                showAlertDialogue("Password Issue", "Please Try Again", false);
                Log.d("COMP3018","Password Change Failed");
            }

            enterPin1ET.setText("");
            enterPin2ET.setText("");
        }
    }

    private void addNewTray(){
        if(changePinB.getVisibility() == View.VISIBLE){
            changePinTV.setVisibility(View.GONE);
            changePinB.setVisibility(View.GONE);

            addTrayTV.setText(getString(R.string.host_instructions_text));
            enterNameET.setVisibility(View.VISIBLE);
        }else{
            String name = enterNameET.getText().toString();

            if(name.trim().isEmpty()){
                showAlertDialogue("Empty Name","Please Enter a Name for the Anchor", false);
                return;
            }

            Intent intent = CloudAnchorActivity.newIntent(SettingsActivity.this);
            intent.putExtra("PLACING_ANCHOR", true);
            intent.putExtra("ANCHOR_NAME", name);
            startActivity(intent);
        }
    }

    private void returnButtonPressed(){
        if(enterPin1ET.getVisibility() == View.VISIBLE){
            addTrayTV.setVisibility(View.VISIBLE);
            addTrayB.setVisibility(View.VISIBLE);

            enterPin1ET.setVisibility(View.GONE);
            enterPin2ET.setVisibility(View.GONE);
        }else if(enterNameET.getVisibility() == View.VISIBLE){
            enterNameET.setVisibility(View.GONE);

            changePinTV.setVisibility(View.VISIBLE);
            changePinB.setVisibility(View.VISIBLE);

            addTrayTV.setText("Add a new Drawer");
        }else{
            finish();
        }
    }

    /**
     * showAlertDialogue, Shows the failure of opening resolve
     */
    private void showAlertDialogue(String title, String description, boolean successful) {
        //Initialise the layouts and views
        View view = LayoutInflater.from(SettingsActivity.this).inflate(R.layout.alert_dialogue, null, false);
        AlertDialog alertDialogue;
        Button okayB = view.findViewById(R.id.alertDoneB);

        TextView titleTV = view.findViewById(R.id.alertTitleTV);
        TextView descriptionTV = view.findViewById(R.id.alertDescriptionTV);

        titleTV.setText(title);
        descriptionTV.setText(description);

        if(successful){
            ImageView logoIV = view.findViewById(R.id.alertLogoIV);
            logoIV.setImageResource(R.drawable.success);
        }

        //Initialise the builder and the alertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setView(view);
        alertDialogue = builder.create();

        okayB.setOnClickListener(view1 -> alertDialogue.dismiss());

        if (alertDialogue.getWindow() != null) {
            alertDialogue.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        //Show the actual alert
        alertDialogue.show();
    }

}