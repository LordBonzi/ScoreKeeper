package io.github.sdsstudios.ScoreKeeper.Options;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import io.github.sdsstudios.ScoreKeeper.R;

/**
 * Created by seth on 18/09/16.
 */

public class StringEditTextOption extends EditTextOption{

    private String mDefaultValue;

    public StringEditTextOption(int mEditTextID, String id, String data, String hint) {
        super(mEditTextID, id, data, hint);
        mDefaultValue = data;
    }

    public static List<StringEditTextOption> loadEditTextOptions(Context ctx) {

        List<StringEditTextOption> mEditTextOptions = new ArrayList<>();

        mEditTextOptions.add(new StringEditTextOption(R.id.editTextLength
                , Option.LENGTH, "00:00:00:0", ctx.getString(R.string.length)));

        mEditTextOptions.add(new StringEditTextOption(R.id.editTextDate
                , Option.DATE, "", ctx.getString(R.string.date)));

        mEditTextOptions.add(new StringEditTextOption(R.id.editTextTitle
                , Option.TITLE, "The Game With No Name", ctx.getString(R.string.title)));

        mEditTextOptions.add(new StringEditTextOption(R.id.editText
                , Option.NOTES, "", ctx.getString(R.string.notes)));

        return mEditTextOptions;
    }


    public String getmDefaultValue() {
        return mDefaultValue;
    }


}
