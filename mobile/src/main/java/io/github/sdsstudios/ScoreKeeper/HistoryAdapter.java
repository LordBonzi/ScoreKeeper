package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by seth on 08/05/16.
 */
public class HistoryAdapter extends SelectableAdapter<HistoryAdapter.ViewHolder>{

    public static List<GameModel> mGameModel;
    private static Context context;
    private int numGames;
    private AppCompatActivity activity;
    private ViewHolder viewHolder;
    private View view;

    private ViewHolder.ClickListener clickListener;

    // Provide a suitable constructor (depends on the kind of dataset)
    public HistoryAdapter(List<GameModel> gameModel, Context context1, int numGamesm, ViewHolder.ClickListener clickListener) {
        mGameModel = gameModel;
        context = context1;
        numGames =numGamesm;
        this.clickListener = clickListener;
    }

    // Create new views (invoked by t
    // he layout manager)
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_adapter, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(view, clickListener);

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

        if (isSelected(position)){
            Log.e("historyadapter", "visible");
            holder.selectedOverlay.setVisibility(View.VISIBLE);
            holder.relativeLayout.setBackgroundColor(context.getResources().getColor(R.color.colorAccent));


        }else {
            Log.e("historyadapter", "invisible");
            holder.selectedOverlay.setVisibility(View.INVISIBLE);
            TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            holder.relativeLayout.setBackgroundResource(outValue.resourceId);

        }




    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return numGames;
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        @SuppressWarnings("unused")

        // each data item is just a string in this case
        public TextView textViewHistoryPlayers;
        public TextView textViewHistoryScore;
        public TextView textViewHistoryDate;
        public TextView textViewHistoryType;
        public TextView textViewHistoryInProgress;
        public RelativeLayout relativeLayout;
        public View selectedOverlay;
        public int color;
        public String inProgress;

        private ClickListener listener;

        public ViewHolder(View v, ClickListener listener) {
            super(v);

            selectedOverlay = v.findViewById(R.id.selected_overlay);
            textViewHistoryPlayers = (TextView)v.findViewById(R.id.textViewHistoryPlayers);
            textViewHistoryDate = (TextView)v.findViewById(R.id.textViewHistoryDate);
            textViewHistoryScore = (TextView)v.findViewById(R.id.textViewHistoryScore);
            textViewHistoryType = (TextView)v.findViewById(R.id.textViewHistoryType);
            textViewHistoryInProgress = (TextView)v.findViewById(R.id.textViewHistoryInProgress);
            relativeLayout = (RelativeLayout)v.findViewById(R.id.relativeLayoutHistoryAdapter);
            color = v.getResources().getColor(R.color.colorAccent);
            inProgress = v.getResources().getString(R.string.in_progress);

            this.listener = listener;

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (listener != null) {
                listener.onItemClicked(getPosition());
            }

        }

        @Override
        public boolean onLongClick(View view) {

            if (listener != null) {
                return listener.onItemLongClicked(getPosition());
            }
            return false;
        }

        public interface ClickListener {
            public void onItemClicked(int position);
            public boolean onItemLongClicked(int position);
        }
    }




}
