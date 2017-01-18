package io.github.sdsstudios.ScoreKeeper.Activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.widget.GridView;

import io.github.sdsstudios.ScoreKeeper.R;
import io.github.sdsstudios.ScoreKeeper.SetGridViewAdapter;
import io.github.sdsstudios.ScoreKeeper.Tab.TabPager;

/**
 * Created by seth on 15/01/17.
 */

public abstract class ScoreKeeperTabActivity extends OptionActivity implements SetGridViewAdapter.OnScoreClickListener, ViewPager.OnPageChangeListener {

    /**
     * Equal to the index of the tab
     **/

    public static final int SETS_LAYOUT = 1;
    public static final int GAME_LAYOUT = 0;
    public static final int OPTIONS_LAYOUT = 0;

    public GridView mSetGridView;
    public TabLayout mTabLayout;
    public SetGridViewAdapter mSetGridViewAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSetGridView = (GridView) findViewById(R.id.setGridView);
        loadTabs();
    }

    public void populateSetGridView() {
        mSetGridView.setNumColumns(mGame.size());
        mSetGridViewAdapter = new SetGridViewAdapter(mGame.getmPlayerArray(), this, this);
        mSetGridView.setAdapter(mSetGridViewAdapter);
    }

    public void loadTabs() {
        TabPager mTabPager = new TabPager(getSupportFragmentManager(), this, CURRENT_ACTIVITY);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.option_tab_container);
        mViewPager.setAdapter(mTabPager);

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        for (int i = 0; i < mTabLayout.getChildCount(); i++) {
            mTabLayout.getChildAt(i).setBackgroundColor(mPrimaryColor);
        }

        mViewPager.addOnPageChangeListener(this);
    }

    public abstract void chooseTab(int layout);

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {

        switch (position) {

            case 0:
                chooseTab(GAME_LAYOUT);
                break;

            case 1:
                chooseTab(SETS_LAYOUT);
                populateSetGridView();
                break;

        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

}
