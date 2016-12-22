package io.github.sdsstudios.ScoreKeeper.OptionTabs;

import android.content.Context;

import io.github.sdsstudios.ScoreKeeper.Option;

/**
 * Created by seth on 21/12/16.
 */

public class CheckBoxPreference extends OptionPreference {

    public CheckBoxPreference(Context context, Option.OptionListener optionListener) {
        super(context, optionListener);
    }

    @Override
    public Option.OptionID getID() {
        return null;
    }

    @Override
    public void setEnabled(boolean enabled) {

    }

    @Override
    public void loadOption(Option option) {

    }
}
