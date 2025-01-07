package com.google.ar.core.examples.java.cloudanchor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

                Log.d("COMP3018","Password Successfully Changed");
            }else{
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
}