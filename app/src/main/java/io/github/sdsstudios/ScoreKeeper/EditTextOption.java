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
    private Option mOption;

    public EditTextOption(EditText mEditText, String mHint, Option o) {
        this.mEditText = mEditText;
        this.mHint = mHint;
        this.mOption = o;
    }

    public static List<EditTextOption> loadEditTextOptions(Activity a){
        //MUST BE IN ORDER OF THE LIST OF POINTERS ABOVE!!!

        List<EditTextOption> mEditTextOptions = new ArrayList<>();

        mEditTextOptions.add(new EditTextOption((EditText) a.findViewById(R.id.editTextMaxScore), a.getString(R.string.max_score)
                , new Option(Option.WINNING_SCORE, 0)));

        mEditTextOptions.add(new EditTextOption((EditText) a.findViewById(R.id.editTextScoreInterval), a.getString(R.string.score_interval)
                , new Option(Option.SCORE_INTERVAL, 0)));

        mEditTextOptions.add(new EditTextOption((EditText) a.findViewById(R.id.editTextDiffToWin), a.getString(R.string.diff_to_win)
                , new Option(Option.SCORE_DIFF_TO_WIN, 0)));

        mEditTextOptions.add(new EditTextOption((EditText) a.findViewById(R.id.editTextMaxScore), a.getString(R.string.num_sets)
                , new Option(Option.NUMBER_SETS, 0)));

        return mEditTextOptions;
    }

    public int getmID() {
        return mOption.getmID();
    }

    public void setmID(int mID) {
        mOption.setmID(mID);
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

    public int getmData() {
        return mOption.getmData();
    }

    public void setmData(int mData) {
        mOption.setmData(mData);
    }

    public Option getmOption() {
        return mOption;
    }

    public void setmOption(Option mOption) {
        this.mOption = mOption;
    }
}
