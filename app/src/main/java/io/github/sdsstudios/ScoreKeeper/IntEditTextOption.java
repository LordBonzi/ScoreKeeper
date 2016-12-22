package io.github.sdsstudios.ScoreKeeper;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by seth on 18/09/16.
 */

public class IntEditTextOption extends EditTextOption{

    public IntEditTextOption(String mHint, OptionID id, int data) {
        super(id, data, mHint);
    }

    public static List<IntEditTextOption> loadEditTextOptions(Activity a){
        //MUST BE IN ORDER OF THE LIST OF POINTERS ABOVE!!!

        List<IntEditTextOption> mIntEditTextOptions = new ArrayList<>();

        mIntEditTextOptions.add(new IntEditTextOption(a.getString(R.string.max_score)
                , OptionID.WINNING_SCORE, 0));

        mIntEditTextOptions.add(new IntEditTextOption(a.getString(R.string.score_interval)
                , OptionID.SCORE_INTERVAL, 1));

        mIntEditTextOptions.add(new IntEditTextOption(a.getString(R.string.diff_to_win)
                , OptionID.SCORE_DIFF_TO_WIN, 0));

        mIntEditTextOptions.add(new IntEditTextOption(a.getString(R.string.num_sets)
                , OptionID.NUMBER_SETS, 1));

        mIntEditTextOptions.add(new IntEditTextOption(a.getString(R.string.starting_score)
                , OptionID.STARTING_SCORE, 0));

        mIntEditTextOptions.add(new IntEditTextOption(a.getString(R.string.dice_minimum)
                , OptionID.DICE_MIN, 1));

        mIntEditTextOptions.add(new IntEditTextOption(a.getString(R.string.dice_maximum)
                , OptionID.DICE_MAX, 6));

        return mIntEditTextOptions;
    }

}
