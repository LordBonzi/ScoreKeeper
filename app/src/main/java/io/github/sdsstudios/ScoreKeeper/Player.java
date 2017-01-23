package io.github.sdsstudios.ScoreKeeper;

import android.util.Log;

import java.util.ArrayList;

import io.github.sdsstudios.ScoreKeeper.Adapters.BigGameAdapter;

/**
 * Created by seth on 21/09/16.
 */

public class Player {
    private final String TAG = "Player";

    private String mName;
    private ArrayList<Integer> mSetScores;
    private BigGameAdapter.ButtonColorListener mButtonColorListener;
    //TODO add image for player

    public Player(String mName, int mScore) {
        this.mName = mName;
        this.mSetScores = new ArrayList<>();

        /**create a blank set. when creating a new Player**/
        mSetScores.add(mScore);
    }

    public BigGameAdapter.ButtonColorListener getmButtonColorListener() {
        return mButtonColorListener;
    }

    public void setmButtonColorListener(BigGameAdapter.ButtonColorListener mButtonColorListener) {
        if (mButtonColorListener != null) {
            this.mButtonColorListener = mButtonColorListener;
            Log.e(TAG, "setbuttonClicklistener");
        }
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

    public void playerClick(int scoreInterval, boolean reverseScoring) {

        if (reverseScoring) {
            setmScore(getmScore() - scoreInterval);
        } else {
            setmScore(getmScore() + scoreInterval);
        }

    }

    public void playerLongClick(int scoreInterval, boolean reverseScoring) {
        if (reverseScoring) {
            setmScore(getmScore() + scoreInterval);
        } else {
            setmScore(getmScore() - scoreInterval);
        }
    }

    public ArrayList<Integer> getmSetScores() {
        return mSetScores;
    }

    public void setmSetScores(ArrayList<Integer> mSetScores) {
        this.mSetScores = mSetScores;
    }

    public void changeSetScore(int position, int score) {
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
