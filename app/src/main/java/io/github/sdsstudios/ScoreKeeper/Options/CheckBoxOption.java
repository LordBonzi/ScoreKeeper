package io.github.sdsstudios.ScoreKeeper.Options;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import io.github.sdsstudios.ScoreKeeper.R;

/**
 * Created by seth on 18/09/16.
 */

public class CheckBoxOption extends Option{

    private int mCheckBoxID;

    public CheckBoxOption(int mCheckBoxID, String id, boolean data, String hint) {
        super(id, data, hint);
        this.mCheckBoxID = mCheckBoxID;
    }

    public static List<CheckBoxOption> loadCheckBoxOptions(Context ctx) {

        List<CheckBoxOption> mCheckBoxOptions = new ArrayList<>();

        mCheckBoxOptions.add(new CheckBoxOption(R.id.checkBoxStopwatch, Option.STOPWATCH, false, ctx.getString(R.string.stopwatch)));
        mCheckBoxOptions.add(new CheckBoxOption(R.id.checkBoxReverseScoring, Option.REVERSE_SCORING, false, ctx.getString(R.string.reverse_scoring)));

        return mCheckBoxOptions;
    }

    public int getmCheckBoxID() {
        return mCheckBoxID;
    }

    public void setmCheckBoxID(int mCheckBoxID) {
        this.mCheckBoxID = mCheckBoxID;
    }
}
