package com.google.ar.core.examples.java.cloudanchor;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
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
        viewAnchorB = findViewById(R.id.resolveLobbyViewB);
        ImageButton backIB = findViewById(R.id.resolveLobbyReturnIB);
        spinner = findViewById(R.id.resolveLobbyAnchorsS);

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

                    }
                }
        );

        //Set the listener when viewing and anchor button
        viewAnchorB.setOnClickListener(v -> {
            //Get the current selected position
            int selectedPosition = spinner.getSelectedItemPosition();

            //Only Run this code if there is a valid selection from the spinner
            if (selectedPosition > -1) {
                Hotspot selectedHotspot = hotspotList.get(selectedPosition);
                drawName = selectedHotspot.getName();

                //We launch the cloud anchor activity, setting it in resolve mode, passing the anchor
                //name and the hotspot code
                Intent intent = CloudAnchorActivity.newIntent(ResolveLobbyActivity.this);
                intent.putExtra("PLACING_ANCHOR", false);
                intent.putExtra("ANCHOR_NAME", drawName);
                intent.putExtra("HOTSPOT_CODE", selectedHotspot.getCode());
                startActivityForResultLauncher.launch(intent);
            } else {
                showAlertDialogue(getString(R.string.alert_error_title), getString(R.string.resolve_fail_anchor_description));
            }
        });


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
    }

    /**
     * loadFromFirebase,
     * Load the list of hotspots from the firebase storage and places this list in the spinner
     */
    private void loadFromFirebase() {

        //Initialise the firebase manage and call the get hotspot list method passing it this listener
        FirebaseManager firebaseManager = new FirebaseManager(this);
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
                    if (nameList.size() > integer) {
                        spinner.setSelection(integer);
                    } else {
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
                showAlertDialogue(getString(R.string.alert_error_title), getString(R.string.resolve_failed_loading_description));
            }
        });
    }

    /**
     * showAlertDialogue,
     * Builds the alert dialogue and shows it to the user
     *
     * @param title         string with the title of the alert
     * @param description   string with the description of the alert
     */
    private void showAlertDialogue(String title, String description) {
        View view;
        TextView alertTitleTV;

        //If passwordEnter is true the user need to use the password enter alert

        //Initialise the layouts and views
        view = LayoutInflater.from(ResolveLobbyActivity.this).inflate(R.layout.alert_dialogue, null, false);
        alertTitleTV = view.findViewById(R.id.alertTitleTV);
        Button okayB = view.findViewById(R.id.alertDoneB);
        TextView descriptionTV = view.findViewById(R.id.alertDescriptionTV);

        descriptionTV.setText(description);

        okayB.setOnClickListener(view2 -> alertDialogue.dismiss());


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