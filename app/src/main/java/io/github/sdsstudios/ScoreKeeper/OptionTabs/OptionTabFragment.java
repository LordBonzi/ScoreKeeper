package io.github.sdsstudios.ScoreKeeper.OptionTabs;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import io.github.sdsstudios.ScoreKeeper.Activity;

public class OptionTabFragment extends PreferenceFragmentCompat {

    private final String TAG = "OptionTabFragment";
    private List<OptionPreference> mOptionsList;
    private Activity mActivity;

    public static OptionTabFragment newInstance(List<OptionPreference> optionsList, Activity activity) {

        OptionTabFragment optionTabFragment = new OptionTabFragment();
        optionTabFragment.setmOptionsList(optionsList);
        optionTabFragment.setmActivity(activity);

        return optionTabFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getContext());
        setPreferenceScreen(screen);

        if (mOptionsList != null) {
            for (OptionPreference optionPreference : mOptionsList) {
                screen.addPreference(optionPreference);
            }
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    public void setmActivity(Activity activity) {
        this.mActivity = activity;
    }

    public void setmOptionsList(List<OptionPreference> mOptionsList) {
        this.mOptionsList = mOptionsList;
    }

}