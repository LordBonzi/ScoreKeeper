package io.github.sdsstudios.ScoreKeeper;

/**
 * Created by seth on 18/09/16.
 */

public class EditTextOption extends Option{

    private String mDefaultValue;
    private String mHint;

    public EditTextOption(OptionID id, Object data, String mHint) {
        super(id, data);
        this.mHint = mHint;
        mDefaultValue = String.valueOf(data);
    }


    public String getmDefaultValue() {
        return mDefaultValue;
    }

    public String getmHint() {
        return mHint;
    }

    public void setmHint(String mHint) {
        this.mHint = mHint;
    }

}
