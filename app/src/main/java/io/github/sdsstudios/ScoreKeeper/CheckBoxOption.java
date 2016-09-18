package io.github.sdsstudios.ScoreKeeper;

import android.widget.CheckBox;

/**
 * Created by seth on 18/09/16.
 */

public class CheckBoxOption{

    private CheckBox mCheckBox;
    private int mData;
    private String mDatabaseColumn;
    private String mHint;

    public CheckBoxOption(CheckBox mEditText, int mData, String mDatabaseColumn, String mHint) {
        this.mDatabaseColumn = mDatabaseColumn;
        this.mData = mData;
        this.mCheckBox = mEditText;
        this.mHint = mHint;
    }

    public String getmHint() {
        return mHint;
    }

    public void setmHint(String mHint) {
        this.mHint = mHint;
    }

    public String getmDatabaseColumn() {
        return mDatabaseColumn;
    }

    public void setmDatabaseColumn(String mDatabaseColumn) {
        this.mDatabaseColumn = mDatabaseColumn;
    }

    public CheckBox getmCheckBox() {
        return mCheckBox;
    }

    public void setmCheckBox(CheckBox mEditText) {
        this.mCheckBox = mEditText;
    }

    public int getmData() {
        return mData;
    }

    public void setmData(int mData) {
        this.mData = mData;
    }
}
