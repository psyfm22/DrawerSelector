package com.google.ar.core.examples.java.cloudanchor;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.examples.java.common.helpers.DisplayRotationHelper;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

public class ResolveAnchorsLobbyActivity extends AppCompatActivity implements SpinnerAdapter.ItemDeletedListener{

    private FirebaseManager firebaseManager;
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
        Button clearB = findViewById(R.id.clearAnchorsB);
        spinner = findViewById(R.id.select_anchors_spinner);
        ImageButton returnIB = findViewById(R.id.anchorLobbyReturnIB);

        clearB.setOnClickListener(view -> deleteAllAnchors());
        resolveB.setOnClickListener(view -> resolveAnchor());
        returnIB.setOnClickListener(view -> finish());

        Log.d("COMP3018", "I am in the oncreate()");

        firebaseManager = new FirebaseManager(this);

        firebaseManager.getHotspotList(new FirebaseManager.HotspotListListener() {
            @Override
            public void onHotspotListFetched(List<Hotspot> hotspots) {
                hotspotList = hotspots;
                Log.d("COMP3018","Original size of list: "+ hotspots.size());
                List<String> nameList = new ArrayList<>();

                for (Hotspot hotspot : hotspotList) {
                    nameList.add(hotspot.getName());
                }

                SpinnerAdapter adapter = new SpinnerAdapter(ResolveAnchorsLobbyActivity.this, nameList, ResolveAnchorsLobbyActivity.this);
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

    @Override
    public void onItemDeleted(int position) {
        //First need to find the current item, then delete it from the firebase
        Hotspot hotspot = hotspotList.get(position);
        Log.d("COMP3018", "Here is the size at start of deletion: "+ hotspotList.size());
        hotspotList.remove(position);
        Log.d("COMP3018", "Here is the after removal from list: "+ hotspotList.size());


        firebaseManager.removeHotspot(hotspot.getCode(), new FirebaseManager.DeleteCallback() {
            @Override
            public void onSuccess() {
                Log.d("COMP3018", "Here is in the onSuccess: "+ hotspotList.size());
                Log.d("COMP3018", "Successfully Deleted the firebase item");
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.d("COMP3018", errorMessage);
            }
        });

    }
}