package com.google.ar.core.examples.java.cloudanchor;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ActivityStageViewModel extends ViewModel {
    private final MutableLiveData<ActivityStage> currentStage = new MutableLiveData<>();

    public ActivityStageViewModel(){
        currentStage.setValue(ActivityStage.MENU);
    }

    public void setActivityStage(ActivityStage activityStage){
        currentStage.setValue(activityStage);
    }

    public MutableLiveData<ActivityStage> getActivityStage(){
        return currentStage;
    }

}
