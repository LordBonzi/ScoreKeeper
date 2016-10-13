package io.github.sdsstudios.ScoreKeeper;

/**
 * Created by Seth on 12/10/2016.
 */

public class Option {
    //DO NOT CHANGE VALUES. WILL CRASH THE APP IF CHANGED

    //EditText IDs only
    public static final int WINNING_SCORE = 0;
    public static final int SCORE_INTERVAL = 1;
    public static final int SCORE_DIFF_TO_WIN = 2;
    public static final int NUMBER_SETS = 3;
    //ADD NEW POINTERS HERE

    //Checkbox IDs only
    public static final int STOPWATCH = 4;
    public static final int REVERSE_SCORING = 5;
    //ADD NEW POINTERS HERE

    private int mID;
    private int mData;
    private int mType;

    public Option(int id, int data) {
        this.mID = id;
        this.mData = data;
    }

    public int getmID() {
        return mID;
    }

    public void setmID(int mID) {
        this.mID = mID;
    }

    public int getmData() {
        return mData;
    }

    public void setmData(int mData) {
        this.mData = mData;
    }

    public int getmType() {
        return mType;
    }

    public void setmType(int mType) {
        this.mType = mType;
    }
}
