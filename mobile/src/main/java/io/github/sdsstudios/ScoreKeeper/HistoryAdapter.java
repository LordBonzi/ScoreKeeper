package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by seth on 08/05/16.
 */
public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder>{

    private ScoreDBAdapter mdbHelper;
    private List<GameModel> mGameModel;
    private Context context;
    private RelativeLayout relativeLayout;
    private int mActivity;
    private int numGames;

    // Provide a suitable constructor (depends on the kind of dataset)
    public HistoryAdapter(List<GameModel> gameModel, ScoreDBAdapter dbHelper, Context context1, RelativeLayout layout, int activity, int numGamesm) {
        mGameModel = gameModel;
        mdbHelper = dbHelper;
        context = context1;
        relativeLayout = layout;
        mActivity = activity;
        numGames =numGamesm;
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
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        GameModel gameModel;

        gameModel = mGameModel.get(mGameModel.size()-position-1);
        holder.textViewHistoryPlayers.setText(gameModel.getPlayers());
        holder.textViewHistoryScore.setText(gameModel.getScore());
        holder.textViewHistoryDate.setText(gameModel.getDate());
        holder.textViewHistoryType.setText(gameModel.getType());

        if (gameModel.getState().equals("IN PROGRESS")) {
            holder.textViewHistoryInProgress.setTextColor(holder.color);
        }


        holder.textViewHistoryInProgress.setText(gameModel.getState());


        holder.relativeLayout.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                Intent intent = new Intent(context, EditGame.class);
                int gameID = Integer.valueOf(mdbHelper.getNewestGame())-position;
                intent.putExtra("gameID", gameID);
                context.startActivity(intent);
            }
        });

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return numGames;
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
        public TextView textViewHistoryInProgress;
        public RelativeLayout relativeLayout;
        public int color;

        public ViewHolder(View v) {
            super(v);
            textViewHistoryPlayers = (TextView)v.findViewById(R.id.textViewHistoryPlayers);
            textViewHistoryDate = (TextView)v.findViewById(R.id.textViewHistoryDate);
            textViewHistoryScore = (TextView)v.findViewById(R.id.textViewHistoryScore);
            textViewHistoryType = (TextView)v.findViewById(R.id.textViewHistoryType);
            textViewHistoryInProgress = (TextView)v.findViewById(R.id.textViewInProgress);
            relativeLayout = (RelativeLayout)v.findViewById(R.id.historyLayout);
            color = v.getResources().getColor(R.color.colorAccent);


        }
    }
}
