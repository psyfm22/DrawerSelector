package com.google.ar.core.examples.java.cloudanchor;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.examples.java.cloudanchor.nexusapi.NexarClient;

import java.util.Locale;

public class ComponentAssignActivity extends AppCompatActivity {

    private NexarClient nexarClient;
    private EditText loadedComponentET;
    private Button assignDrawerB;
    private FirebaseManager firebaseManager;
    private ActivityResultLauncher<Intent> startActivityForResultLauncher;


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
        ImageButton closeB = findViewById(R.id.assignReturnIB);

        closeB.setOnClickListener(v -> finish());

        nexarClient = getNexarClient(loadComponentQueryB);

        firebaseManager = new FirebaseManager(this);

        loadComponentQueryB.setOnClickListener(v -> {
            loadComponentQueryB.setEnabled(false);
            String component = inputComponentET.getText().toString();
            queryNexarClient(component, loadComponentQueryB);
        });

        //We want to get data returned from the activity as it will allow us to reset the spinner if
        //there have been deletions in the firebase storage
        startActivityForResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadedComponentET.setText("");
                        inputComponentET.setText("");
                        assignDrawerB.setEnabled(true);
                    }
                }
        );


        assignDrawerB.setOnClickListener(v -> {
            assignDrawerB.setEnabled(false);

            String inputComponent = loadedComponentET.getText().toString().trim();

            if (inputComponent.isEmpty()) {
                assignDrawerB.setEnabled(true);
                return;
            }

            inputComponent = inputComponent.toLowerCase(Locale.ENGLISH);
            String capitalisedInput = toUppercase(inputComponent);


            String finalCategory = inputComponent;
            firebaseManager.inputComponent(30, capitalisedInput, new FirebaseManager.NewComponentCallback() {
                @Override
                public void onSuccess(long key) {
                    //We launch the cloud anchor activity, setting it in resolve mode, passing the anchor
                    //name and the hotspot code
                    Intent intent = CloudAnchorActivity.newIntent(ComponentAssignActivity.this);
                    intent.putExtra("PLACING_ANCHOR", false);
                    intent.putExtra("ANCHOR_NAME", finalCategory);
                    intent.putExtra("HOTSPOT_CODE", key);
                    startActivityForResultLauncher.launch(intent);
                }

                @Override
                public void onNoDrawers() {
                    showAlertDialogue("No Available Drawers");
                    assignDrawerB.setEnabled(true);
                }

                @Override
                public void onFailure(String errorMessage) {
                    showAlertDialogue(errorMessage);
                    assignDrawerB.setEnabled(true);
                }
            });
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

    private void queryNexarClient(String component, Button loadComponentQueryB) {
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

    private String toUppercase(String input) {
        //This is the array of words
        String[] words = input.split("\\s+");
        StringBuilder capitalisedName = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                capitalisedName.append(" ");
            }
            String cap = words[i].substring(0, 1).toUpperCase() + words[i].substring(1);
            capitalisedName.append(cap);
        }
        return capitalisedName.toString();
    }

    /**
     * showAlertDialogue,
     * Builds the alert dialogue and shows it to the user
     *
     * @param description string with the description of the alert
     */
    private void showAlertDialogue(String description) {
        View view = LayoutInflater.from(ComponentAssignActivity
                .this).inflate(R.layout.alert_dialogue, null, false);

        TextView alertTitleTV = view.findViewById(R.id.alertTitleTV);
        Button okayB = view.findViewById(R.id.alertDoneB);
        TextView descriptionTV = view.findViewById(R.id.alertDescriptionTV);

        descriptionTV.setText(description);

        alertTitleTV.setText("Error");

        //Initialise the builder and the alertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(ComponentAssignActivity.this);
        builder.setView(view);
        AlertDialog alertDialogue = builder.create();

        okayB.setOnClickListener(view2 -> alertDialogue.dismiss());

        if (alertDialogue.getWindow() != null) {
            alertDialogue.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        //Show the actual alert
        alertDialogue.show();

    }
}