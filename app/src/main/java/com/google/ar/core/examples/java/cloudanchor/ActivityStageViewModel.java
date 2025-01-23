package com.google.ar.core.examples.java.cloudanchor;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * ActivityStageViewModel,
 * View Model which keeps track of the stage the settings activity is in. This allows for the user
 * to rotate the phone and stage in the same stage of tha activity.
 */
public class ActivityStageViewModel extends ViewModel {
    private final MutableLiveData<SettingsActivityStage> currentStage = new MutableLiveData<>();

    /**
     * constructor,
     * Set the current stage to the initial value of menu as this is where the activity starts
     */
    public ActivityStageViewModel(){
        currentStage.setValue(SettingsActivityStage.MENU);
    }

    /**
     * setActivityStage,
     * Setter for currentStage
     *
     * @param settingsActivityStage input
     */
    public void setActivityStage(SettingsActivityStage settingsActivityStage){
        currentStage.setValue(settingsActivityStage);
    }

    /**
     * getActivityStage,
     * getter for currentStage
     *
     * @return currentStage
     */
    public MutableLiveData<SettingsActivityStage> getActivityStage(){
        return currentStage;
    }

}
