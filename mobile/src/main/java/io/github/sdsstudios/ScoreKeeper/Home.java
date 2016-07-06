package io.github.sdsstudios.ScoreKeeper;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.firebase.analytics.FirebaseAnalytics;

public class Home extends AppCompatActivity implements UpdateTabsListener{

    private Intent newGameIntent;
    private Intent settingsIntent;
    private Intent aboutIntent;
    private RecyclerView.Adapter adapter;
    private ScoreDBAdapter dbHelper;
    private RelativeLayout relativeLayout;
    private FirebaseAnalytics mFirebaseAnalytics;



    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private Home.SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private CustomViewPager mViewPager;
    private MenuItem deleteMenuItem, settingsMenuItem;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private int currentTab = 1;
    AppBarLayout appBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        appBarLayout = (AppBarLayout)findViewById(R.id.appbar);
        mSectionsPagerAdapter = new Home.SectionsPagerAdapter(getSupportFragmentManager(), 3);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (CustomViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPagingEnabled(true);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentTab = position;

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        dbHelper = new ScoreDBAdapter(this).open();

        newGameIntent = new Intent(this, NewGame.class);
        aboutIntent = new Intent(this, About.class);
        settingsIntent = new Intent(this, Settings.class);
        relativeLayout = (RelativeLayout) findViewById(R.id.historyLayout);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabNewGame);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(newGameIntent);
            }
        });


    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        deleteMenuItem = menu.findItem(R.id.action_delete);
        settingsMenuItem = menu.findItem(R.id.action_settings);
        deleteMenuItem.setVisible(false);

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        dbHelper.open();

        mViewPager.setAdapter(mSectionsPagerAdapter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        dbHelper.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(settingsIntent);
            return true;
        }if (id == R.id.action_about) {
            startActivity(aboutIntent);
            return true;
        }

        if (id == android.R.id.home) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public void gamesDeleted() {
        mSectionsPagerAdapter = new Home.SectionsPagerAdapter(getSupportFragmentManager(), 3);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (CustomViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(this.currentTab);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void multiSelectEnabled() {
        AppBarLayout.LayoutParams layoutParams = new AppBarLayout.LayoutParams(0,0);
        tabLayout.setLayoutParams(layoutParams);
        toolbar.setLayoutParams(layoutParams);
        mViewPager.setPagingEnabled(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //hold current color of status bar
            int statusBarColor = getWindow().getStatusBarColor();
            //set your gray color
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

    }

    @Override
    public void multiSelectDisabled() {
        AppBarLayout.LayoutParams layoutParams = new AppBarLayout.LayoutParams(AppBarLayout.LayoutParams.MATCH_PARENT, AppBarLayout.LayoutParams.WRAP_CONTENT);
        tabLayout.setLayoutParams(layoutParams);
        toolbar.setLayoutParams(layoutParams);
        mViewPager.setPagingEnabled(true);


    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter{
        private int count;

        public SectionsPagerAdapter(FragmentManager fm, int count) {
            super(fm);
            this.count = count;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return count;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getString(R.string.in_progress);
                case 1:
                    return getResources().getString(R.string.completed);
                case 2:
                    return getResources().getString(R.string.all_games);

            }
            return null;
        }
    }
}



