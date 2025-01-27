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

/**
 * SpinnerAdapterWithDelete,
 * Define the spinner adapter for when allowing the user to delete items in the string
 */
public class SpinnerAdapterWithDelete extends ArrayAdapter<String> {
    private final List<String> items;
    private final LayoutInflater inflater;
    private final ItemDeletedListener listener;

    /**
     * constructor,
     * Set the items for the spinner, the context and the listener for when items are deleted
     *
     * @param context context adapter is being used in (so activity)
     * @param items the names of the anchors in the list
     * @param itemDeletedListener listener for when the item is deleted
     */
    public SpinnerAdapterWithDelete(Context context, List<String> items, ItemDeletedListener itemDeletedListener) {
        super(context, R.layout.spinner_item, items);
        this.items = items;
        this.inflater = LayoutInflater.from(context);
        this.listener = itemDeletedListener;
    }

    //Interface definition for when an item is deleted
    public interface ItemDeletedListener {
        void onItemDeleted(int position);
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

        //Initialise and set the textview, don't need to add the delete here
        TextView itemText = view.findViewById(R.id.itemNameTV);
        itemText.setText(items.get(position));

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

        //Initialise the textview and the delete Button
        TextView itemText = view.findViewById(R.id.itemNameTV);
        ImageView deleteButton = view.findViewById(R.id.itemDeleteIV);

        //Set the item text
        itemText.setText(items.get(position));

        //What to do when the delete button is pressed
        deleteButton.setOnClickListener(v -> {

            //Remove the item from the list and notify there has been a change in dataset
            items.remove(position);
            notifyDataSetChanged();

            //Call the listener while passing it the position
            if (listener != null) {
                listener.onItemDeleted(position);
            }
        });

        return view;
    }
}
