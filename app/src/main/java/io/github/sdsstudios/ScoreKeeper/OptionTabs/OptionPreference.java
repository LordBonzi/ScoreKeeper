package io.github.sdsstudios.ScoreKeeper.OptionTabs;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import io.github.sdsstudios.ScoreKeeper.Activity;
import io.github.sdsstudios.ScoreKeeper.CheckBoxOption;
import io.github.sdsstudios.ScoreKeeper.Game;
import io.github.sdsstudios.ScoreKeeper.GameDBAdapter;
import io.github.sdsstudios.ScoreKeeper.IntEditTextOption;
import io.github.sdsstudios.ScoreKeeper.Option;
import io.github.sdsstudios.ScoreKeeper.PlayerListAdapter;

/**
 * Created by seth on 15/12/16.
 */

public abstract class OptionPreference extends Preference {

    private static String TAG = "OptionPreference";
    public Option.OptionListener mOptionListener;

    public OptionPreference(Context context, Option.OptionListener optionListener) {
        super(context);
        this.mOptionListener = optionListener;
    }

    public static List<OptionPreference> createNewGameList(Game game, Activity activity, Context context
            , RelativeLayout relativeLayout) {

        List<OptionPreference> optionsList = new ArrayList<>();

        optionsList.add(new PlayerListPreference(context, game.getmPlayerArray(), game, activity, relativeLayout, (PlayerListAdapter.PlayerChangeListener) context));

        return optionsList;
    }

    public static List<OptionPreference> createAdvancedOptionsList(Game game, Activity activity, Context context
            , GameDBAdapter dbAdapter) {
        List<OptionPreference> optionsList = new ArrayList<>();

        for (IntEditTextOption e : game.getmIntEditTextOptions()) {
            optionsList.add(new TextPreference(context, e, game, activity, dbAdapter));
        }

        for (CheckBoxOption c : game.getmCheckBoxOptions()) {
            optionsList.add(new CheckBoxPreference(context, c, game, activity, dbAdapter));
        }

        return optionsList;
    }

    public abstract Option.OptionID getID();
    public abstract void setEnabled(boolean enabled);

    public abstract void setOption(Option option);

}
