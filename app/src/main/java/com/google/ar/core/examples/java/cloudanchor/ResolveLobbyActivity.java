package com.google.ar.core.examples.java.cloudanchor;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.ar.core.examples.java.common.helpers.DisplayRotationHelper;
import com.google.firebase.database.DatabaseError;
import java.util.ArrayList;
import java.util.List;

/**
 * ResolveLobbyActivity,
 * Lobby where users can select which anchor they would like to resolve. They can also navigate to
 * the settings page from here
 */
public class ResolveLobbyActivity extends AppCompatActivity {
    private Spinner spinner;
    private List<Hotspot> hotspotList;
    private FirebaseManager firebaseManager;
    private DisplayRotationHelper displayRotationHelper;
    private AlertDialog alertDialogue;
    private ActivityResultLauncher<Intent> startActivityForResultLauncher;
    private String drawName = "";
    static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, ResolveLobbyActivity.class);
    }
    private DrawPositionViewModel drawPositionViewModel;
    private Button viewAnchorB;

    /**
     * onCreate,
     * Initialise the UI elements and setup the activity from the start
     *
     * @param savedInstanceState N/A
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resolve_lobby);

        //Assign the rotation helper
        displayRotationHelper = new DisplayRotationHelper(this);

        //Initialise the UI Elements
        viewAnchorB = findViewById(R.id.lobbyViewB);
        ImageButton backIB = findViewById(R.id.lobbyReturnIB);
        ImageView settingsIV = findViewById(R.id.lobbySettingsIV);
        spinner = findViewById(R.id.lobbyAnchorsS);

        //Disable the button
        viewAnchorB.setEnabled(false);

        //Initialise the draw position view model to keep track of the user selection
        drawPositionViewModel = new ViewModelProvider(this).get(DrawPositionViewModel.class);

        //We want to get data returned from the activity as it will allow us to reset the spinner if
        //there have been deletions in the firebase storage
        startActivityForResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if(data != null){
                            String activityType = data.getStringExtra("ACTIVITY_TYPE");
                            if (activityType != null && activityType.equals("SETTINGS_ACTIVITY")){
                                viewAnchorB.setEnabled(false);
                                loadFromFirebase();
                            }
                        }

                    }
                }
        );

        //Set the listener when viewing and anchor button
        viewAnchorB.setOnClickListener(v -> {
            //Get the current selected position
            int selectedPosition = spinner.getSelectedItemPosition();

            //Only Run this code if there is a valid selection from the spinner
            if(selectedPosition > -1){
                Hotspot selectedHotspot = hotspotList.get(selectedPosition);
                drawName = selectedHotspot.getName();

                //We launch the cloud anchor activity, setting it in resolve mode, passing the anchor
                //name and the hotspot code
                Intent intent = CloudAnchorActivity.newIntent(ResolveLobbyActivity.this);
                intent.putExtra("PLACING_ANCHOR", false);
                intent.putExtra("ANCHOR_NAME", drawName);
                intent.putExtra("HOTSPOT_CODE", selectedHotspot.getCode());
                startActivityForResultLauncher.launch(intent);
            }else{
                showAlertDialogue(getString(R.string.alert_error_title),getString(R.string.resolve_fail_anchor_description), false);
            }
        });

        //When the settings button is clicked we need to show the alert dialogue with the password
        //for the user to enter
        settingsIV.setOnClickListener(v -> showAlertDialogue(getString(R.string.password_alert_title), "", true));

        //Set on click listener to return from this activity
        backIB.setOnClickListener(v -> finish());

        //Call this method to load all the values into the spinner
        loadFromFirebase();
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
        if(alertDialogue != null){
            alertDialogue.dismiss();
        }
    }

    /**
     * loadFromFirebase,
     * Load the list of hotspots from the firebase storage and places this list in the spinner
     */
    private void loadFromFirebase(){

        //Initialise the firebase manage and call the get hotspot list method passing it this listener
        firebaseManager = new FirebaseManager(this);
        firebaseManager.getHotspotList(new FirebaseManager.HotspotListListener() {
            /**
             * onHotspotListFetched,
             * When the hotspot list is successfully fetched from the firebase database then we need
             * to load them into the correct values
             *
             * @param hotspots list of hotspot containing the anchors details
             */
            @Override
            public void onHotspotListFetched(List<Hotspot> hotspots) {
                //Load the hotspots into list
                hotspotList = hotspots;

                //Get the names of all the hotspots
                List<String> nameList = new ArrayList<>();
                for (Hotspot hotspot : hotspotList) {
                    nameList.add(hotspot.getName());
                }

                //Initialise the custom spinner adapter passing this activity as the context and
                //Passing it the name list, set this adapter for the spinner
                SpinnerAdapter adapter = new SpinnerAdapter(ResolveLobbyActivity.this, nameList);
                spinner.setAdapter(adapter);


                //observe draw position view model and when it changes set the spinner to that
                //integer value
                drawPositionViewModel.getPosition().observe(ResolveLobbyActivity.this, integer -> {
                    if(nameList.size()>integer){
                        spinner.setSelection(integer);
                    }else{
                        spinner.setSelection(0);
                    }
                });

                //When a spinner item is selected we need to make sure we change the draw position
                //view model
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        drawPositionViewModel.setPosition(i);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
                //As it has now all loaded we can enable the button
                viewAnchorB.setEnabled(true);
            }

            /**
             * onError,
             * If there is an an error with loading the firebase details then alert the user
             *
             * @param error the error that occurred
             */
            @Override
            public void onError(DatabaseError error) {
                showAlertDialogue(getString(R.string.alert_error_title),getString(R.string.resolve_failed_loading_description), false);
            }
        });
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
            view = LayoutInflater.from(ResolveLobbyActivity.this).inflate(R.layout.password_alert_dialogue, null, false);
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
                        Intent intent = SettingsActivity.newIntent(ResolveLobbyActivity.this);
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
            view = LayoutInflater.from(ResolveLobbyActivity.this).inflate(R.layout.alert_dialogue, null, false);
            alertTitleTV = view.findViewById(R.id.alertTitleTV);
            Button okayB = view.findViewById(R.id.alertDoneB);
            TextView descriptionTV = view.findViewById(R.id.alertDescriptionTV);

            descriptionTV.setText(description);

            okayB.setOnClickListener(view2 -> alertDialogue.dismiss());
        }

        alertTitleTV.setText(title);

        //Initialise the builder and the alertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(ResolveLobbyActivity.this);
        builder.setView(view);
        alertDialogue = builder.create();

        if (alertDialogue.getWindow() != null) {
            alertDialogue.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        //Show the actual alert
        alertDialogue.show();
    }
}