package com.google.ar.core.examples.java.cloudanchor;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class DrawSelectedViewModel extends ViewModel {
    private final MutableLiveData<String> currentSelection = new MutableLiveData<>();
    private final MutableLiveData<List<String>> drawerList = new MutableLiveData<>();


    public MutableLiveData<List<String>> getDrawerList() {
        return drawerList;
    }

    public void setDrawList(List<String> drawerList) {
        this.drawerList.setValue(drawerList);
    }

    public void deleteItem(int index){
        List<String> newList = drawerList.getValue();
        if (newList != null && !newList.isEmpty() && index >= 0 && index < newList.size()) {
            newList.remove(index);
        }
        drawerList.setValue(newList);
    }

    public void setCurrentSelection(String drawerName){
        currentSelection.setValue(drawerName);
    }

    public MutableLiveData<String> getCurrentSelection(){
        return currentSelection;
    }
}
