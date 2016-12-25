package io.github.sdsstudios.ScoreKeeper.OptionTabs;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.widget.RelativeLayout;

import io.github.sdsstudios.ScoreKeeper.Activity;
import io.github.sdsstudios.ScoreKeeper.Game;
import io.github.sdsstudios.ScoreKeeper.GameDBAdapter;
import io.github.sdsstudios.ScoreKeeper.R;

public class OptionTabPager extends FragmentStatePagerAdapter {

    private Activity mActivity;
    private Context mCtx;
    private Game mGame;
    private GameDBAdapter mGameDBAdapter;
    private RelativeLayout mRelativeLayout;

    public OptionTabPager(FragmentManager fm, Activity mActivity, Context ctx, Game mGame, GameDBAdapter mDbHelper, RelativeLayout mRelativeLayout) {
        super(fm);
        this.mActivity = mActivity;
        this.mCtx = ctx;
        this.mGame = mGame;
        this.mGameDBAdapter = mDbHelper;
        this.mRelativeLayout = mRelativeLayout;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return OptionTabFragment.newInstance(OptionPreference.createNewGameList(mGame, mActivity, mCtx, mRelativeLayout)
                        , mActivity);

            case 1:
                return OptionTabFragment.newInstance(OptionPreference.createAdvancedOptionsList(mGame, mActivity, mCtx, mGameDBAdapter)
                        , mActivity);

            default:
                return null;

        }
    }

    @Override
    public int getCount() {
        if (mActivity == Activity.EDIT_GAME) {
            return 3;
        } else {
            return 2;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {

        if (position == 2 && mActivity == Activity.EDIT_GAME) {
            return mCtx.getString(R.string.sets);
        }

        switch (position) {
            case 0:
                return mActivity == Activity.EDIT_GAME ? mCtx.getString(R.string.info) : mCtx.getString(R.string.options);
            case 1:
                return mActivity == Activity.EDIT_GAME ? mCtx.getString(R.string.options) : mCtx.getString(R.string.advanced);

            default:
                return mCtx.getString(R.string.game);
        }
    }
}