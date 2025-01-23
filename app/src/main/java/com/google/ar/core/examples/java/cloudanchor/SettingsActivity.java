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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.view.inputmethod.InputMethodManager;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.ar.core.examples.java.common.helpers.DisplayRotationHelper;
import com.google.firebase.database.DatabaseError;

import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements SpinnerAdapterWithDelete.ItemDeletedListener{

    private DisplayRotationHelper displayRotationHelper;
    private TextView changePinTV, addTrayTV, manageDrawerTV;
    private Button changePinB, addTrayB, manageDrawerB, clearAllB;
    private EditText enterPin1ET, enterPin2ET, enterNameET;
    private FirebaseManager firebaseManager;
    private Spinner spinner;
    private List<Hotspot> hotspotList;
    private ActivityStageViewModel activityStageViewModel;
    private final StringBuilder finalPassword = new StringBuilder();
    private static final String SPINNER_POSITION_KEY = "SPINNER_POSITION_KEY";

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

        manageDrawerTV = findViewById(R.id.settingsManageDrawersTV);
        manageDrawerB = findViewById(R.id.settingsManageDrawersB);

        enterPin1ET = findViewById(R.id.settingsEnterPin1ET);
        enterPin2ET = findViewById(R.id.settingsEnterPin2ET);

        enterNameET = findViewById(R.id.settingsEnterNameET);

        clearAllB = findViewById(R.id.settingsClearAllB);
        spinner = findViewById(R.id.settings_anchors_spinner);

        activityStageViewModel = new ViewModelProvider(this).get(ActivityStageViewModel.class);
        activityStageViewModel.getActivityStage().observe(this, this::setCurrentActivity);

        firebaseManager = new FirebaseManager(this);
        firebaseManager.getHotspotList(new FirebaseManager.HotspotListListener() {
            @Override
            public void onHotspotListFetched(List<Hotspot> hotspots) {
                hotspotList = hotspots;
                List<String> nameList = new ArrayList<>();

                for (Hotspot hotspot : hotspotList) {
                    nameList.add(hotspot.name());
                }

                SpinnerAdapterWithDelete adapter = new SpinnerAdapterWithDelete(SettingsActivity.this, nameList, SettingsActivity.this);
                spinner.setAdapter(adapter);

                if (savedInstanceState != null) {
                    int position;
                    if(nameList.isEmpty()){
                        position = savedInstanceState.getInt(SPINNER_POSITION_KEY, -1);
                    }else{
                        position = savedInstanceState.getInt(SPINNER_POSITION_KEY, 0);
                    }
                    spinner.setSelection(position);
                }
            }
            @Override
            public void onError(DatabaseError error) {
                showAlertDialogue(getString(R.string.alert_error_title), getString(R.string.settings_failure_fetching_description), false);
                Log.d("COMP3018", "Error fetching hotspot list", error.toException());
            }
        });

        returnB.setOnClickListener(v -> {
            closeKeyboard(v);
            returnButtonPressed();
        });

        changePinB.setOnClickListener(v -> changeAccessPin());

        addTrayB.setOnClickListener(v -> addNewTray());

        manageDrawerB.setOnClickListener(v -> activityStageViewModel.setActivityStage(ActivityStage.MANAGE_DRAWERS));

        clearAllB.setOnClickListener(v -> {
            clearAllB.setEnabled(false);
            spinner.setEnabled(false);

            firebaseManager.removeAllData(new FirebaseManager.DeleteAllCallback() {
                @Override
                public void onSuccess() {
                    clearAllB.setEnabled(true);
                    spinner.setEnabled(true);
//                    showAlertDialogue(getString(R.string.success), getString(R.string.settings_all_deleted_description), true);
                    recreate();
                }

                @Override
                public void onFailure(String errorMessage) {
                    showAlertDialogue(getString(R.string.alert_error_title), getString(R.string.settings_failure_deleting_all_description), false);
                    clearAllB.setEnabled(true);
                    spinner.setEnabled(true);
                    recreate();

                }
            });
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
            activityStageViewModel.setActivityStage(ActivityStage.CHANGE_PIN);
        }else{
            String password1 = enterPin1ET.getText().toString();
            String password2 = enterPin2ET.getText().toString();

            if(password1.equals(password2) && password1.length() == 4) {
                finalPassword.append(password1);

                String hashedPassword = BCrypt.hashpw(password1,BCrypt.gensalt());

                firebaseManager.changePasscode(hashedPassword, new FirebaseManager.ChangePasscodeCallback() {
                    @Override
                    public void onSuccess() {
                        showAlertDialogue(getString(R.string.settings_changed_password_title), getString(R.string.settings_changed_password_description), true);

                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        showAlertDialogue(getString(R.string.settings_password_issue_title), getString(R.string.settings_password_issue_description), false);

                    }
                });
            }else{
                showAlertDialogue(getString(R.string.settings_password_issue_title), getString(R.string.settings_password_issue_description), false);
            }
            finalPassword.setLength(0);
            enterPin1ET.setText("");
            enterPin2ET.setText("");
        }
    }

    private void addNewTray(){
        if(changePinB.getVisibility() == View.VISIBLE){
            activityStageViewModel.setActivityStage(ActivityStage.ADD_DRAWER);
        }else{
            String name = enterNameET.getText().toString();

            if(name.trim().isEmpty()){
                showAlertDialogue(getString(R.string.settings_empty_name_title),getString(R.string.settings_empty_name_description), false);
                return;
            }

            Intent intent = CloudAnchorActivity.newIntent(SettingsActivity.this);
            intent.putExtra("PLACING_ANCHOR", true);
            intent.putExtra("ANCHOR_NAME", name);
            startActivity(intent);
        }
    }

    private void returnButtonPressed(){
        if(enterPin1ET.getVisibility() == View.VISIBLE ||
                enterNameET.getVisibility() == View.VISIBLE ||
                spinner.getVisibility() == View.VISIBLE){
            activityStageViewModel.setActivityStage(ActivityStage.MENU);
        }else{
            Intent resultIntent = new Intent();
            resultIntent.putExtra("ACTIVITY_TYPE", "SETTINGS_ACTIVITY");
            setResult(RESULT_OK, resultIntent);
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

    @Override
    public void onItemDeleted(int position) {
        Hotspot hotspot = hotspotList.get(position);

        firebaseManager.removeHotspot(hotspot.code(), new FirebaseManager.DeleteCallback() {
            @Override
            public void onSuccess() {
                hotspotList.remove(position);
            }

            @Override
            public void onFailure(String errorMessage) {
                showAlertDialogue(getString(R.string.alert_error_title), getString(R.string.settings_failure_deleting_description), false);
                Log.d("COMP3018", errorMessage);
            }
        });
    }


    private void setCurrentActivity(ActivityStage activityStage){
        if(activityStage.equals(ActivityStage.MENU)){
            enterPin1ET.setText("");
            enterPin2ET.setText("");
            enterNameET.setText("");

            enterPin1ET.setVisibility(View.GONE);
            enterPin2ET.setVisibility(View.GONE);
            enterNameET.setVisibility(View.GONE);
            spinner.setVisibility(View.GONE);
            clearAllB.setVisibility(View.GONE);

            changePinTV.setVisibility(View.VISIBLE);
            changePinB.setVisibility(View.VISIBLE);
            addTrayTV.setVisibility(View.VISIBLE);
            addTrayB.setVisibility(View.VISIBLE);
            manageDrawerTV.setVisibility(View.VISIBLE);
            manageDrawerB.setVisibility(View.VISIBLE);

            addTrayTV.setText(R.string.settings_add_drawer_description);
        } else if (activityStage.equals(ActivityStage.CHANGE_PIN)) {
            addTrayTV.setVisibility(View.GONE);
            addTrayB.setVisibility(View.GONE);
            manageDrawerTV.setVisibility(View.GONE);
            manageDrawerB.setVisibility(View.GONE);

            enterPin1ET.setVisibility(View.VISIBLE);
            enterPin2ET.setVisibility(View.VISIBLE);
        } else if (activityStage.equals(ActivityStage.ADD_DRAWER)) {
            addTrayTV.setText(getString(R.string.settings_host_instruction_description));
            changePinTV.setVisibility(View.GONE);
            changePinB.setVisibility(View.GONE);
            manageDrawerTV.setVisibility(View.GONE);
            manageDrawerB.setVisibility(View.GONE);

            enterNameET.setVisibility(View.VISIBLE);
        }else{
            changePinTV.setVisibility(View.GONE);
            changePinB.setVisibility(View.GONE);
            addTrayTV.setVisibility(View.GONE);
            addTrayB.setVisibility(View.GONE);
            manageDrawerTV.setVisibility(View.GONE);
            manageDrawerB.setVisibility(View.GONE);

            spinner.setVisibility(View.VISIBLE);
            clearAllB.setVisibility(View.VISIBLE);
        }
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        int selectedPosition = spinner.getSelectedItemPosition();
        outState.putInt(SPINNER_POSITION_KEY, selectedPosition);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        int position = savedInstanceState.getInt(SPINNER_POSITION_KEY, 0);
        spinner.setSelection(position);
    }

    /**
     * closeKeyboard, closes the keyboard
     *
     * @param view currentView
     */
    private void closeKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}