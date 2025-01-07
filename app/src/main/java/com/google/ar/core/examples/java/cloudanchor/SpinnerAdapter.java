package com.google.ar.core.examples.java.cloudanchor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class SpinnerAdapter extends ArrayAdapter<String> {
    private final List<String> items;
    private final LayoutInflater inflater;

    public SpinnerAdapter(Context context, List<String> items) {
        super(context, R.layout.spinner_item, items);
        this.items = items;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.spinner_item, parent, false);
        }

        TextView itemText = view.findViewById(R.id.itemNameTV);

        itemText.setText(items.get(position));

        return view;
    }

    @Override
    public View getDropDownView(int position, View view, @NonNull ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.spinner_item, parent, false);
        }

        TextView itemText = view.findViewById(R.id.itemNameTV);
        ImageView deleteButton = view.findViewById(R.id.itemDeleteIV);

        itemText.setText(items.get(position));

        deleteButton.setOnClickListener(v -> {
            items.remove(position);
            notifyDataSetChanged();
        });

        return view;
    }
}
