package io.github.sdsstudios.ScoreKeeper;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by seth on 18/09/16.
 */

public class CheckBoxOption extends Option{

    //DO NOT CHANGE VALUES. WILL CRASH THE APP IF CHANGED

    //Checkbox IDs only
    public static final int STOPWATCH = 0;
    public static final int REVERSE_SCORING = 1;
    //ADD NEW POINTERS HERE

    private int mCheckBoxID;
    private String mHint;

    public CheckBoxOption(int mCheckBoxID, String mHint, int id, boolean data) {
        super(id, data);
        this.mCheckBoxID = mCheckBoxID;
        this.mHint = mHint;
    }

    public static List<CheckBoxOption> loadCheckBoxOptions(Activity a){
        //MUST BE IN ORDER OF THE LIST OF POINTERS ABOVE!!!

        List<CheckBoxOption> mCheckBoxOptions = new ArrayList<>();

        mCheckBoxOptions.add(new CheckBoxOption(R.id.checkBoxStopwatch, a.getString(R.string.stopwatch)
        , CheckBoxOption.STOPWATCH, false));

        mCheckBoxOptions.add(new CheckBoxOption(R.id.checkBoxReverseScoring, a.getString(R.string.reverse_scoring)
        , CheckBoxOption.REVERSE_SCORING, false));

        return mCheckBoxOptions;
    }

    public String getmHint() {
        return mHint;
    }

    public void setmHint(String mHint) {
        this.mHint = mHint;
    }

    public int getmCheckBoxID() {
        return mCheckBoxID;
    }

    public void setmCheckBoxID(int mCheckBoxID) {
        this.mCheckBoxID = mCheckBoxID;
    }
}
