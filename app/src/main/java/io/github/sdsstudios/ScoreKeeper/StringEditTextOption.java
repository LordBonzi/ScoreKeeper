package io.github.sdsstudios.ScoreKeeper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by seth on 18/09/16.
 */

public class StringEditTextOption extends EditTextOption{

    public StringEditTextOption(int editTextID, int id, String data) {
        super(editTextID, id, data);
    }

    public static List<StringEditTextOption> loadEditTextOptions(){
        //MUST BE IN ORDER OF THE LIST OF POINTERS ABOVE!!!

        List<StringEditTextOption> mEditTextOptions = new ArrayList<>();

        mEditTextOptions.add(new StringEditTextOption(R.id.editTextLength
                , StringEditTextOption.LENGTH, "00:00:00:0"));

        mEditTextOptions.add(new StringEditTextOption(R.id.editTextDate
                , StringEditTextOption.DATE, ""));

        mEditTextOptions.add(new StringEditTextOption(R.id.editTextTitle
                , StringEditTextOption.TITLE, "The Game With No Name"));

        return mEditTextOptions;
    }

}
