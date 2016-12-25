package io.github.sdsstudios.ScoreKeeper;

/**
 * Created by seth on 18/09/16.
 */

public class EditTextOption extends Option{

    private String mDefaultValue;

    public EditTextOption(OptionID id, Object data, String mHint) {
        super(id, data, mHint);
        mDefaultValue = String.valueOf(data);
    }

    public String getmDefaultValue() {
        return mDefaultValue;
    }

}
