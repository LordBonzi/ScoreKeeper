package io.github.sdsstudios.ScoreKeeper;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by seth on 08/05/16.
 */
public class BigGameAdapter extends RecyclerView.Adapter<BigGameAdapter.ViewHolder> {
    ScoreDBAdapter dbHelper;
    int gameID;
    private int maxScore, scoreInterval, diffToWin;
    private List<Player> mPlayerArray;
    private boolean enabled, reverseScrolling;
    private GameListener gameListener;

    // Provide a suitable constructor (depends on the kind of dataset)
    public BigGameAdapter(List<Player> mPlayerArray, ScoreDBAdapter dbAdapter,
                          int id, boolean menabled, int maxScore, GameListener gameListener, boolean reverseScrolling, int scoreInterval, int diffToWin) {

        this.mPlayerArray = mPlayerArray;
        dbHelper =dbAdapter;
        gameID = id;
        enabled = menabled;
        this.maxScore = maxScore;
        this.gameListener = gameListener;
        this.reverseScrolling = reverseScrolling;
        this.scoreInterval = scoreInterval;
        this.diffToWin = diffToWin;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.big_game_adapter, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Player p = mPlayerArray.get(position);

        holder.textViewPlayer.setText(p.getmName());
        holder.butonScore.setText(String.valueOf(p.getmScore()));

        if (enabled) {
            holder.butonScore.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int score = 0;
                    int buttonScore = 0;

                    buttonScore = Integer.valueOf(holder.butonScore.getText().toString());
                    if (reverseScrolling){
                        score = buttonScore -= scoreInterval;
                    }else {
                        score = buttonScore += scoreInterval;
                    }

                    holder.butonScore.setText(String.valueOf(score));
                    p.setmScore(score);
                    dbHelper.open().updatePlayers(mPlayerArray, gameID);

                    if (maxScore != 0) {
                        for (int i = 0; i < mPlayerArray.size(); i++) {
                            if (maxScore < 0) {
                                if (p.getmScore() <= maxScore && scoreDifference(score)) {
                                    gameListener.gameWon(p.getmName());
                                }

                            } else if (maxScore >= 0) {
                                if (p.getmScore() >= maxScore&& scoreDifference(score)) {
                                    gameListener.gameWon(p.getmName());
                                }

                            }

                        }
                    }

                }
            });

            holder.butonScore.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int score = 0;
                    int buttonScore = 0;

                    buttonScore = Integer.valueOf(holder.butonScore.getText().toString());
                    if (reverseScrolling){
                        score = buttonScore += scoreInterval;
                    }else {
                        score = buttonScore -= scoreInterval;
                    }
                    if (score == -1) {

                    } else {
                        holder.butonScore.setText(String.valueOf(score));
                        p.setmScore(score);
                        dbHelper.open().updatePlayers(mPlayerArray, gameID);
                    }

                    return true;
                }
            });

            holder.imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    gameListener.deletePlayer(position);
                }
            });

            holder.editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    gameListener.editPlayer(position);
                }
            });

        }else{
            holder.butonScore.setEnabled(false);
        }
    }

    private boolean scoreDifference(int score){
        boolean b = false;
        for (int i = 0; i < mPlayerArray.size(); i++){
            if (Math.abs(score-Integer.valueOf(String.valueOf(mPlayerArray.get(i).getmScore()))) >= diffToWin){
                b = true;
            }
        }
        return  b;
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mPlayerArray.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textViewPlayer;
        public Button butonScore;
        public ImageButton imageButton, editButton;


        public ViewHolder(View v) {
            super(v);
            textViewPlayer = (TextView)v.findViewById(R.id.listTextViewPlayer);
            butonScore = (Button) v.findViewById(R.id.listButtonScore);
            imageButton = (ImageButton)v.findViewById(R.id.buttonDelete);
            editButton = (ImageButton)v.findViewById(R.id.buttonEdit);

        }
    }

    public interface GameListener{
        void gameWon(String winner);
        void deletePlayer(int position);
        void editPlayer(int position);
    }
}
