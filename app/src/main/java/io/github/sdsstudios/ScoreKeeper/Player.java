package io.github.sdsstudios.ScoreKeeper;

import java.util.ArrayList;

/**
 * Created by seth on 21/09/16.
 */

public class Player {
    private String mName;
    private ArrayList<Integer> mSetScores;
    //TODO add image for player

    public Player(String mName, int mScore) {
        this.mName = mName;
        this.mSetScores = new ArrayList<Integer>();
        //create a blank set.
        mSetScores.add(mScore);
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public int getmScore() {
        return mSetScores.get(mSetScores.size() - 1);
    }

    public void setmScore(int mScore) {
        mSetScores.set(mSetScores.size() - 1, mScore);

    }

    public ArrayList<Integer> getmSetScores() {
        return mSetScores;
    }

    public void setmSetScores(ArrayList<Integer> mSetScores) {
        this.mSetScores = mSetScores;
    }

    void changeSetScore(int position, int score) {
        mSetScores.set(position, score);
    }

    public void addSetAtPosition(int index, int score) {
        mSetScores.add(index, score);
    }

    public void addSet(int score){
        mSetScores.add(score);
    }

    public void deleteSet(int index){
        mSetScores.remove(index);

    }

    public int getNumSetsPlayed() {
        return mSetScores.size();
    }

    public int overallScore(){
        int overallScore = 0;

        for (int score : mSetScores){
            overallScore += score;
        }

        return overallScore;
    }
}
