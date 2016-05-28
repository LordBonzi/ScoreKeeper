package io.github.sdsstudios.ScoreKeeper;

import android.util.Log;

import java.util.ArrayList;


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

    public static ArrayList<GameModel> createGameModel(int numGames, ScoreDBAdapter dbHelper){
        CursorHelper cursorHelper = new CursorHelper();
        DateHelper dateHelper = new DateHelper();
        String p, s ,d ,t = null;

        ArrayList arrayListPlayer;
        ArrayList arrayListScore;
        String date;

        ArrayList<GameModel> gameModelArrayList = new ArrayList<>();

        for (int i = 1; i <= numGames; i++) {
            p = null;
            s = null;

            arrayListPlayer = cursorHelper.getArrayById(ScoreDBAdapter.KEY_PLAYERS, i, dbHelper);
            arrayListScore = cursorHelper.getArrayById(ScoreDBAdapter.KEY_SCORE, i, dbHelper);
            date = cursorHelper.getTimeById( i, dbHelper);

            Log.i("Gamemodel", String.valueOf(date));

            d = dateHelper.gameDate(date);

            if (arrayListPlayer.size() == 2){
                t = "2 Player Game";
                p = arrayListPlayer.get(0) + " vs " + arrayListPlayer.get(1);
                s = arrayListScore.get(0) + ":" + arrayListScore.get(1);

            }else if (arrayListPlayer.size() == 3){
                t = "3 Player Game";
                p = arrayListPlayer.get(0) + " vs " + arrayListPlayer.get(1) + " vs " + arrayListPlayer.get(2);

            }else if (arrayListPlayer.size() > 3 && arrayListPlayer.size() < 10){
                t = "Group Game";
                p = arrayListPlayer.get(0) + " vs " + arrayListPlayer.get(1);
                s = arrayListScore.get(0) + ":" + arrayListScore.get(1);

            }else if (arrayListPlayer.size() > 10){
                t = "Huge Game";
                p = arrayListPlayer.get(0) + " vs " + arrayListPlayer.get(1);
                s = arrayListScore.get(0) + ":" + arrayListScore.get(1);

            }else if (arrayListPlayer.size() == 1){
                t = "Game is too small. How did you make it this small. it is a bug. you must report it.";
                p = String.valueOf(arrayListPlayer.get(0));
                s = String.valueOf(arrayListScore.get(0));
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
