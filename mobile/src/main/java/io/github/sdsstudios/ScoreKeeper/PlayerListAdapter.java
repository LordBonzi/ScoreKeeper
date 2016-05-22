package io.github.sdsstudios.ScoreKeeper;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by seth on 08/05/16.
 */
public class PlayerListAdapter extends RecyclerView.Adapter<PlayerListAdapter.ViewHolder> {
    private ArrayList<String> mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textViewPlayer;
        public Button buttonDelete;

        public ViewHolder(View v) {
            super(v);
            textViewPlayer = (TextView)v.findViewById(R.id.textViewPlayer);
            buttonDelete = (Button)v.findViewById(R.id.buttonDelete);

        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public PlayerListAdapter(ArrayList<String> myDataset) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PlayerListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.player_list_adapter, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textViewPlayer.setText(mDataset.get(0));
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        };
        holder.buttonDelete.setOnClickListener(onClickListener);


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
