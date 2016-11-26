package io.github.sdsstudios.ScoreKeeper;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by seth on 18/09/16.
 */

public class IntEditTextOption extends EditTextOption{

    private String mHint;

    public IntEditTextOption(int editTextID, String mHint, int id, int data) {
        super(editTextID, id, data);

        this.mHint = mHint;
    }

    public static List<IntEditTextOption> loadEditTextOptions(Activity a){
        //MUST BE IN ORDER OF THE LIST OF POINTERS ABOVE!!!

        List<IntEditTextOption> mIntEditTextOptions = new ArrayList<>();

        mIntEditTextOptions.add(new IntEditTextOption(R.id.editTextMaxScore, a.getString(R.string.max_score)
                , IntEditTextOption.WINNING_SCORE, 0));

        mIntEditTextOptions.add(new IntEditTextOption(R.id.editTextScoreInterval, a.getString(R.string.score_interval)
                , IntEditTextOption.SCORE_INTERVAL, 1));

        mIntEditTextOptions.add(new IntEditTextOption(R.id.editTextDiffToWin, a.getString(R.string.diff_to_win)
                , IntEditTextOption.SCORE_DIFF_TO_WIN, 0));

        mIntEditTextOptions.add(new IntEditTextOption(R.id.editTextNumSets, a.getString(R.string.num_sets)
                , IntEditTextOption.NUMBER_SETS, 1));

        mIntEditTextOptions.add(new IntEditTextOption(R.id.editTextStartingScore, a.getString(R.string.starting_score)
                , IntEditTextOption.STARTING_SCORE, 0));

        return mIntEditTextOptions;
    }


    public String getmHint() {
        return mHint;
    }

    public void setmHint(String mHint) {
        this.mHint = mHint;
    }


}
