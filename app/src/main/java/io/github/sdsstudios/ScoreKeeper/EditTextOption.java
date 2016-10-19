package io.github.sdsstudios.ScoreKeeper;

import android.app.Activity;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by seth on 18/09/16.
 */

public class EditTextOption {

    private EditText mEditText;
    private String mHint;
    private int mID;

    public EditTextOption(EditText mEditText, String mHint, int id) {
        this.mEditText = mEditText;
        this.mHint = mHint;
        this.mID = id;
    }

    public static List<EditTextOption> loadEditTextOptions(Activity a){
        //MUST BE IN ORDER OF THE LIST OF POINTERS ABOVE!!!

        List<EditTextOption> mEditTextOptions = new ArrayList<>();

        mEditTextOptions.add(new EditTextOption((EditText) a.findViewById(R.id.editTextMaxScore), a.getString(R.string.max_score)
                , Option.WINNING_SCORE));

        mEditTextOptions.add(new EditTextOption((EditText) a.findViewById(R.id.editTextScoreInterval), a.getString(R.string.score_interval)
                , Option.SCORE_INTERVAL));

        mEditTextOptions.add(new EditTextOption((EditText) a.findViewById(R.id.editTextDiffToWin), a.getString(R.string.diff_to_win)
                , Option.SCORE_DIFF_TO_WIN));

        mEditTextOptions.add(new EditTextOption((EditText) a.findViewById(R.id.editTextNumSets), a.getString(R.string.num_sets)
                , Option.NUMBER_SETS));

        return mEditTextOptions;
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

    public EditText getmEditText() {
        return mEditText;
    }

    public void setmEditText(EditText mEditText) {
        this.mEditText = mEditText;
    }

}
