package io.github.sdsstudios.ScoreKeeper.Listeners;

/**
 * Created by seth on 13/01/17.
 */
public interface GameListener {
    void onGameWon(String winner);

    void deletePlayer(int position);

    void editPlayer(int position);

}
