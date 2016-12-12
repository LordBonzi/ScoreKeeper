package io.github.sdsstudios.ScoreKeeper;

/**
 * Created by Seth on 12/10/2016.
 */

public class Option {

    private int mID;
    private int mIntData;
    private String mStringData;
    private boolean mBoolData;

    public Option(int id, int data) {
        this.mID = id;
        this.mIntData = data;
    }

    public Option(int id, String data) {
        this.mID = id;
        this.mStringData = data;
    }

    public Option(int id, boolean data) {
        this.mID = id;
        this.mBoolData = data;
    }

    public int getmID() {
        return mID;
    }

    public void setmID(int mID) {
        this.mID = mID;
    }

    public boolean getBoolean(){
        return mBoolData;
    }

    public int getInt(){
        return mIntData;
    }

    public void setInt(int data) {
        mIntData = data;
    }

    public String getString(){
        return (String) mStringData;
    }

    public void setString(String data) {
        mStringData = data;
    }

    public boolean isChecked(){
        return mBoolData;
    }

    public void setChecked(boolean checked){
        mBoolData = checked;
    }
}
