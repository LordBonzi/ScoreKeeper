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
    private Option mOption;

    public CheckBoxOption(CheckBox mEditText, String mHint, Option o) {
        this.mCheckBox = mEditText;
        this.mHint = mHint;
        this.mOption = o;
    }

    public static List<CheckBoxOption> loadCheckBoxOptions(Activity a){
        //MUST BE IN ORDER OF THE LIST OF POINTERS ABOVE!!!

        List<CheckBoxOption> mCheckBoxOptions = new ArrayList<>();

        mCheckBoxOptions.add(new CheckBoxOption((CheckBox) a.findViewById(R.id.checkBoxStopwatch), a.getString(R.string.stopwatch)
                , new Option(Option.STOPWATCH, 0)));

        mCheckBoxOptions.add(new CheckBoxOption((CheckBox) a.findViewById(R.id.checkBoxReverseScoring), a.getString(R.string.reverse_scoring)
                , new Option(Option.REVERSE_SCORING, 0)));

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

    public boolean ismChecked() {
        if (mOption.getmData() == 0){
            return false;
        }else{
            return true;
        }
    }

    public void setmChecked(boolean mChecked) {
        if (!mChecked){
            mOption.setmData(0);
        }else{
            mOption.setmData(1);
        }
    }

    public Option getmOption() {
        return mOption;
    }

    public void setmOption(Option mOption) {
        this.mOption = mOption;
    }
}
