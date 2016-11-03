package io.github.sdsstudios.ScoreKeeper;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by seth on 18/09/16.
 */

public class EditTextOption extends Option{

    //DO NOT CHANGE VALUES. WILL CRASH THE APP IF CHANGED

    //EditText IDs only
    public static final int WINNING_SCORE = 0;
    public static final int SCORE_INTERVAL = 1;
    public static final int SCORE_DIFF_TO_WIN = 2;
    public static final int NUMBER_SETS = 3;
    //ADD NEW POINTERS HERE

    private String mHint;
    private int mEditTextID;

    public EditTextOption(int editTextID, String mHint, int id, int data) {
        super(id, data);

        this.mHint = mHint;
        this.mEditTextID = editTextID;
    }

    public static List<EditTextOption> loadEditTextOptions(Activity a){
        //MUST BE IN ORDER OF THE LIST OF POINTERS ABOVE!!!

        List<EditTextOption> mEditTextOptions = new ArrayList<>();

        mEditTextOptions.add(new EditTextOption(R.id.editTextMaxScore, a.getString(R.string.max_score)
                , EditTextOption.WINNING_SCORE, 0));

        mEditTextOptions.add(new EditTextOption(R.id.editTextScoreInterval, a.getString(R.string.score_interval)
                , EditTextOption.SCORE_INTERVAL, 1));

        mEditTextOptions.add(new EditTextOption(R.id.editTextDiffToWin, a.getString(R.string.diff_to_win)
                , EditTextOption.SCORE_DIFF_TO_WIN, 0));

        mEditTextOptions.add(new EditTextOption(R.id.editTextNumSets, a.getString(R.string.num_sets)
                , EditTextOption.NUMBER_SETS, 1));

        return mEditTextOptions;
    }

    public int getmEditTextID() {
        return mEditTextID;
    }

    public void setmEditTextID(int mEditTextID) {
        this.mEditTextID = mEditTextID;
    }

    public String getmHint() {
        return mHint;
    }

    public void setmHint(String mHint) {
        this.mHint = mHint;
    }


}
