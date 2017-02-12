package io.github.sdsstudios.ScoreKeeper.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import io.github.sdsstudios.ScoreKeeper.Game;
import io.github.sdsstudios.ScoreKeeper.Listeners.ButtonPlayerListener;
import io.github.sdsstudios.ScoreKeeper.Player;
import io.github.sdsstudios.ScoreKeeper.R;

/**
 * Created by seth on 08/05/16.
 */

public class BigGameAdapter extends RecyclerView.Adapter<BigGameAdapter.ViewHolder> {
    private String TAG = "BigGameAdapter";
    private Game mGame;
    private boolean mEnabled;
    private ButtonPlayerListener mButtonPlayerListener;
    private int mDefaultButtonColor;

    private int mWinningColor, mLosingColor;

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
        Context ctx = parent.getContext();

        mWinningColor = ctx.getResources().getColor(R.color.first_place);
        mLosingColor = ctx.getResources().getColor(R.color.last_place);

        return vh;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        TypedValue typedValue = new TypedValue();
        recyclerView.getContext().getTheme().resolveAttribute(R.attr.buttonBackground, typedValue, true);

        mDefaultButtonColor = typedValue.data;

    }

    private void setColor(int scorePosition, Button button) {
        switch (scorePosition) {
            case Game.WINNING:
                button.setBackgroundColor(mWinningColor);
                break;

            case Game.LOSING:
                button.setBackgroundColor(mLosingColor);
                break;

            case 0:
                Log.e(TAG, String.valueOf(mDefaultButtonColor));
                button.setBackgroundColor(mDefaultButtonColor);
                break;

        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        final Player player = mGame.getPlayer(holder.getAdapterPosition());

        holder.mTextViewPlayer.setText(player.getmName());
        holder.mButtonScore.setText(String.valueOf(player.getmScore()));

        if (mEnabled) {
            holder.mButtonScore.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    mGame.onPlayerClick(holder.getAdapterPosition());

                    holder.mButtonScore.setText(String.valueOf(player.getmScore()));

                    mGame.isGameWon();

                }
            });

            holder.mButtonScore.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    mGame.onPlayerLongClick(holder.getAdapterPosition());
                    holder.mButtonScore.setText(String.valueOf(player.getmScore()));

                    return true;
                }
            });

            mGame.setPlayer(player, position);

        } else {

            holder.mButtonScore.setEnabled(false);

        }

    }

    @Override
    public int getItemCount() {
        return mGame.size();
    }

    public interface ButtonColorListener {
        void updateColor(int scorePosition);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextViewPlayer;
        public Button mButtonScore;


        public ViewHolder(View v) {
            super(v);
            mTextViewPlayer = (TextView) v.findViewById(R.id.listTextViewPlayer);
            mButtonScore = (Button) v.findViewById(R.id.listButtonScore);

        }

    }
}
