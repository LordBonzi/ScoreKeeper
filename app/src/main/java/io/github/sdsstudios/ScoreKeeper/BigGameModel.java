package io.github.sdsstudios.ScoreKeeper;

import java.util.ArrayList;


/**
 * Created by Seth Schroeder on 22/05/2016.
 */

public class BigGameModel {
    private String mPlayer;
    private String mScore;
    private static ScoreDBAdapter dbHelper;

    public void closeDB(){
        dbHelper.close();
    }

    public BigGameModel(ScoreDBAdapter scoreDBAdapter){
        this (null , null, scoreDBAdapter);
    }

    public BigGameModel(String players, String score, ScoreDBAdapter dbAdapter) {
        mPlayer = players;
        mScore = score;
        dbHelper = dbAdapter;

    }

    public static ArrayList<BigGameModel> createGameModel(int numPlayers, ArrayList pArray, ArrayList sArray){
        String p, s = null;

        ArrayList<BigGameModel> gameModelArrayList = new ArrayList<>();

        for (int i = 0; i < numPlayers; i++) {
            p = pArray.get(i).toString();
            s = sArray.get(i).toString();

            gameModelArrayList.add(new BigGameModel(p , s, dbHelper));
        }
        return gameModelArrayList;
    }

    public String getPlayers() {
        return mPlayer;
    }

    public String getScore() {
        return mScore;
    }

    public String setScore(int score) {
        return mScore;
    }


}
