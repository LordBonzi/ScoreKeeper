package io.github.sdsstudios.ScoreKeeper;

import android.app.Activity;
import android.widget.CheckBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by seth on 18/09/16.
 */

public class CheckBoxOption{

    private CheckBox mCheckBox;
    private String mHint;
    private int mID;

    public CheckBoxOption(CheckBox mEditText, String mHint, int mID) {
        this.mCheckBox = mEditText;
        this.mHint = mHint;
        this.mID = mID;
    }

    public static List<CheckBoxOption> loadCheckBoxOptions(Activity a){
        //MUST BE IN ORDER OF THE LIST OF POINTERS ABOVE!!!

        List<CheckBoxOption> mCheckBoxOptions = new ArrayList<>();

        mCheckBoxOptions.add(new CheckBoxOption((CheckBox) a.findViewById(R.id.checkBoxStopwatch), a.getString(R.string.stopwatch)
        , Option.STOPWATCH));

        mCheckBoxOptions.add(new CheckBoxOption((CheckBox) a.findViewById(R.id.checkBoxReverseScoring), a.getString(R.string.reverse_scoring)
        , Option.REVERSE_SCORING));

        return mCheckBoxOptions;
    }

    public int getmID() {
        return mID;
    }

    public void setmID(int mID) {
        this.mID = mID;
    }

    public String getmHint() {
        return mHint;
    }

    public void setmHint(String mHint) {
        this.mHint = mHint;
    }

    public CheckBox getmCheckBox() {
        return mCheckBox;
    }

    public void setmCheckBox(CheckBox mEditText) {
        this.mCheckBox = mEditText;
    }

    public void setChecked(boolean isChecked){
        mCheckBox.setChecked(isChecked);
    }

}
