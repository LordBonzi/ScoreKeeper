package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crash.FirebaseCrash;

import java.util.ArrayList;

public class Home extends AppCompatActivity {

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
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new Home.SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        dbHelper = new ScoreDBAdapter(this).open();

        newGameIntent = new Intent(this, NewGame.class);
        aboutIntent = new Intent(this, About.class);
        settingsIntent = new Intent(this, Settings.class);
        relativeLayout = (RelativeLayout) findViewById(R.id.historyLayout);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                FirebaseCrash.report(new Exception(t.toString()));

            }
        });

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabNewGame);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(newGameIntent);
            }
        });

        //Shared Preferences stuff
        final String PREFS_NAME = "scorekeeper";

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        if (settings.getBoolean("my_first_time", true)) {

            saveSharedPrefs();
            settings.edit().putBoolean("my_first_time", false).commit();
        }else {
            SharedPreferences sharedPref = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

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

        return super.onOptionsItemSelected(item);
    }

    public void saveSharedPrefs(){
        SharedPreferences sharedPref = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.apply();
    }

    public Integer recentNumGames(){
        int numGames = 0;
        if (Integer.valueOf(dbHelper.getNewestGame()) == 1){
            numGames = 1;
        }else if (Integer.valueOf(dbHelper.getNewestGame()) == 2){
            numGames = 2;
        }else if (Integer.valueOf(dbHelper.getNewestGame()) >= 3){
            numGames = 3;
        }

        return numGames;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        ScoreDBAdapter dbHelper = new ScoreDBAdapter(getActivity());
        RecyclerView.Adapter historyAdapter;
        ArrayList<GameModel> gameModel;
        GameModel gModel;
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static Home.PlaceholderFragment newInstance(int sectionNumber) {
            Home.PlaceholderFragment fragment = new Home.PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onPause() {
            super.onPause();
            gModel.closeDB();
            dbHelper.close();
        }

        @Override
        public void onResume() {
            super.onResume();
            dbHelper.open();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_home, container, false);

            int numGames = 1;
            RecyclerView recyclerViewHome = (RecyclerView)rootView.findViewById(R.id.homeList);
            TextView textViewHome = (TextView)rootView.findViewById(R.id.textViewNoGames);
            RelativeLayout fragmentHomeLayout = (RelativeLayout) getActivity().findViewById(R.id.fragmentHomeLayout);
            RecyclerView.LayoutManager mLayoutManager;

            mLayoutManager = new LinearLayoutManager(getActivity());
            recyclerViewHome.setLayoutManager(mLayoutManager);
            dbHelper = new ScoreDBAdapter(getActivity());
            dbHelper.open();
            gModel = new GameModel(dbHelper);
            Log.e("numrows", ""+dbHelper.numRows());

            try {
                if (dbHelper.numRows() != 0) {
                    switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                        case 1:
                            textViewHome.setText(R.string.games_in_progress);
                            break;

                        case 2:
                            textViewHome.setText(R.string.completed_games);
                            break;

                        case 3:
                            textViewHome.setText(R.string.all_games);
                            break;

                    }

                    gameModel = GameModel.createGameModel(dbHelper.numRows(), getArguments().getInt(ARG_SECTION_NUMBER), getActivity());
                    historyAdapter = new HistoryAdapter(gameModel, getActivity(), fragmentHomeLayout, getArguments().getInt(ARG_SECTION_NUMBER), gameModel.size());
                    recyclerViewHome.setAdapter(historyAdapter);

                } else {

                    switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                        case 1:
                            textViewHome.setText(R.string.games_in_progress);
                            break;

                        case 2:
                            textViewHome.setText(R.string.completed_games);
                            break;

                        case 3:
                            textViewHome.setText(R.string.all_games);
                            break;

                    }

                }
            }catch (Exception e){
                e.printStackTrace();
                FirebaseCrash.report(new Exception(e.toString()));

            }
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return Home.PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
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



