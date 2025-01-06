package com.google.ar.core.examples.java.cloudanchor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseError;

import java.util.List;

public class ResolveAnchorsLobbyActivity extends AppCompatActivity {

    private FirebaseManager firebaseManager;
    private Spinner spinner;

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

        clearB.setOnClickListener(view -> {
            deleteAllAnchors();
        });
        resolveB.setOnClickListener(view ->{
            resolveAnchor();
        });


        firebaseManager = new FirebaseManager(this);

        firebaseManager.getHotspotList(new FirebaseManager.HotspotListListener() {
            @Override
            public void onHotspotListFetched(List<String> displayNames) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        ResolveAnchorsLobbyActivity.this,
                        android.R.layout.simple_spinner_item,
                        displayNames);

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
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
        String selectedItem = (String) spinner.getSelectedItem();

        Log.d("COMP3018",selectedItem);

        Intent intent = CloudAnchorActivity.newIntent(ResolveAnchorsLobbyActivity.this);
        intent.putExtra("PLACING_ANCHOR", false);
        startActivity(intent);

    }
}