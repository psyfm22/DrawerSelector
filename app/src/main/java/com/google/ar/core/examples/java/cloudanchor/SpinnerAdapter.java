package com.google.ar.core.examples.java.cloudanchor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;
/**
 * SpinnerAdapter,,
 * Define the spinner adapter for when allowing the user with allowing for deleting items
 */
public class SpinnerAdapter extends ArrayAdapter<String>  {
    private final List<String> items;
    private final LayoutInflater inflater;

    /**
     * constructor,
     * Set the items for the spinner and the context
     *
     * @param context context adapter is being used in (so activity)
     * @param names the names of the anchors in the list
     */
    public SpinnerAdapter(@NonNull Context context, List<String> names) {
        super(context, R.layout.spinner_item, names);
        this.items = names;
        this.inflater = LayoutInflater.from(context);
    }

    /**
     * getView,
     * This is called when the when spinner displays a single item
     *
     * @param position item position in the list
     * @param view the existing view for the item
     * @param parent parent view group
     * @return the view to be displayed by spinner
     */
    @NonNull
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.spinner_item, parent, false);
        }

        //Initialise and set the item Text
        TextView nameTV = view.findViewById(R.id.spinnerNameTV);
        nameTV.setText(items.get(position));

        //Return the view
        return view;
    }

    /**
     * getDropDownView
     * Method is run when the spinner is clicked on and display the whole list of items
     *
     * @param position item position in the list
     * @param view the existing view for the item
     * @param parent parent view group
     * @return the view to be displayed by spinner
     */
    @Override
    public View getDropDownView(int position, View view, @NonNull ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.spinner_item, parent, false);
        }
        //Initialise and set the item Text
        TextView nameTV = view.findViewById(R.id.spinnerNameTV);
        nameTV.setText(items.get(position));

        //Return the view
        return view;
    }

}
