package io.github.sdsstudios.ScoreKeeper.Options;

/**
 * Created by seth on 18/09/16.
 */

public class EditTextOption extends Option{

    private int mEditTextID;

    public EditTextOption(int editTextID, String id, Object data, String mHint) {
        super(id, data, mHint);
        mEditTextID = editTextID;
    }

    public int getmEditTextID() {
        return mEditTextID;
    }

    public void setmEditTextID(int mEditTextID) {
        this.mEditTextID = mEditTextID;
    }

}
