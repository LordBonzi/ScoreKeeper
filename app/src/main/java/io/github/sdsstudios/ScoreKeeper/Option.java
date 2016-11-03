package io.github.sdsstudios.ScoreKeeper;

/**
 * Created by Seth on 12/10/2016.
 */

public class Option {

    private int mID;
    private int mData;

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
