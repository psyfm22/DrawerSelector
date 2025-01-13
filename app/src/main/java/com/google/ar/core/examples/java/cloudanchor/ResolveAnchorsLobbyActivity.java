package com.google.ar.core.examples.java.cloudanchor;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.examples.java.common.helpers.DisplayRotationHelper;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

public class ResolveAnchorsLobbyActivity extends AppCompatActivity{

    private Spinner spinner;
    private List<Hotspot> hotspotList;

    private DisplayRotationHelper displayRotationHelper;
    static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, ResolveAnchorsLobbyActivity.class);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resolve_anchors_lobby);

        displayRotationHelper = new DisplayRotationHelper(this);

        Button resolveB = findViewById(R.id.resolve_button);
        spinner = findViewById(R.id.settings_anchors_spinner);
        ImageButton returnIB = findViewById(R.id.anchorLobbyReturnIB);

        resolveB.setOnClickListener(view -> resolveAnchor());
        returnIB.setOnClickListener(view -> finish());

        FirebaseManager firebaseManager = new FirebaseManager(this);

        firebaseManager.getHotspotList(new FirebaseManager.HotspotListListener() {
            @Override
            public void onHotspotListFetched(List<Hotspot> hotspots) {
                hotspotList = hotspots;
                List<String> nameList = new ArrayList<>();

                for (Hotspot hotspot : hotspotList) {
                    nameList.add(hotspot.getName());
                }

                SpinnerAdapter adapter = new SpinnerAdapter1(ResolveAnchorsLobbyActivity.this, nameList);
                spinner.setAdapter(adapter);
            }

            @Override
            public void onError(DatabaseError error) {
                Log.d("COMP3018", "Error fetching hotspot list", error.toException());
            }
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

    private void resolveAnchor(){
        int selectedPosition = spinner.getSelectedItemPosition();

        if(selectedPosition > -1){
            Hotspot selectedHotspot = hotspotList.get(selectedPosition);

            Log.d("COMP3018",selectedHotspot.getName());

            Intent intent = CloudAnchorActivity.newIntent(ResolveAnchorsLobbyActivity.this);
            intent.putExtra("PLACING_ANCHOR", false);
            intent.putExtra("HOTSPOT_CODE", selectedHotspot.getCode());
            startActivity(intent);
        }else{
            showAlertDialogue();
        }
    }

    /**
     * showAlertDialogue, Shows the failure of opening resolve
     */
    private void showAlertDialogue() {
        //Initialise the layouts and views
        View view = LayoutInflater.from(ResolveAnchorsLobbyActivity.this).inflate(R.layout.alert_dialogue, null, false);
        AlertDialog alertDialogue;
        Button okayB = view.findViewById(R.id.alertDoneB);

        //Initialise the builder and the alertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(ResolveAnchorsLobbyActivity.this);
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