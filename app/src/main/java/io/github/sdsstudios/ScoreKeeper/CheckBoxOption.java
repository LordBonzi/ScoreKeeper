package io.github.sdsstudios.ScoreKeeper;

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

    public CheckBoxOption(int mCheckBoxID, int id, boolean data) {
        super(id, data);
        this.mCheckBoxID = mCheckBoxID;
    }

    public static List<CheckBoxOption> loadCheckBoxOptions() {
        //MUST BE IN ORDER OF THE LIST OF POINTERS ABOVE!!!

        List<CheckBoxOption> mCheckBoxOptions = new ArrayList<>();

        mCheckBoxOptions.add(new CheckBoxOption(R.id.checkBoxStopwatch,
                CheckBoxOption.STOPWATCH, false));

        mCheckBoxOptions.add(new CheckBoxOption(R.id.checkBoxReverseScoring,
                CheckBoxOption.REVERSE_SCORING, false));

        return mCheckBoxOptions;
    }

    public int getmCheckBoxID() {
        return mCheckBoxID;
    }

    public void setmCheckBoxID(int mCheckBoxID) {
        this.mCheckBoxID = mCheckBoxID;
    }
}
