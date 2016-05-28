package io.github.sdsstudios.ScoreKeeper;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by seth on 08/05/16.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<GameModel> mGameModel;

    // Provide a suitable constructor (depends on the kind of dataset)
    public HistoryAdapter(List<GameModel> gameModel) {
        mGameModel = gameModel;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_adapter, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        GameModel gameModel = mGameModel.get(position);

        holder.textViewHistoryPlayers.setText(gameModel.getPlayers());
        holder.textViewHistoryScore.setText(gameModel.getScore());
        holder.textViewHistoryDate.setText(gameModel.getDate());
        holder.textViewHistoryType.setText(gameModel.getType());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mGameModel.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textViewHistoryPlayers;
        public TextView textViewHistoryScore;
        public TextView textViewHistoryDate;
        public TextView textViewHistoryType;

        public ViewHolder(View v) {
            super(v);
            textViewHistoryPlayers = (TextView)v.findViewById(R.id.textViewHistoryPlayers);
            textViewHistoryDate = (TextView)v.findViewById(R.id.textViewHistoryDate);
            textViewHistoryScore = (TextView)v.findViewById(R.id.textViewHistoryScore);
            textViewHistoryType = (TextView)v.findViewById(R.id.textViewHistoryType);

        }
    }
}
