package io.github.sdsstudios.ScoreKeeper;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

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
        CursorHelper cursorHelper = new CursorHelper();
        String w = null;
        String l = null;
        String d = null;
        ArrayList arrayListPlayer;
        ArrayList arrayListScore;
        ArrayList arrayListDate;

        ArrayList<GameModel> gameModelArrayList = new ArrayList<>();

        for (int i = 1; i <= numGames; i++) {

            arrayListPlayer = cursorHelper.getArrayById(ScoreDBAdapter.KEY_PLAYERS, i, dbHelper);
            arrayListScore = cursorHelper.getArrayById(ScoreDBAdapter.KEY_SCORE, i, dbHelper);
            arrayListDate = cursorHelper.getArrayById(ScoreDBAdapter.KEY_TIME, i, dbHelper);

            //TODO delete log tags in GameModel

            Object objWinner = Collections.max(arrayListScore);
            int winnerIndex = arrayListScore.indexOf(objWinner);

            Object objLoser = Collections.min(arrayListScore);
            int loserIndex = arrayListScore.indexOf(objLoser);

            d = String.valueOf(arrayListDate.get(2) + ":" + arrayListDate.get(1));

            w = String.valueOf(arrayListPlayer.get(winnerIndex));
            l = String.valueOf(arrayListPlayer.get(loserIndex));

            Log.i("GameModel", w + " , " + l + " , " + d);


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
