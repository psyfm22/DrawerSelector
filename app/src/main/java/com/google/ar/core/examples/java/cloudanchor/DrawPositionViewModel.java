package com.google.ar.core.examples.java.cloudanchor;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class DrawPositionViewModel extends ViewModel {

    private final MutableLiveData<Integer> position = new MutableLiveData<>();

    public DrawPositionViewModel(){
        position.setValue(0);
    }


    public MutableLiveData<Integer> getPosition() {
        return position;
    }

    public void setPosition(int number){
        position.setValue(number);
    }
}
