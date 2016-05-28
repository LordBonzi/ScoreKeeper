package io.github.sdsstudios.ScoreKeeper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


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
        Calendar calendar = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm d/M/yyyy");
        String p, s ,d ,t = null;

        ArrayList arrayListPlayer;
        ArrayList arrayListScore;

        ArrayList<GameModel> gameModelArrayList = new ArrayList<>();

        for (int i = 1; i <= numGames; i++) {
            p = null;
            s = null;

            arrayListPlayer = cursorHelper.getArrayById(ScoreDBAdapter.KEY_PLAYERS, i, dbHelper);
            arrayListScore = cursorHelper.getArrayById(ScoreDBAdapter.KEY_SCORE, i, dbHelper);

            d = String.valueOf(i);

            if (arrayListPlayer.size() == 2){
                t = "2 Player Game";
                p = arrayListPlayer.get(0) + " vs " + arrayListPlayer.get(1);
                s = arrayListScore.get(0) + ":" + arrayListScore.get(1);

            }else if (arrayListPlayer.size() == 3){
                t = "3 Player Game";
                p = arrayListPlayer.get(0) + " vs " + arrayListPlayer.get(1);

                if (arrayListScore.size() != arrayListPlayer.size()){
                    s = arrayListScore.get(0) + ":" + arrayListScore.get(0);
                }else{
                    s = arrayListScore.get(0) + ":" + arrayListScore.get(1);

                }

            }else if (arrayListPlayer.size() > 3 && arrayListPlayer.size() < 10){
                t = "Group Game";
                p = arrayListPlayer.get(0) + " vs " + arrayListPlayer.get(1);
                s = arrayListScore.get(0) + ":" + arrayListScore.get(1);

            }else if (arrayListPlayer.size() > 10){
                t = "Big Group Game";
                p = arrayListPlayer.get(0) + " vs " + arrayListPlayer.get(1);
                s = arrayListScore.get(0) + ":" + arrayListScore.get(1);
            }else if (arrayListPlayer.size() == 1){
                t = "Game is too small. How did you make it this small. it is a bug.";
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
