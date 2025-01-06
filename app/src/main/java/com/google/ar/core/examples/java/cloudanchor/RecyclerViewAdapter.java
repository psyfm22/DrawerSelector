package com.google.ar.core.examples.java.cloudanchor;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.viewHolder>{
    private List<Hotspot> hotspotList;
    private final LayoutInflater layoutInflater;
    private final Context context;


    public RecyclerViewAdapter(Context context, List<Hotspot> hotspotList) {
        //Assign and initialise the different variables
        this.context = context;
        this.hotspotList = hotspotList;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Creates and returns a new CollageViewHolder object
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return CollageViewHolder A new CollageViewHolder that holds the inflated View.
     */
    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView= layoutInflater.inflate(R.layout.recycler_view_layout, parent, false);

        return new viewHolder(itemView, this);
    }

    /**
     * onBindViewHolder, Binds the data item at the views inside the View holder
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(viewHolder holder, int position) {
        holder.bind(hotspotList.get(position), this);
    }

    @Override
    public int getItemCount() {
        return hotspotList.size();
    }

    static class viewHolder extends RecyclerView.ViewHolder {
        TextView messageTV;
        ImageView deleteIV;

        /**
         * CollageViewHolder Constructor, assign all the imageViews
         *
         * @param itemView The view of the collage card
         */
        viewHolder(View itemView, final RecyclerViewAdapter adapter) {
            super(itemView);

            messageTV = itemView.findViewById(R.id.viewMessageTV);
            deleteIV = itemView.findViewById(R.id.viewDeleteIV);
        }

        void bind(final Hotspot hotspot, final RecyclerViewAdapter adapter) {
            // For each image stored in the collage card
            messageTV.setText(hotspot.getName());

        }
    }
}