package io.github.sdsstudios.ScoreKeeper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by seth on 18/09/16.
 */

public class CheckBoxOption extends Option{

    public CheckBoxOption(OptionID id, boolean data, String hint) {
        super(id, data, hint);
    }

    public static List<CheckBoxOption> loadCheckBoxOptions(android.app.Activity activity) {
        //MUST BE IN ORDER OF THE LIST OF POINTERS ABOVE!!!

        List<CheckBoxOption> mCheckBoxOptions = new ArrayList<>();

        mCheckBoxOptions.add(new CheckBoxOption(OptionID.STOPWATCH, false, activity.getString(R.string.stopwatch)));
        mCheckBoxOptions.add(new CheckBoxOption(OptionID.REVERSE_SCORING, false, activity.getString(R.string.reverse_scoring)));

        return mCheckBoxOptions;
    }
}
