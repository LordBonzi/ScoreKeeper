package io.github.sdsstudios.ScoreKeeper;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by seth on 18/09/16.
 */

public class IntEditTextOption extends EditTextOption{

    private String mHint;
    private int mDefaultValue;

    public IntEditTextOption(int editTextID, String mHint, int id, int data, int mDefaultValue) {
        super(editTextID, id, data);

        this.mHint = mHint;
        this.mDefaultValue = mDefaultValue;
    }

    public static List<IntEditTextOption> loadEditTextOptions(Activity a){
        //MUST BE IN ORDER OF THE LIST OF POINTERS ABOVE!!!

        List<IntEditTextOption> mIntEditTextOptions = new ArrayList<>();

        mIntEditTextOptions.add(new IntEditTextOption(R.id.editTextMaxScore, a.getString(R.string.max_score)
                , WINNING_SCORE, 0, 0));

        mIntEditTextOptions.add(new IntEditTextOption(R.id.editTextScoreInterval, a.getString(R.string.score_interval)
                , SCORE_INTERVAL, 1, 1));

        mIntEditTextOptions.add(new IntEditTextOption(R.id.editTextDiffToWin, a.getString(R.string.diff_to_win)
                , SCORE_DIFF_TO_WIN, 0, 0));

        mIntEditTextOptions.add(new IntEditTextOption(R.id.editTextNumSets, a.getString(R.string.num_sets)
                , NUMBER_SETS, 1, 1));

        mIntEditTextOptions.add(new IntEditTextOption(R.id.editTextStartingScore, a.getString(R.string.starting_score)
                , STARTING_SCORE, 0, 0));

        mIntEditTextOptions.add(new IntEditTextOption(R.id.editTextDiceMin, a.getString(R.string.dice_minimum)
                , DICE_MIN, 1, 1));

        mIntEditTextOptions.add(new IntEditTextOption(R.id.editTextDiceMax, a.getString(R.string.dice_maximum)
                , DICE_MAX, 6, 6));

        return mIntEditTextOptions;
    }

    public int getmDefaultValue() {
        return mDefaultValue;
    }

    public void setmDefaultValue(int mDefaultValue) {
        this.mDefaultValue = mDefaultValue;
    }

    public String getmHint() {
        return mHint;
    }

    public void setmHint(String mHint) {
        this.mHint = mHint;
    }


}
