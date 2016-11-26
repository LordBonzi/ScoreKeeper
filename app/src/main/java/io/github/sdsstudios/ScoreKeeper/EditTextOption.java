package io.github.sdsstudios.ScoreKeeper;

/**
 * Created by seth on 18/09/16.
 */

public class EditTextOption extends Option{

    //DO NOT CHANGE VALUES. WILL CRASH THE APP IN FUTURE UPDATES IF CHANGED

    public static final int NUM_INT_OPTIONS = 5;

    //EditText IDs only
    public static final int WINNING_SCORE = 0;
    public static final int SCORE_INTERVAL = 1;
    public static final int SCORE_DIFF_TO_WIN = 2;
    public static final int NUMBER_SETS = 3;
    public static final int STARTING_SCORE = 4;
    public static final int LENGTH = 5;
    public static final int DATE = 6;
    public static final int TITLE = 7;
    //ADD NEW POINTERS HERE

    private int mEditTextID;

    public EditTextOption(int editTextID, int id, int data) {
        super(id, data);

        this.mEditTextID = editTextID;
    }

    public EditTextOption(int editTextID, int id, String data) {
        super(id, data);

        this.mEditTextID = editTextID;
    }

    public int getmEditTextID() {
        return mEditTextID;
    }

    public void setmEditTextID(int mEditTextID) {
        this.mEditTextID = mEditTextID;
    }

}
