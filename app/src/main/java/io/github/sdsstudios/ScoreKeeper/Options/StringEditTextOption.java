package io.github.sdsstudios.ScoreKeeper.Options;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import io.github.sdsstudios.ScoreKeeper.R;

/**
 * Created by seth on 18/09/16.
 */

public class StringEditTextOption extends EditTextOption{

    private String mDefaultValue;

    public StringEditTextOption(int mEditTextID, OptionID id, String data, String hint) {
        super(mEditTextID, id, data, hint);
        mDefaultValue = data;
    }

    public static List<StringEditTextOption> loadEditTextOptions(Activity a) {
        //MUST BE IN ORDER OF THE LIST OF POINTERS ABOVE!!!

        List<StringEditTextOption> mEditTextOptions = new ArrayList<>();

        mEditTextOptions.add(new StringEditTextOption(R.id.editTextLength
                , OptionID.LENGTH, "00:00:00:0", a.getString(R.string.length)));

        mEditTextOptions.add(new StringEditTextOption(R.id.editTextDate
                , OptionID.DATE, "", a.getString(R.string.date)));

        mEditTextOptions.add(new StringEditTextOption(R.id.editTextTitle
                , OptionID.TITLE, "The Game With No Name", a.getString(R.string.title)));

        return mEditTextOptions;
    }


    public String getmDefaultValue() {
        return mDefaultValue;
    }


}
