/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ar.core.examples.java.cloudanchor;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** A helper class to manage all communications with Firebase. */
class FirebaseManager {
  private static final String TAG = "COMP3018";
  /** Listener for a new room code. */
  interface RoomCodeListener {

    /** Invoked when a new room code is available from Firebase. */
    void onNewRoomCode(Long newRoomCode);

    /** Invoked if a Firebase Database Error happened while fetching the room code. */
    void onError(DatabaseError error);
  }

  /** Listener for a new cloud anchor ID. */
  interface CloudAnchorIdListener {

    /** Invoked when a new cloud anchor ID is available. */
    void onNewCloudAnchorId(String cloudAnchorId);
  }

  // Names of the nodes used in the Firebase Database
  private static final String ROOT_FIREBASE_HOTSPOTS = "hotspot_list",
          ROOT_LAST_ROOM_CODE = "last_room_code", KEY_LOCATION_NAME = "location_name",
          KEY_ANCHOR_ID = "hosted_anchor_id", KEY_TIMESTAMP = "updated_at_timestamp",
          KEY_CURRENT_STORAGE = "current_storage", KEY_MAX_STORAGE = "max_storage",
          KEY_CATEGORY = "category_name", ROOT_PASSCODE = "password_code";
  private final FirebaseApp app;
  private final DatabaseReference hotspotListRef, roomCodeRef, passcodeRef;
  private DatabaseReference currentRoomRef = null;
  private ValueEventListener currentRoomListener = null;

  /**
   * Default constructor for the FirebaseManager.
   *
   * @param context The application context.
   */
  FirebaseManager(Context context) {
    app = FirebaseApp.initializeApp(context);
    if (app != null) {

      DatabaseReference rootRef = FirebaseDatabase.getInstance(app).getReference();

      FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
      firebaseAppCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance());

      hotspotListRef = rootRef.child(ROOT_FIREBASE_HOTSPOTS);
      roomCodeRef = rootRef.child(ROOT_LAST_ROOM_CODE);
      passcodeRef = rootRef.child(ROOT_PASSCODE);

      DatabaseReference.goOnline();

    } else {
      hotspotListRef = null;
      roomCodeRef = null;
      passcodeRef = null;
    }
  }

  /**
   * Gets a new room code from the Firebase Database. Invokes the listener method when a new room
   * code is available.
   */
  void getNewRoomCode(RoomCodeListener listener) {
    Preconditions.checkNotNull(app, "Firebase App was null");
    roomCodeRef.runTransaction(
        new Transaction.Handler() {
          @Override
          public Transaction.Result doTransaction(MutableData currentData) {
            Long nextCode = Long.valueOf(1);
            Object currVal = currentData.getValue();
            if (currVal != null) {
              Long lastCode = Long.valueOf(currVal.toString());
              nextCode = lastCode + 1;
            }
            currentData.setValue(nextCode);
            return Transaction.success(currentData);
          }

          @Override
          public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
            if (!committed) {
              listener.onError(error);
              return;
            }
            Long roomCode = currentData.getValue(Long.class);
            listener.onNewRoomCode(roomCode);
          }
        });
  }

  /** Stores the given anchor ID in the given room code. */
  void storeAnchorIdInRoom(Long roomCode, String cloudAnchorId, String displayName) {
    Preconditions.checkNotNull(app, "Firebase App was null");
    DatabaseReference roomRef = hotspotListRef.child(String.valueOf(roomCode));
    roomRef.child(KEY_LOCATION_NAME).setValue(displayName);
    roomRef.child(KEY_ANCHOR_ID).setValue(cloudAnchorId);
    roomRef.child(KEY_TIMESTAMP).setValue(System.currentTimeMillis());
    roomRef.child(KEY_CURRENT_STORAGE).setValue(0);
    roomRef.child(KEY_MAX_STORAGE).setValue(0);
    roomRef.child(KEY_CATEGORY).setValue("");
  }

  /**
   * Registers a new listener for the given room code. The listener is invoked whenever the data for
   * the room code is changed.
   */
  void registerNewListenerForRoom(Long roomCode, CloudAnchorIdListener listener) {
    Preconditions.checkNotNull(app, "Firebase App was null");
    clearRoomListener();
    currentRoomRef = hotspotListRef.child(String.valueOf(roomCode));
    currentRoomListener =
        new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {
            Object valObj = dataSnapshot.child(KEY_ANCHOR_ID).getValue();
            if (valObj != null) {
              String anchorId = String.valueOf(valObj);
              if (!anchorId.isEmpty()) {
                listener.onNewCloudAnchorId(anchorId);
              }
            }
          }

          @Override
          public void onCancelled(DatabaseError databaseError) {
            Log.w(TAG, "The Firebase operation was cancelled.", databaseError.toException());
          }
        };
    currentRoomRef.addValueEventListener(currentRoomListener);
  }

  /**
   * Resets the current room listener registered using {@link #registerNewListenerForRoom(Long,
   * CloudAnchorIdListener)}.
   */
  void clearRoomListener() {
    if (currentRoomListener != null && currentRoomRef != null) {
      currentRoomRef.removeEventListener(currentRoomListener);
      currentRoomListener = null;
      currentRoomRef = null;
    }
  }


  /* Need to test this sufficiently */
  void removeHotspot(long key, final DeleteCallback deleteCallback){
    Preconditions.checkNotNull(app, "Firebase App was null");

    DatabaseReference hotspotRef = hotspotListRef.child(String.valueOf(key));

    // Remove the specific hotspot
    hotspotRef.removeValue()
            .addOnSuccessListener(aVoid -> {
              Log.d(TAG, "Hotspot with key " + key + " removed successfully.");
              deleteCallback.onSuccess();
            })
            .addOnFailureListener(e ->{
              Log.e(TAG, "Error removing hotspot with key " + key + ": " + e.getMessage());
              deleteCallback.onFailure(e.getMessage());
            });
  }

  void removeAllData(final DeleteAllCallback deleteAllCallback){
    Preconditions.checkNotNull(app, "Firebase App was null");

    //Remove the value
    hotspotListRef.removeValue()
            .addOnSuccessListener(aVoid -> {
              Log.d(TAG, "All hotspot data removed successfully.");

              // Remove data from the "last_room_code" node
              roomCodeRef.removeValue()
                      .addOnSuccessListener(aAVoid -> {
                        deleteAllCallback.onSuccess();
                        Log.d(TAG, "Last room code removed successfully.");
                      })
                      .addOnFailureListener(e -> {
                        deleteAllCallback.onFailure(e.getMessage());
                        Log.e(TAG, "Error removing last room code: " + e.getMessage());
                      });
            })
            .addOnFailureListener(e -> {
              deleteAllCallback.onFailure(e.getMessage());
              Log.e(TAG, "Error removing hotspot data: " + e.getMessage());
            });
  }


  void getHotspotList(final HotspotListListener listener) {
    Preconditions.checkNotNull(app, "Firebase App was null");

    List<Hotspot> hotspotList = new ArrayList<>();

    ValueEventListener valueEventListener = new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
          String keyString = snapshot.getKey();

          long keyLong = 0;
          if(keyString != null){
            keyLong = Long.parseLong(keyString);
          }

          String locationName = snapshot.child(KEY_LOCATION_NAME).getValue(String.class);
          String categoryName = snapshot.child(KEY_CATEGORY).getValue(String.class);
          Integer currentStorage = snapshot.child(KEY_CURRENT_STORAGE).getValue(int.class);
          Integer maxStorage = snapshot.child(KEY_MAX_STORAGE).getValue(int.class);

          int finalCurrentStorage = Objects.requireNonNullElse(currentStorage, 0);
          int finalMaxStorage = Objects.requireNonNullElse(maxStorage, 0);

          Hotspot hotspot = new Hotspot(keyLong, locationName, categoryName, finalCurrentStorage,finalMaxStorage);
          hotspotList.add(hotspot);
        }
        listener.onHotspotListFetched(hotspotList);


        if (hotspotListRef != null) {
          hotspotListRef.removeEventListener(this);
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        Log.d("Hotspot", "Failed to read value.", error.toException());
        listener.onError(error);
      }
    };

    hotspotListRef.addValueEventListener(valueEventListener);
  }

  void checkPasscode(String passcode, final PasscodeCallback callback){
    Preconditions.checkNotNull(app, "Firebase App was null");

    passcodeRef.get().addOnCompleteListener(task -> {
      if(task.isSuccessful()){
        String foundPassword = task.getResult().getValue(String.class);
        if(foundPassword != null){
          boolean doPasswordsMatch = BCrypt.checkpw(passcode, foundPassword);
          if(doPasswordsMatch){
            callback.onSuccess();
          }else {
            callback.onPasswordsDiffer();
          }
        }else{
          String hashedPassword = BCrypt.hashpw(passcode, BCrypt.gensalt());
          passcodeRef.setValue(hashedPassword)
                  .addOnSuccessListener(aVoid -> callback.onSuccess())
                  .addOnFailureListener(e -> callback.onPasswordUploadFailure(e.getMessage()));
        }
      }
    });
  }

  void changePasscode(String passcode, final ChangePasscodeCallback callback){
    Preconditions.checkNotNull(app, "Firebase App was null");

    passcodeRef.setValue(passcode)
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));

  }

  void inputComponent(int currentStorage, int maxStorage, String category) {
    Preconditions.checkNotNull(app, "Firebase App was null");
    List<Hotspot> hotspotList = new ArrayList<>();

    ValueEventListener valueEventListener = new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

          String keyString = snapshot.getKey();

          long keyLong = 0;
          if(keyString != null){
            keyLong = Long.parseLong(keyString);
          }

          String locationName = snapshot.child(KEY_LOCATION_NAME).getValue(String.class);
          String categoryName = snapshot.child(KEY_CATEGORY).getValue(String.class);
          Integer currentStorage = snapshot.child(KEY_CURRENT_STORAGE).getValue(int.class);
          Integer maxStorage = snapshot.child(KEY_MAX_STORAGE).getValue(int.class);

          int finalCurrentStorage = Objects.requireNonNullElse(currentStorage, 0);
          int finalMaxStorage = Objects.requireNonNullElse(maxStorage, 0);

          Hotspot hotspot = new Hotspot(keyLong, locationName, categoryName, finalCurrentStorage,finalMaxStorage);
          hotspotList.add(hotspot);
        }

        if (hotspotList != null) {
          Log.d("COMP3018", "Here: " + hotspotList.size());
        } else {
          Log.d("COMP3018", "hotspotList is null");
        }
        Log.d("COMP3018", "Hotspot size: "+ hotspotList.size());

        Log.d("COMP3018", hotspotList.get(0).getCategory());

        boolean isCategoryWithSpace = false;

        for (int i=0; i<hotspotList.size();i++){
          Log.d("COMP3018",""+ i);
          Hotspot hotspot = hotspotList.get(i);
          if(hotspot.getCategory().equals(category)){
            if(hotspot.getCurrentStorage() != hotspot.getMaxStorage()){
              isCategoryWithSpace = true;
              Hotspot updatedHotspot = new Hotspot(hotspot.getCode(), hotspot.getName(), hotspot.getCategory(),
                      hotspot.getCurrentStorage()+1, hotspot.getMaxStorage());
              hotspotList.set(i, updatedHotspot);
            }
          }
        }
        Log.d("COMP3018","boolean is category with space: "+ isCategoryWithSpace);
        if(isCategoryWithSpace){
          if (hotspotListRef != null) {
            hotspotListRef.setValue(hotspotList)
                    .addOnSuccessListener(aVoid -> {
                      Log.d("COMP3018", "On Success of Changing");
                    })
                    .addOnFailureListener(e -> {
                      Log.d("COMP3018", "On Failure");
                    });
          }
        }


        boolean successUpload = false;
        if(!isCategoryWithSpace ){

          for (int i=0; i<hotspotList.size();i++) {
            Hotspot hotspot = hotspotList.get(i);
            if(hotspot.getCategory().isEmpty()){
              Log.d("COMP3018","Do I get here in the hotspot category is empty ");
              Hotspot updatedHotspot = new Hotspot(hotspot.getCode(), hotspot.getName(), category, currentStorage, maxStorage);
              hotspotList.set(i, updatedHotspot);
              successUpload = true;
            }
          }
          if(successUpload){
            if (hotspotListRef != null) {
              hotspotListRef.setValue(hotspotList)
                      .addOnSuccessListener(aVoid -> {
                        Log.d("COMP3018", "On Success of Changing");
                      })
                      .addOnFailureListener(e -> {
                        Log.d("COMP3018", "On Failure");
                      });
            }
          }
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        Log.d("Hotspot", "Failed to read value.", error.toException());
      }
    };

    hotspotListRef.addValueEventListener(valueEventListener);
  }

  interface HotspotListListener {
    void onHotspotListFetched(List<Hotspot> displayNames);

    void onError(DatabaseError error);
  }

  interface DeleteCallback {
    void onSuccess();
    void onFailure(String errorMessage);
  }

  interface PasscodeCallback {
    void onSuccess();
    void onPasswordUploadFailure(String errorMessage);
    void onPasswordsDiffer();
  }
  interface ChangePasscodeCallback {
    void onSuccess();
    void onFailure(String errorMessage);
  }

  interface DeleteAllCallback {
    void onSuccess();
    void onFailure(String errorMessage);
  }
}
