package com.google.ar.core.examples.java.cloudanchor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ResolveAnchorsLobbyActivity extends AppCompatActivity {

    private FirebaseManager firebaseManager;

    static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, ResolveAnchorsLobbyActivity.class);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resolve_anchors_lobby);

        Button clearB = findViewById(R.id.clearAnchorsB);

        clearB.setOnClickListener(view -> {
            deleteAllAnchors();
        });
        firebaseManager = new FirebaseManager(this);

    }

    private void deleteAllAnchors(){
        firebaseManager.removeAllData();
    }
}