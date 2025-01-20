package com.google.ar.core.examples.java.cloudanchor;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
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
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.List;


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
    private DrawSelectedViewModel drawSelectedViewModel;
    private Button viewAnchorB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resolve_lobby);

        //Assign the rotation helper
        displayRotationHelper = new DisplayRotationHelper(this);

        viewAnchorB = findViewById(R.id.selectBeginViewingB);
        viewAnchorB.setEnabled(false);
        ImageButton backIB = findViewById(R.id.selectReturnIB);
        ImageView settingsIV = findViewById(R.id.selectSettingsIV);
        spinner = findViewById(R.id.select_anchors_spinner);

        startActivityForResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();

                        if(data != null){
                            String activityType = data.getStringExtra("ACTIVITY_TYPE");
                            if(activityType != null && activityType.equals("CLOUD_ANCHOR_ACTIVITY")){
                                Log.d("COMP3018", "Scan QR Code successful");
                                ScanOptions scanOptions = new ScanOptions();
                                scanOptions.setPrompt("Scan the QR Code");
                                scanOptions.setBeepEnabled(true);
                                scanOptions.setOrientationLocked(true);
                                scanOptions.setCaptureActivity(CustomCaptureActivity.class);
                                launcher.launch(scanOptions);
                            }else{
                                viewAnchorB.setEnabled(false);
                                drawSelectedViewModel.deleteAllItems();
                                loadFromFirebase();
                            }
                        }

                    } else if (result.getResultCode() == RESULT_CANCELED) {
                        Log.d("COMP3018", "Scan QR Code was canceled");
                    }
                }
        );

        drawSelectedViewModel = new ViewModelProvider(ResolveLobbyActivity.this).get(DrawSelectedViewModel.class);

        drawSelectedViewModel.getDrawerList().observe(this, strings -> {
            SpinnerAdapterNoDelete adapter = new SpinnerAdapterNoDelete(ResolveLobbyActivity.this, strings);
            spinner.setAdapter(adapter);

            String selectedItem = drawSelectedViewModel.getCurrentSelection().getValue();
            if (selectedItem != null) {
                int position = strings.indexOf(selectedItem);
                if (position != -1) {
                    spinner.setSelection(position);
                }
            }
        });


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, android.view.View selectedItemView, int position, long id) {
                String selectedItem = (String) parentView.getItemAtPosition(position);
                Log.d("COMP3018","Here is Selected: " + selectedItem);
                drawSelectedViewModel.setCurrentSelection(selectedItem);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        viewAnchorB.setOnClickListener(v -> {
            int selectedPosition = spinner.getSelectedItemPosition();

            if(selectedPosition > -1){
                Hotspot selectedHotspot = hotspotList.get(selectedPosition);
                Log.d("COMP3018",selectedHotspot.getName());
                drawName = selectedHotspot.getName();

                Intent intent = CloudAnchorActivity.newIntent(ResolveLobbyActivity.this);
                intent.putExtra("PLACING_ANCHOR", false);
                intent.putExtra("HOTSPOT_CODE", selectedHotspot.getCode());
                startActivityForResultLauncher.launch(intent);
            }else{
                showAlertDialogue("Error","Error resolving Anchor, Please try again", false);
            }
        });

        settingsIV.setOnClickListener(v -> showAlertDialogue("Enter Password: ", "", true));

        backIB.setOnClickListener(v -> finish());

        loadFromFirebase();
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

    private void loadFromFirebase(){
        firebaseManager = new FirebaseManager(this);
        firebaseManager.getHotspotList(new FirebaseManager.HotspotListListener() {
            @Override
            public void onHotspotListFetched(List<Hotspot> hotspots) {
                hotspotList = hotspots;
                List<String> nameList = new ArrayList<>();

                for (Hotspot hotspot : hotspotList) {
                    nameList.add(hotspot.getName());
                }

                drawSelectedViewModel.setDrawList(nameList);
                viewAnchorB.setEnabled(true);
            }

            @Override
            public void onError(DatabaseError error) {
                showAlertDialogue("Error","Failed Loading File", false);
                Log.d("COMP3018", "Error fetching hotspot list", error.toException());
            }
        });
    }


    /**
     * showAlertDialogue, Shows the success of adding the alert dialogue
     */
    private void showAlertDialogue(String title, String description, boolean passwordEnter) {
        View view;
        TextView alertTitleTV;

        if(passwordEnter){
            view = LayoutInflater.from(ResolveLobbyActivity.this).inflate(R.layout.password_alert_dialogue, null, false);
            StringBuilder enteredCode = new StringBuilder();

            alertTitleTV = view.findViewById(R.id.passwordAlertTitleTV);

            //Initialise the layouts and views
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

            enterB.setOnClickListener(view1 -> {
                enterB.setEnabled(false);
                String passwordEntered = enterPasswordET.getText().toString();

                firebaseManager.checkPasscode(passwordEntered, new FirebaseManager.PasscodeCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d("COMP3018", "On Success");
                        alertDialogue.dismiss();
                        Intent intent = SettingsActivity.newIntent(ResolveLobbyActivity.this);
                        startActivity(intent);
                    }

                    @Override
                    public void onPasswordUploadFailure(String errorMessage) {
                        Log.d("COMP3018", "Upload Failed");
                        showAlertDialogue("Error Uploading", "Please Try Again", false);
                    }

                    @Override
                    public void onPasswordsDiffer() {
                        Log.d("COMP3018", "Passwords Differ");
                        alertDialogue.dismiss();
                        showAlertDialogue("Error", "Incorrect Password", false);
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

            okayB.setOnClickListener(view2 -> {
                alertDialogue.dismiss();
            });
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

    ActivityResultLauncher<ScanOptions> launcher = registerForActivityResult(new ScanContract(), result->{
        if(result.getContents() != null){
            AlertDialog.Builder builder = new AlertDialog.Builder(ResolveLobbyActivity.this);
            if(result.getContents().equals(drawName)){
                Log.d("COMP3018","Correct Name");
                builder.setTitle("Correct QR Code");
                builder.setMessage("Press Okay to return");
                builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }else{
                builder.setTitle("Incorrect QR Code");
                builder.setMessage("Please Try Locating the Draw Again");
                builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        }
    });
}