package io.github.sdsstudios.ScoreKeeper;

import java.util.ArrayList;

/**
 * Created by Seth Schroeder on 22/05/2016.
 */

public class GameModel {
    private String mWinner;
    private String mLoser;
    private String mDate;

    public GameModel(String winner, String loser, String date) {
        mWinner = winner;
        mLoser = loser;
        mDate = date;
    }

    public static ArrayList<GameModel> createGameModel(int numGames, ScoreDBAdapter dbHelper) {
        String w = null;
        String l = null;
        String d = null;

        ArrayList<GameModel> gameModelArrayList = new ArrayList<>();

        for (int i = 1; i <= numGames; i++) {
            w = String.valueOf(i);
            l = String.valueOf(i);
            d = String.valueOf(i);

            gameModelArrayList.add(new GameModel(w , l , d));
        }
        return gameModelArrayList;
    }

    public String getWinner() {
        return mWinner;
    }

    public String getLoser() {
        return mLoser;
    }

    public String getDate() {
        return mDate;
    }

}
