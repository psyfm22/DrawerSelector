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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

public class ResolveAnchorsLobbyActivity extends AppCompatActivity {

    private FirebaseManager firebaseManager;
    private Spinner spinner;
    private List<Hotspot> hotspotList;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter recyclerViewAdapter;

    static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, ResolveAnchorsLobbyActivity.class);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resolve_anchors_lobby);

        Button resolveB = findViewById(R.id.resolve_button);
        Button clearB = findViewById(R.id.clearAnchorsB);
        spinner = findViewById(R.id.select_anchors_spinner);
        ImageButton returnIB = findViewById(R.id.anchorLobbyReturnIB);
//        recyclerView = findViewById(R.id.recyclerView);

        clearB.setOnClickListener(view -> deleteAllAnchors());
        resolveB.setOnClickListener(view -> resolveAnchor());
        returnIB.setOnClickListener(view -> finish());



        firebaseManager = new FirebaseManager(this);

        firebaseManager.getHotspotList(new FirebaseManager.HotspotListListener() {
            @Override
            public void onHotspotListFetched(List<Hotspot> hotspots) {

                hotspotList = hotspots;

                List<String> nameList = new ArrayList<>();

                for (Hotspot hotspot : hotspotList) {
                    nameList.add(hotspot.getName());
                }

                SpinnerAdapter adapter = new SpinnerAdapter(ResolveAnchorsLobbyActivity.this, nameList);
                spinner.setAdapter(adapter);


//                ArrayAdapter<String> adapter = new ArrayAdapter<>(
//                        ResolveAnchorsLobbyActivity.this,
//                        android.R.layout.simple_spinner_item,
//                        nameList);
//
//                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                spinner.setAdapter(adapter);


//                recyclerView.setLayoutManager(new LinearLayoutManager(ResolveAnchorsLobbyActivity.this));
//                recyclerViewAdapter = new RecyclerViewAdapter(ResolveAnchorsLobbyActivity.this, hotspotList);
//                recyclerView.setAdapter(recyclerViewAdapter);
            }

            @Override
            public void onError(DatabaseError error) {
                Log.d("COMP3018", "Error fetching hotspot list", error.toException());
            }
        });

    }

    private void deleteAllAnchors(){
        firebaseManager.removeAllData();
        recreate();
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