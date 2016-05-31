package io.github.sdsstudios.ScoreKeeper;

import android.media.Image;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by seth on 08/05/16.
 */
public class PlayerListAdapter extends RecyclerView.Adapter<PlayerListAdapter.ViewHolder>{
    Snackbar snackbar = null;
    private String backup;
    private ArrayList<String> mDataset;
    private ScoreDBAdapter mDbHelper;
    private int mGameID;


    // Provide a suitable constructor (depends on the kind of dataset)
        public PlayerListAdapter(ArrayList<String> myDataset, ScoreDBAdapter dbHelper, int gameID) {
            mDataset = myDataset;
            mDbHelper = dbHelper;
            mGameID = gameID;
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
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element

            holder.textViewPlayer.setText(mDataset.get(position));
            holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeAt(position);
                }
            });

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }

    public void removeAt(int position) {

        backup = mDataset.get(position);
        mDataset.remove(position);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undoPlayerRemoval();
            }
        };

        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mDataset.size());

        mDbHelper.updateGame(mDataset, null, 1, ScoreDBAdapter.KEY_PLAYERS,  mGameID );
        Snackbar snackbar = Snackbar.make(NewGame.newGameCoordinatorLayout, "Player removed.", Snackbar.LENGTH_LONG)
                .setAction("Undo", onClickListener);
        snackbar.show();

    }

    public void undoPlayerRemoval(){
        mDataset.add(mDataset.size(), backup);
        notifyItemRemoved(mDataset.size());
        notifyItemRangeChanged(mDataset.size(), mDataset.size());
        mDbHelper.updateGame(mDataset, null, 1, ScoreDBAdapter.KEY_PLAYERS,  mGameID );

        snackbar = Snackbar.make(NewGame.newGameCoordinatorLayout, "Undo complete.", Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textViewPlayer;
        public ImageButton buttonDelete;

        public ViewHolder(View v) {
            super(v);
            textViewPlayer = (TextView) v.findViewById(R.id.textViewPlayer);
            buttonDelete = (ImageButton) v.findViewById(R.id.buttonDelete);

        }
        }

}
