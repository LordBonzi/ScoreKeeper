package io.github.sdsstudios.ScoreKeeper.Listeners;

/**
 * Created by seth on 13/01/17.
 */

public interface ButtonPlayerListener {
    void onScoreClick(int playerIndex);

    void onScoreLongClick(int playerIndex);

    void editPlayer(int playerIndex);

    void deletePlayer(int playerIndex);
}
