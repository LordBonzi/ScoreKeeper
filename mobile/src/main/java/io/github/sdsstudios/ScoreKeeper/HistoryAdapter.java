package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

    private List<GameModel> mGameModel;
    private Context context;
    private int numGames;

    // Provide a suitable constructor (depends on the kind of dataset)
    public HistoryAdapter(List<GameModel> gameModel, Context context1, int numGamesm) {
        mGameModel = gameModel;
        context = context1;
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
        final GameModel gameModel;

        gameModel = mGameModel.get(mGameModel.size()-position-1);
        holder.textViewHistoryPlayers.setText(gameModel.getPlayers());
        holder.textViewHistoryScore.setText(gameModel.getScore());
        holder.textViewHistoryDate.setText(gameModel.getDate());
        holder.textViewHistoryType.setText(gameModel.getType());

        if (gameModel.getState().equals(holder.inProgress)) {
            holder.textViewHistoryInProgress.setTextColor(holder.color);
        }

        holder.textViewHistoryInProgress.setText(gameModel.getState());
        holder.relativeLayout.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                if (gameModel.getState().equals(holder.inProgress)){

                    AlertDialog dialog;
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);

                    builder.setTitle(R.string.carry_on);

                    builder.setMessage(R.string.continue_game_message);

                    builder.setNeutralButton(R.string.edit, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(context, EditGame.class);
                            Log.i("gameIDHistory", ""+ gameModel.getGameID());
                            intent.putExtra("gameID", gameModel.getGameID());
                            context.startActivity(intent);
                        }
                    });

                    builder.setPositiveButton(R.string.carry_on, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(context, MainActivity.class);
                            intent.putExtra("gameID", gameModel.getGameID());
                            context.startActivity(intent);
                        }
                    });

                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

                    dialog = builder.create();
                    dialog.show();

                }else {
                    Intent intent = new Intent(context, EditGame.class);
                    intent.putExtra("gameID", gameModel.getGameID());
                    context.startActivity(intent);
                }
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
        public String inProgress;

        public ViewHolder(View v) {
            super(v);
            textViewHistoryPlayers = (TextView)v.findViewById(R.id.textViewHistoryPlayers);
            textViewHistoryDate = (TextView)v.findViewById(R.id.textViewHistoryDate);
            textViewHistoryScore = (TextView)v.findViewById(R.id.textViewHistoryScore);
            textViewHistoryType = (TextView)v.findViewById(R.id.textViewHistoryType);
            textViewHistoryInProgress = (TextView)v.findViewById(R.id.textViewInProgress);
            relativeLayout = (RelativeLayout)v.findViewById(R.id.historyLayout);
            color = v.getResources().getColor(R.color.colorAccent);
            inProgress = v.getResources().getString(R.string.in_progress);


        }
    }
}
