package io.github.sdsstudios.ScoreKeeper;

import java.util.ArrayList;


/**
 * Created by Seth Schroeder on 22/05/2016.
 */

public class BigGameModel {
    private String mPlayer;
    private String mScore;

    public BigGameModel(String players, String score) {
        mPlayer = players;
        mScore = score;

    }

    public static ArrayList<BigGameModel> createGameModel(int numPlayers, ArrayList pArray, ArrayList sArray, ScoreDBAdapter dbHelper){
        String p, s = null;

        ArrayList<BigGameModel> gameModelArrayList = new ArrayList<>();

        for (int i = 0; i <= numPlayers-1; i++) {
            p = pArray.get(i).toString();
            s = sArray.get(i).toString();

            gameModelArrayList.add(new BigGameModel(p , s));
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
