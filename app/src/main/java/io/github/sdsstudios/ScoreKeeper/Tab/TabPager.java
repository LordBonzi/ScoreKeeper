package io.github.sdsstudios.ScoreKeeper.Tab;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import io.github.sdsstudios.ScoreKeeper.Activity;
import io.github.sdsstudios.ScoreKeeper.R;

/**
 * Created by seth on 15/01/17.
 */
public class TabPager extends FragmentPagerAdapter {

    private Activity mActivity;
    private Context mCtx;

    public TabPager(FragmentManager fm, Context context, Activity activity) {
        super(fm);

        this.mCtx = context;
        this.mActivity = activity;
    }

    @Override
    public Fragment getItem(int position) {
        return TabFragment.newInstance(position + 1);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return (mActivity == Activity.GAME_ACTIVITY) ? mCtx.getString(R.string.game) : mCtx.getString(R.string.options);
            case 1:
                return (mActivity == Activity.GAME_ACTIVITY) ? mCtx.getString(R.string.sets) : mCtx.getString(R.string.sets);

        }
        return null;
    }
}
