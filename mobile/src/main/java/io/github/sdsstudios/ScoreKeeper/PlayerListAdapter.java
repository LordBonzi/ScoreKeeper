package io.github.sdsstudios.ScoreKeeper;

import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by seth on 08/05/16.
 */
public class PlayerListAdapter extends RecyclerView.Adapter<PlayerListAdapter.ViewHolder>{
    private String backup;
    private int backupIndex;
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
        public void onBindViewHolder(final ViewHolder holder, int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.textViewPlayer.setText(mDataset.get(position));
            holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeAt(holder.getAdapterPosition());
                    Log.i("PlayerListAdapter", String.valueOf(mDataset));
                    mDbHelper.updateGame(mDataset, null, ScoreDBAdapter.KEY_PLAYERS,  mGameID );
                }
            });

        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }

    public void removeAt(int position) {
        final View.OnClickListener clickListener = new View.OnClickListener() {
            public void onClick(View v) {
                undoPlayerRemoval();
            }
        };

        if (mDataset.size() >= 3) {
            backup = mDataset.get(mDataset.size() - 1 );
            backupIndex = mDataset.indexOf(backup);
            mDataset.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mDataset.size());
            Snackbar snackbar = Snackbar.make(NewGame.newGameCoordinatorLayout, "Player removed", Snackbar.LENGTH_LONG)
                    .setAction("Undo", clickListener);
            snackbar.show();
        }else{
            undoPlayerRemoval();
        }
    }

    public void undoPlayerRemoval(){
        mDataset.add(backupIndex, backup);
        notifyItemRemoved(backupIndex);
        notifyItemRangeChanged(backupIndex, mDataset.size());
        Snackbar snackbar = Snackbar.make(NewGame.newGameCoordinatorLayout, "Undo Complete for removal of " + backup, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textViewPlayer;
        public Button buttonDelete;

        public ViewHolder(View v) {
            super(v);
            textViewPlayer = (TextView) v.findViewById(R.id.textViewPlayer);
            buttonDelete = (Button) v.findViewById(R.id.buttonDelete);

        }
        }

}
