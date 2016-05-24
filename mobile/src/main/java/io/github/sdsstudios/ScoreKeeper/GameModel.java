package io.github.sdsstudios.ScoreKeeper;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Seth Schroeder on 22/05/2016.
 */

public class GameModel {
    private String mPlayers;
    private String mScore;
    private String mType;
    private String mDate;

    public GameModel(String players, String score, String date, String type) {
        mPlayers = players;
        mScore = score;
        mDate = date;
        mType = type;
    }

    public static ArrayList<GameModel> createGameModel(int numGames, ScoreDBAdapter dbHelper) {
        CursorHelper cursorHelper = new CursorHelper();
        String p = null;
        String s = null;
        String d = null;
        String t = null;
        ArrayList arrayListPlayer;
        ArrayList arrayListScore;
        ArrayList arrayListDate;

        ArrayList<GameModel> gameModelArrayList = new ArrayList<>();

        for (int i = 1; i <= numGames; i++) {

            arrayListPlayer = cursorHelper.getArrayById(ScoreDBAdapter.KEY_PLAYERS, i, dbHelper);
            arrayListScore = cursorHelper.getArrayById(ScoreDBAdapter.KEY_SCORE, i, dbHelper);
            arrayListDate = cursorHelper.getArrayById(ScoreDBAdapter.KEY_TIME, i, dbHelper);

            Object objWinner = Collections.max(arrayListScore);
            int winnerIndex = arrayListScore.indexOf(objWinner);

            Object objLoser = Collections.min(arrayListScore);
            int loserIndex = arrayListScore.indexOf(objLoser);

            d = String.valueOf(arrayListDate.get(2) + ":" + arrayListDate.get(1));

            p = String.valueOf(arrayListPlayer.get(winnerIndex));
            s = String.valueOf(arrayListPlayer.get(loserIndex));

            if (arrayListPlayer.size() == 2){

            }else if (arrayListPlayer.size() == 3){

            }else if (arrayListPlayer.size() > 3 && arrayListPlayer.size() < 10){

            }else if (arrayListPlayer.size() > 10){

            }

            gameModelArrayList.add(new GameModel(p , s , d, t));
        }
        return gameModelArrayList;
    }

    public String getPlayers() {
        return mPlayers;
    }



    public String getScore() {
        return mScore;
    }

    public String getDate() {
        return mDate;
    }

    public String getType() {
        return mType;
    }

}
