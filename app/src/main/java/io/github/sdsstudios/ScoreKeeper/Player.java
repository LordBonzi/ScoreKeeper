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

    public Player(String mName, int mScore) {
        this.mName = mName;
        this.mScore = mScore;
        this.mSetScores = new ArrayList<Integer>();
        //create a blank set.
        mSetScores.add(0,0);
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
        mSetScores.set(mSetScores.size() - 1, mScore);

    }

    public ArrayList<Integer> getmSetScores() {
        return mSetScores;
    }

    public void setmSetScores(ArrayList<Integer> mSetScores) {
        this.mSetScores = mSetScores;
    }

    public void fillSetArray(int numSets) {
        for (int i = 0; i < numSets - 1; i++) {
            addSet(0);
        }
    }

    public void addSet(int score){
        mSetScores.add(score);
    }

    public void deleteSet(int index){
        mSetScores.remove(index);

    }

    public int overallScore(){
        int overallScore = 0;

        for (int score : mSetScores){
            overallScore += score;
        }

        return overallScore;
    }
}
