package io.github.sdsstudios.ScoreKeeper.Options;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import io.github.sdsstudios.ScoreKeeper.R;

/**
 * Created by seth on 18/09/16.
 */

public class IntEditTextOption extends EditTextOption{

    private int mDefaultValue;

    public IntEditTextOption(int editTextID, String mHint, String id, int data) {
        super(editTextID, id, data, mHint);

        mDefaultValue = data;
    }

    public static List<IntEditTextOption> loadEditTextOptions(Context ctx) {

        List<IntEditTextOption> mIntEditTextOptions = new ArrayList<>();

        mIntEditTextOptions.add(new IntEditTextOption(R.id.editTextMaxScore, ctx.getString(R.string.max_score)
                , Option.WINNING_SCORE, 0));

        mIntEditTextOptions.add(new IntEditTextOption(R.id.editTextScoreInterval, ctx.getString(R.string.score_interval)
                , Option.SCORE_INTERVAL, 1));

        mIntEditTextOptions.add(new IntEditTextOption(R.id.editTextDiffToWin, ctx.getString(R.string.diff_to_win)
                , Option.SCORE_DIFF_TO_WIN, 0));

        mIntEditTextOptions.add(new IntEditTextOption(R.id.editTextNumSets, ctx.getString(R.string.num_sets)
                , Option.NUMBER_SETS, 1));

        mIntEditTextOptions.add(new IntEditTextOption(R.id.editTextStartingScore, ctx.getString(R.string.starting_score)
                , Option.STARTING_SCORE, 0));

        mIntEditTextOptions.add(new IntEditTextOption(R.id.editTextDiceMin, ctx.getString(R.string.dice_minimum)
                , Option.DICE_MIN, 1));

        mIntEditTextOptions.add(new IntEditTextOption(R.id.editTextDiceMax, ctx.getString(R.string.dice_maximum)
                , Option.DICE_MAX, 6));

        return mIntEditTextOptions;
    }


    public int getmDefaultValue() {
        return mDefaultValue;
    }


}
