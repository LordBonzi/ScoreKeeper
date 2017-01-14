package io.github.sdsstudios.ScoreKeeper.Listeners;

/**
 * Created by seth on 13/01/17.
 */

public interface ButtonPlayerListener {
    void onScoreChange(int playerIndex, int score);

    void changePlayerName(int playerIndex);
}
