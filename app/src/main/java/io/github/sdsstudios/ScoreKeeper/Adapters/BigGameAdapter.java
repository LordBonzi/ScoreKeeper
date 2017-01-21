package io.github.sdsstudios.ScoreKeeper.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import io.github.sdsstudios.ScoreKeeper.Game;
import io.github.sdsstudios.ScoreKeeper.Listeners.ButtonPlayerListener;
import io.github.sdsstudios.ScoreKeeper.Player;
import io.github.sdsstudios.ScoreKeeper.R;

/**
 * Created by seth on 08/05/16.
 */
public class BigGameAdapter extends RecyclerView.Adapter<BigGameAdapter.ViewHolder> {
    private Game mGame;
    private boolean mEnabled;
    private ButtonPlayerListener mButtonPlayerListener;

    public BigGameAdapter(Game mGame, boolean mEnabled, ButtonPlayerListener mButtonPlayerListener) {

        this.mEnabled = mEnabled;
        this.mGame = mGame;
        this.mButtonPlayerListener = mButtonPlayerListener;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.big_game_adapter, parent, false);

        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        final Player player = mGame.getPlayer(position);

        holder.textViewPlayer.setText(player.getmName());
        holder.buttonScore.setText(String.valueOf(player.getmScore()));

        if (mEnabled) {
            holder.buttonScore.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    mGame.onPlayerClick(position);

                    int score = player.getmScore();

                    holder.buttonScore.setText(String.valueOf(score));
                    player.setmScore(score);

                    mGame.isGameWon();

                }
            });

            holder.buttonScore.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    mGame.onPlayerLongClick(position);

                    int score = player.getmScore();

                    holder.buttonScore.setText(String.valueOf(score));
                    player.setmScore(score);

                    return true;
                }
            });

            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mButtonPlayerListener.deletePlayer(position);
                }
            });

            holder.editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mButtonPlayerListener.editPlayer(position);
                }
            });

            mGame.setPlayer(player, position);

        } else {

            holder.buttonScore.setEnabled(false);

        }

    }

    private void colorButton(Button button, Player player) {
        switch (mGame.scorePosition(player.getmScore())) {

        }
        ;
    }

    @Override
    public int getItemCount() {
        return mGame.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
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
