package io.github.sdsstudios.ScoreKeeper;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by seth on 18/09/16.
 */

public class StringEditTextOption extends EditTextOption{

    public StringEditTextOption(OptionID id, String data, String hint) {
        super(id, data, hint);
    }

    public static List<StringEditTextOption> loadEditTextOptions(Activity a) {
        //MUST BE IN ORDER OF THE LIST OF POINTERS ABOVE!!!

        List<StringEditTextOption> mEditTextOptions = new ArrayList<>();

        mEditTextOptions.add(new StringEditTextOption(OptionID.LENGTH, "00:00:00:0", a.getString(R.string.length)));

        mEditTextOptions.add(new StringEditTextOption(OptionID.DATE, "", a.getString(R.string.date)));

        mEditTextOptions.add(new StringEditTextOption(OptionID.TITLE, "The Game With No Name", a.getString(R.string.title)));

        return mEditTextOptions;
    }

}
