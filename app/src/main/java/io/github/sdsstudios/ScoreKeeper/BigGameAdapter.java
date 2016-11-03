package io.github.sdsstudios.ScoreKeeper;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

/**
 * Created by seth on 08/05/16.
 */
public class BigGameAdapter extends RecyclerView.Adapter<BigGameAdapter.ViewHolder> {
    ScoreDBAdapter dbHelper;
    private Game mGame;
    private boolean enabled;
    private GameListener gameListener;
    private boolean reverseScoring;
    private int scoreInterval;
    private int maxScore;
    private int diffToWin;


    // Provide a suitable constructor (depends on the kind of dataset)
    public BigGameAdapter(Game mGame, ScoreDBAdapter dbAdapter, boolean menabled, GameListener gameListener) {

        dbHelper = dbAdapter;
        enabled = menabled;
        this.gameListener = gameListener;
        this.mGame = mGame;

        reverseScoring = mGame.isChecked(CheckBoxOption.REVERSE_SCORING);
        scoreInterval = mGame.getData(EditTextOption.SCORE_INTERVAL);
        maxScore = mGame.getData(EditTextOption.WINNING_SCORE);
        diffToWin = mGame.getData(EditTextOption.SCORE_DIFF_TO_WIN);

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
        final Player p = mGame.getPlayer(position);

        holder.textViewPlayer.setText(p.getmName());
        holder.buttonScore.setText(String.valueOf(p.getmScore()));

        if (enabled) {
            holder.buttonScore.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int score = 0;
                    int buttonScore = 0;


                    buttonScore = Integer.valueOf(holder.buttonScore.getText().toString());
                    if (reverseScoring) {
                        score = buttonScore -= scoreInterval;
                    } else {
                        score = buttonScore += scoreInterval;
                    }

                    holder.buttonScore.setText(String.valueOf(score));
                    p.setmScore(score);

                    if (maxScore != 0) {
                        if (maxScore < 0) {
                            if (p.getmScore() <= maxScore && scoreDifference(score)) {
                                gameListener.gameWon(p.getmName());
                            }

                        } else if (maxScore >= 0) {
                            if (p.getmScore() >= maxScore && scoreDifference(score)) {
                                gameListener.gameWon(p.getmName());
                            }

                        }

                    }

                }
            });

            holder.buttonScore.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int score = 0;
                    int buttonScore = 0;

                    buttonScore = Integer.valueOf(holder.buttonScore.getText().toString());

                    if (reverseScoring) {
                        score = buttonScore += scoreInterval;
                    } else {
                        score = buttonScore -= scoreInterval;
                    }
                    if (score == -1) {

                    } else {
                        holder.buttonScore.setText(String.valueOf(score));
                        p.setmScore(score);
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

            mGame.setPlayer(p, position);
            dbHelper.updateGame(mGame);

        } else {
            holder.buttonScore.setEnabled(false);
        }


    }

    private boolean scoreDifference(int score) {
        boolean b = false;
        List<Player> playerArray = mGame.getmPlayerArray();

        for (int i = 0; i < playerArray.size(); i++) {
            if (Math.abs(score - Integer.valueOf(String.valueOf(playerArray.get(i).getmScore()))) >= diffToWin) {
                b = true;
            }
        }
        return b;
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mGame.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textViewPlayer;
        public Button buttonScore;
        public ImageButton imageButton, editButton;


        public ViewHolder(View v) {
            super(v);
            textViewPlayer = (TextView) v.findViewById(R.id.listTextViewPlayer);
            buttonScore = (Button) v.findViewById(R.id.listButtonScore);
            imageButton = (ImageButton) v.findViewById(R.id.buttonDelete);
            editButton = (ImageButton) v.findViewById(R.id.buttonEdit);

        }
    }

    public interface GameListener {
        void gameWon(String winner);

        void deletePlayer(int position);

        void editPlayer(int position);
    }
}
