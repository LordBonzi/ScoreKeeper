package io.github.sdsstudios.ScoreKeeper;

import android.widget.EditText;

/**
 * Created by seth on 18/09/16.
 */

public class EditTextOption {

    private EditText mEditText;
    private int mData;
    private String mDatabaseColumn;
    private String mHint;

    public EditTextOption(EditText mEditText, int mData, String mDatabaseColumn, String mHint) {
        this.mDatabaseColumn = mDatabaseColumn;
        this.mData = mData;
        this.mEditText = mEditText;
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

    public EditText getmEditText() {
        return mEditText;
    }

    public void setmEditText(EditText mEditText) {
        this.mEditText = mEditText;
    }

    public int getmData() {
        return mData;
    }

    public void setmData(int mData) {
        this.mData = mData;
    }
}
