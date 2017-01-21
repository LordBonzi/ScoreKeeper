package io.github.sdsstudios.ScoreKeeper;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.github.sdsstudios.ScoreKeeper.Activity.ScoreKeeperActivity;
import io.github.sdsstudios.ScoreKeeper.Listeners.ButtonPlayerListener;

/**
 * Created by seth on 07/01/17.
 */

public class ButtonPlayer implements View.OnClickListener, View.OnLongClickListener {

    private Button mButton;
    private TextView mTextView;
    private Player mPlayer;
    private ButtonPlayerListener mButtonPlayerListener;
    private int mPlayerIndex;

    public ButtonPlayer(Player player
            , TextView mTextView, Button mButton
            , ButtonPlayerListener mButtonPlayerListener) {

        this.mTextView = mTextView;
        this.mButton = mButton;
        this.mButtonPlayerListener = mButtonPlayerListener;
        this.mPlayer = player;

        if (mButton.getId() == R.id.buttonP1) {
            mPlayerIndex = 0;
        } else {
            mPlayerIndex = 1;
        }

        setOnClickListener(this);
        setOnLongClickListener(this);

        reload(player);
    }

    public static List<ButtonPlayer> createButtonPlayerList(Game game
            , ScoreKeeperActivity ctx, ButtonPlayerListener buttonPlayerListener) {

        List<ButtonPlayer> list = new ArrayList<>();

        list.add(new ButtonPlayer(
                game.getmPlayerArray().get(0)
                , (TextView) ctx.findViewById(R.id.textViewP1)
                , (Button) ctx.findViewById(R.id.buttonP1)
                , buttonPlayerListener));

        list.add(new ButtonPlayer(
                game.getmPlayerArray().get(1)
                , (TextView) ctx.findViewById(R.id.textViewP2)
                , (Button) ctx.findViewById(R.id.buttonP2)
                , buttonPlayerListener));

        return list;
    }

    public void setOnClickListener(View.OnClickListener clickListener) {
        mButton.setOnClickListener(clickListener);
        mTextView.setOnClickListener(clickListener);
    }

    public void setOnLongClickListener(View.OnLongClickListener clickListener) {
        mButton.setOnLongClickListener(clickListener);
    }

    public Button getmButton() {
        return mButton;
    }

    public void reload(Player player) {
        this.mPlayer = player;

        setButtonText();
        setTextViewText();
    }

    private void setTextViewText() {
        mTextView.setText(mPlayer.getmName());
    }

    public void onScoreButtonClick() {
        mButtonPlayerListener.onScoreClick(mPlayerIndex);
        setButtonText();

    }

    public void onScoreButtonLongClick() {

        mButtonPlayerListener.onScoreLongClick(mPlayerIndex);
        setButtonText();

    }

    private void setButtonText() {
        mButton.setText(String.valueOf(mPlayer.getmScore()));
    }

    public void startNewSet() {
        setButtonText();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mButton.getId()) {
            onScoreButtonClick();
        } else {
            mButtonPlayerListener.editPlayer(mPlayerIndex);
        }
    }

    @Override
    public boolean onLongClick(View v) {

        onScoreButtonLongClick();

        return true;
    }
}
