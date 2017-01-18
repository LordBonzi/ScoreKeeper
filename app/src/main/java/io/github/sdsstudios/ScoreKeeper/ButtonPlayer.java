package io.github.sdsstudios.ScoreKeeper;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.github.sdsstudios.ScoreKeeper.Activity.ScoreKeeperActivity;
import io.github.sdsstudios.ScoreKeeper.Listeners.ButtonPlayerListener;
import io.github.sdsstudios.ScoreKeeper.Options.Option;

/**
 * Created by seth on 07/01/17.
 */

public class ButtonPlayer implements View.OnClickListener, View.OnLongClickListener {
    private boolean mReverseScoring;
    private int mScoreInterval;

    private Button mButton;
    private TextView mTextView;
    private int mScore;
    private String mPlayerName;
    private ButtonPlayerListener mButtonPlayerListener;
    private int mPlayerIndex;

    public ButtonPlayer(boolean mReverseScoring
            , int mScoreInterval, Player player
            , TextView mTextView, Button mButton
            , ButtonPlayerListener mButtonPlayerListener) {

        this.mReverseScoring = mReverseScoring;
        this.mScoreInterval = mScoreInterval;
        this.mTextView = mTextView;
        this.mButton = mButton;
        this.mButtonPlayerListener = mButtonPlayerListener;

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
                game.isChecked(Option.OptionID.REVERSE_SCORING)
                , game.getInt(Option.OptionID.SCORE_INTERVAL)
                , game.getmPlayerArray().get(0)
                , (TextView) ctx.findViewById(R.id.textViewP1)
                , (Button) ctx.findViewById(R.id.buttonP1)
                , buttonPlayerListener));

        list.add(new ButtonPlayer(
                game.isChecked(Option.OptionID.REVERSE_SCORING)
                , game.getInt(Option.OptionID.SCORE_INTERVAL)
                , game.getmPlayerArray().get(1)
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
        mScore = player.getmScore();
        mPlayerName = player.getmName();

        setButtonText();
        setTextViewText();
    }

    private void setTextViewText() {
        mTextView.setText(mPlayerName);
    }

    public void onScoreButtonClick() {

        if (mReverseScoring) {
            mScore -= mScoreInterval;
        } else {
            mScore += mScoreInterval;
        }

        setButtonText();

        mButtonPlayerListener.onScoreChange(mPlayerIndex, mScore);

    }

    public void onScoreButtonLongClick() {

        if (mReverseScoring) {
            mScore += mScoreInterval;
        } else {
            mScore -= mScoreInterval;
        }

        setButtonText();

        mButtonPlayerListener.onScoreChange(mPlayerIndex, mScore);
    }

    private void setButtonText() {
        mButton.setText(String.valueOf(mScore));
    }

    public void startNewSet() {
        mScore = 0;
        setButtonText();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mButton.getId()) {
            onScoreButtonClick();
        } else {
            mButtonPlayerListener.changePlayerName(mPlayerIndex);
        }
    }

    @Override
    public boolean onLongClick(View v) {

        onScoreButtonLongClick();

        return true;
    }
}
