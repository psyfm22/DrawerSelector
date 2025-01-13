package com.google.ar.core.examples.java.cloudanchor;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class SpinnerAdapter1 extends ArrayAdapter<String>  {
    private final List<String> items;
    private final LayoutInflater inflater;


    public SpinnerAdapter1(@NonNull Context context, List<String> names) {
        super(context, R.layout.spinner_item_without_delete, names);
        this.items = names;
        this.inflater = LayoutInflater.from(context);
    }
    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.spinner_item_without_delete, parent, false);
        }
        Log.d("COMP3018", "In the view");

        TextView itemText = view.findViewById(R.id.itemNameWDTV);
        itemText.setText(items.get(position));

        return view;
    }

    @Override
    public View getDropDownView(int position, View view, @NonNull ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.spinner_item_without_delete, parent, false);
        }
        Log.d("COMP3018","GetDropDownView");
        TextView itemText = view.findViewById(R.id.itemNameWDTV);
        itemText.setText(items.get(position));
        return view;
    }

}
