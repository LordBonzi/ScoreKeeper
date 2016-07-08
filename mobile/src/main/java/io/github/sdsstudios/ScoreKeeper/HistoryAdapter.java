package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by seth on 08/05/16.
 */
public class HistoryAdapter extends SelectableAdapter<HistoryAdapter.ViewHolder>{

    public static List<GameModel> mGameModel;
    private static Context context;
    private int numGames;
    private GameModel gameModel;
    private SharedPreferences sharedPreferences;
    private boolean colorise;
    private int tab;
    private UpdateTabsListener updateTabsListener;
    public static boolean actionModeDisabled = true;

    private ViewHolder.ClickListener clickListener;

    // Provide a suitable constructor (depends on the kind of dataset)
    public HistoryAdapter(List<GameModel> gameModel, Context context1, int numGamesm, ViewHolder.ClickListener clickListener, int tab2) {
        mGameModel = gameModel;
        context = context1;
        numGames =numGamesm;
        this.clickListener = clickListener;
        tab = tab2;
    }

    public void removeItem(int position) {
        notifyItemRemoved(position);
    }

    public void removeItems(List<Integer> positions) {
        // Reverse-sort the list
        Collections.sort(positions, new Comparator<Integer>() {
            @Override
            public int compare(Integer lhs, Integer rhs) {
                return rhs - lhs;
            }
        });

        // Split the list in ranges
        while (!positions.isEmpty()) {
            if (positions.size() == 1) {
                removeItem(positions.get(0));
                positions.remove(0);
            } else {

                    int count = 1;
                    while (positions.size() > count && positions.get(count).equals(positions.get(count - 1) - 1)) {
                        ++count;
                    }

                    if (count == 1) {
                        removeItem(positions.get(0));
                    } else {
                        removeRange(positions.get(count - 1), count);
                    }

                    for (int i = 0; i < count; ++i) {
                        positions.remove(0);
                    }

            }
        }

    }

    public void deleteSelectedGames(ScoreDBAdapter dbHelper){
        for (int i = 0; i < getSelectedItems().size(); i++){
            dbHelper.deleteGame(getSelectedItems().get(i));
        }

        notifyDataSetChanged();
    }

    private void removeRange(int positionStart, int itemCount) {
        for (int i = 0; i < itemCount; ++i) {
            mGameModel.remove(positionStart);
        }
        notifyItemRangeRemoved(positionStart, itemCount);
    }

    // Create new views (invoked by t
    // he layout manager)

    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_adapter, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(view, clickListener);
        sharedPreferences = context.getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);

        colorise = sharedPreferences.getBoolean("prefColoriseUnfinishedGames", false);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        if (mGameModel.size() == 0){

        }else if(mGameModel.size() > 0){

            try {
                gameModel = mGameModel.get(mGameModel.size() - position - 1);

                holder.textViewHistoryPlayers.setText(gameModel.getPlayers());
                holder.textViewHistoryScore.setText(gameModel.getScore());
                holder.textViewHistoryDate.setText(gameModel.getDate());
                holder.textViewHistoryType.setText(gameModel.getType());

                if (tab != 1) {
                    holder.textViewHistoryInProgress.setText(gameModel.getState());

                    if (colorise) {
                        holder.textViewHistoryInProgress.setTextColor(context.getResources().getColor(R.color.colorAccent));
                    }
                }

                TypedValue outValue = new TypedValue();
                context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);

                if (isSelected(gameModel.getGameID()) ) {
                    holder.relativeLayout.setBackgroundColor(context.getResources().getColor(R.color.multiselect));
                } else if (!isSelected(gameModel.getGameID()) || actionModeDisabled){

                    holder.relativeLayout.setBackgroundResource(outValue.resourceId);
                }
            }catch (Exception e){

            }

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
        public int color;
        public String inProgress;

        private ClickListener listener;

        public ViewHolder(View v, ClickListener listener) {
            super(v);

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
                final GameModel gameModel;
                gameModel = mGameModel.get(mGameModel.size()-getAdapterPosition()-1);
                listener.onItemClicked(getAdapterPosition(), gameModel.getGameID());
            }

        }

        @Override
        public boolean onLongClick(View view) {

            if (listener != null) {
                final GameModel gameModel;
                gameModel = mGameModel.get(mGameModel.size()-getAdapterPosition()-1);
                return listener.onItemLongClicked(getAdapterPosition(), gameModel.getGameID());
            }

            return false;
        }

        public interface ClickListener {
            public void onItemClicked(int position, int gameID);
            public boolean onItemLongClicked(int position, int gameID);
        }
    }




}
