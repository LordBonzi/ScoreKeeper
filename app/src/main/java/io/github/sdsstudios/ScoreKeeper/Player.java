package io.github.sdsstudios.ScoreKeeper;

import java.util.ArrayList;

/**
 * Created by seth on 21/09/16.
 */

public class Player {
    private String mName;
    private int mScore;
    private ArrayList<Integer> mSetScores;
    //TODO add image for player

    public Player(String mName, int mScore, ArrayList<Integer> mSetScores) {
        this.mName = mName;
        this.mScore = mScore;
        this.mSetScores = mSetScores;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public int getmScore() {
        return mScore;
    }

    public void setmScore(int mScore) {
        this.mScore = mScore;
    }

    public ArrayList<Integer> getmSetScores() {
        return mSetScores;
    }

    public void setScoreForSet(int index, int score){
        mSetScores.set(index, score);
    }

    public void addSet(int score){
        mSetScores.add(score);
    }

    public void deleteSet(int index){
        mSetScores.remove(index);

    }

    public void setmSetScores(ArrayList<Integer> mSetScores) {
        this.mSetScores = mSetScores;
    }
}
