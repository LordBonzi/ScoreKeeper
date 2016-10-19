package io.github.sdsstudios.ScoreKeeper;

/**
 * Created by Seth on 12/10/2016.
 */

public class Option {
    //DO NOT CHANGE VALUES. WILL CRASH THE APP IF CHANGED

    //Pointers for different view types
    public static final int EDIT_TEXT = 1;
    public static final int CHECK_BOX = 2;

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

    public static final int[] EDIT_TEXT_IDS = {WINNING_SCORE, SCORE_INTERVAL, SCORE_DIFF_TO_WIN, NUMBER_SETS};
    public static final int[] CHECK_BOX_IDS = {STOPWATCH, REVERSE_SCORING};

    private int mID;
    private int mData;
    private int mViewType;

    public Option(int id, int data, int view) {
        this.mID = id;
        this.mData = data;
        this.mViewType = view;
    }

    public int getmID() {
        if (mViewType == EDIT_TEXT){
            return mID;
        }else{
            return mID - EDIT_TEXT_IDS.length;
        }
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

    public int getmViewType() {
        return mViewType;
    }

    public void setmViewType(int mViewType) {
        this.mViewType = mViewType;
    }

    public boolean isChecked(){
        return mData != 0;
    }

    public void setChecked(boolean checked){
        if (checked){
            mData = 1;
        }else{
            mData = 0;
        }
    }
}
