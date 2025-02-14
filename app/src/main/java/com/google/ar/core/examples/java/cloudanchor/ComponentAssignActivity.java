package com.google.ar.core.examples.java.cloudanchor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.examples.java.cloudanchor.nexusapi.NexarClient;

import java.util.Locale;

public class ComponentAssignActivity extends AppCompatActivity {

    private NexarClient nexarClient;
    private EditText loadedComponentET;
    private Button assignDrawerB;
    private static final int MAX_HOLDER = 30;
    private FirebaseManager firebaseManager;

    static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, ComponentAssignActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_component_assign);


        Button loadComponentQueryB = findViewById(R.id.assignLoadComponentB);
        EditText inputComponentET = findViewById(R.id.assignEnterComponentET);
        loadedComponentET = findViewById(R.id.assignLoadedComponentET);
        assignDrawerB = findViewById(R.id.assignDrawerB);
        Button closeB = findViewById(R.id.assignCloseB);

        closeB.setOnClickListener(v ->{
            finish();
        });

        nexarClient = getNexarClient(loadComponentQueryB);

        firebaseManager = new FirebaseManager(this);

        loadComponentQueryB.setOnClickListener(v -> {
            loadComponentQueryB.setEnabled(false);
            String component = inputComponentET.getText().toString();
            queryNexarClient(component, loadComponentQueryB);
        });

        assignDrawerB.setOnClickListener(v -> {
            assignDrawerB.setEnabled(false);
            Log.d("COMP3018", "Button has been called");

            String inputComponent = loadedComponentET.getText().toString().trim();

            if(inputComponent.isEmpty()){
                assignDrawerB.setEnabled(true);
                return;
            }

            inputComponent = inputComponent.toLowerCase(Locale.ENGLISH);
            String capitalisedInput = toUppercase(inputComponent);


            firebaseManager.inputComponent(0, 30, capitalisedInput);
        });

    }


    @NonNull
    private NexarClient getNexarClient(Button button) {
        NexarClient nexarClient = new NexarClient();
        try {
            nexarClient.getAccessToken(new NexarClient.AccessCallback() {
                @Override
                public void AccessFound(String response) {
                    Log.d("COMP3018", response);
                    button.setEnabled(true);
                }

                @Override
                public void AccessFailed(String errorMessage) {
                    Toast.makeText(ComponentAssignActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return nexarClient;
    }

    private void queryNexarClient(String component, Button loadComponentQueryB){
        nexarClient.Query(component, new NexarClient.QueryCallback() {
            @Override
            public void QueryFound(String categoryName) {
                loadedComponentET.setText(categoryName);
                Toast.makeText(ComponentAssignActivity.this, categoryName, Toast.LENGTH_LONG).show();
                loadComponentQueryB.setEnabled(true);
                assignDrawerB.setEnabled(true);
            }

            @Override
            public void QueryFailed(String errorMessage) {
                Toast.makeText(ComponentAssignActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                loadComponentQueryB.setEnabled(true);
                assignDrawerB.setEnabled(true);
            }
        });
    }

    private String toUppercase(String input){
        //This is the array of words
        String[] words = input.split("\\s+");
        StringBuilder capitalisedName = new StringBuilder();

        for(int i=0;i<words.length;i++){
            Log.d("COMP3018", "I: "+ i+" word: "+ words[i]);

            if (i > 0) {
                capitalisedName.append(" ");
            }


            String cap = words[i].substring(0, 1).toUpperCase() + words[i].substring(1);
            capitalisedName.append(cap);
        }
        return capitalisedName.toString();
    }
}