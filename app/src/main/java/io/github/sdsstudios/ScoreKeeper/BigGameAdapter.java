package io.github.sdsstudios.ScoreKeeper;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by seth on 08/05/16.
 */
public class BigGameAdapter extends RecyclerView.Adapter<BigGameAdapter.ViewHolder> {
    private GameDBAdapter mDbHelper;
    private Game mGame;
    private boolean mEnabled;
    private boolean mReverseScoring;
    private int mScoreInterval;
    private Game.GameListener mGameListener;

    // Provide a suitable constructor (depends on the kind of dataset)
    public BigGameAdapter(Game mGame, GameDBAdapter dbAdapter, boolean menabled, Game.GameListener mGameListener) {

        mDbHelper = dbAdapter;
        mEnabled = menabled;
        this.mGame = mGame;
        this.mGameListener = mGameListener;

        mReverseScoring = mGame.isChecked(Option.OptionID.REVERSE_SCORING);
        mScoreInterval = mGame.getInt(Option.OptionID.SCORE_INTERVAL);

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

        if (mEnabled) {
            holder.buttonScore.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    int score = 0;
                    int buttonScore = 0;

                    buttonScore = Integer.valueOf(holder.buttonScore.getText().toString());

                    if (mReverseScoring) {
                        score = buttonScore -= mScoreInterval;
                    } else {
                        score = buttonScore += mScoreInterval;
                    }

                    holder.buttonScore.setText(String.valueOf(score));
                    p.setmScore(score);

                    mGame.setGameListener(mGameListener);
                    mGame.isGameWon();

                }
            });

            holder.buttonScore.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int score = 0;
                    int buttonScore = 0;

                    buttonScore = Integer.valueOf(holder.buttonScore.getText().toString());

                    if (mReverseScoring) {
                        score = buttonScore += mScoreInterval;
                    } else {
                        score = buttonScore -= mScoreInterval;
                    }
                    if (score == -1) {

                    } else {
                        holder.buttonScore.setText(String.valueOf(score));
                        p.setmScore(score);
                    }

                    return true;
                }
            });

            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mGameListener.deletePlayer(position);
                }
            });

            holder.editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mGameListener.editPlayer(position);
                }
            });

            mGame.setPlayer(p, position);
            mDbHelper.updateGame(mGame);

        } else {

            holder.buttonScore.setEnabled(false);

        }

    }

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
        public ImageButton deleteButton, editButton;


        public ViewHolder(View v) {
            super(v);
            textViewPlayer = (TextView) v.findViewById(R.id.listTextViewPlayer);
            buttonScore = (Button) v.findViewById(R.id.listButtonScore);
            deleteButton = (ImageButton) v.findViewById(R.id.buttonDelete);
            editButton = (ImageButton) v.findViewById(R.id.buttonEdit);

        }
    }
}
