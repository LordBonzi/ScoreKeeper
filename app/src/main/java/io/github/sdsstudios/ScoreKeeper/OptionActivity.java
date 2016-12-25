package io.github.sdsstudios.ScoreKeeper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.github.sdsstudios.ScoreKeeper.Helper.DataHelper;
import io.github.sdsstudios.ScoreKeeper.OptionTabs.OptionTabPager;

/**
 * Created by seth on 11/12/16.
 */

public abstract class OptionActivity extends AppCompatActivity implements PlayerListAdapter.PlayerChangeListener {

    public static final String STATE_GAMEID = "mGameID";
    public static String TAG;

    public Snackbar mSnackBar;
    public Activity CURRENT_ACTIVITY;
    public Intent mHomeIntent;
    public RelativeLayout mRelativeLayout;
    public GameDBAdapter mDbHelper;
    public int mGameID;
    public Game mGame;
    public DataHelper mDataHelper = new DataHelper();
    public SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CURRENT_ACTIVITY = getActivity();

        TAG = CURRENT_ACTIVITY == Activity.EDIT_GAME ? "EditGame" : "NewGame";

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Themes.themeActivity(this, CURRENT_ACTIVITY == Activity.EDIT_GAME ? R.layout.activity_edit_game : R.layout.activity_new_game
                , true);

        AdView mAdView = (AdView) findViewById(R.id.adViewHome);
        AdCreator adCreator = new AdCreator(mAdView, this);
        adCreator.createAd();


        mHomeIntent = new Intent(this, Home.class);

        mDbHelper = new GameDBAdapter(this);
        mDbHelper.open();

        if (CURRENT_ACTIVITY == Activity.EDIT_GAME) {
            AdView mAdView2 = (AdView) findViewById(R.id.adViewHome2);
            AdCreator adCreator2 = new AdCreator(mAdView2, this);
            adCreator2.createAd();
            mRelativeLayout = (RelativeLayout) findViewById(R.id.layoutEditGame);

            Bundle extras = getIntent().getExtras();
            mGameID = extras.getInt("GAME_ID");

            mGame = mDataHelper.getGame(mGameID, mDbHelper);
        } else {
            mRelativeLayout = (RelativeLayout) findViewById(R.id.newGameLayout);
        }

        loadActivity(savedInstanceState);
        loadTabs();
    }

    abstract Activity getActivity();

    abstract void loadActivity(Bundle savedInstanceState);

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_delete).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_delete_presets).setVisible(true);
        menu.findItem(R.id.action_delete_timelimits).setVisible(true);
        menu.findItem(R.id.action_reset).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {

            case android.R.id.home:
                onBackPressed();
                break;

        }

        return super.onOptionsItemSelected(item);

    }

    private void loadTabs() {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        OptionTabPager mOptionTabPager = new OptionTabPager(getSupportFragmentManager(), CURRENT_ACTIVITY
                , this, mGame, mDbHelper, mRelativeLayout);

        ViewPager mViewPager = (ViewPager) findViewById(R.id.option_tab_container);
        mViewPager.setAdapter(mOptionTabPager);

        tabLayout.setupWithViewPager(mViewPager);

        for (int i = 0; i < tabLayout.getChildCount(); i++) {
            tabLayout.getChildAt(i).setBackgroundColor(
                    mSharedPreferences.getInt("prefPrimaryColor", Themes.DEFAULT_PRIMARY_COLOR(this)));
        }
    }

    public void setOptionChangeListeners() {

    }

    public void invalidSnackbar(String message) {
        Snackbar snackbar;
        snackbar = Snackbar.make(mRelativeLayout, message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDbHelper.open();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDbHelper.close();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDbHelper.close();
    }

    public void setGameTime() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        mGame.setmTime(sdfDate.format(now));
    }

    public void updateGame() {
        mDbHelper.open().updateGame(mGame);
        mDbHelper.close();
    }

    public void deleteEmptyPlayers() {
        List<Player> playerArray = mGame.getmPlayerArray();

        for (int i = 0; i < playerArray.size(); i++) {
            if (playerArray.get(i).getmName().equals("")
                    || playerArray.get(i).getmName() == null
                    || playerArray.get(i).getmName().equals(" ")) {
                playerArray.remove(i);
            }
        }

        mGame.setmPlayerArray(playerArray);

    }

    /**
     * OnPlayerChangeListener
     **/
    @Override
    public void onPlayerChange(Player player, int position) {
        mGame.setPlayer(player, position);
        updateGame();
    }

    @Override
    public void addPlayer(Player player) {
        player.setmScore(mGame.getInt(Option.OptionID.STARTING_SCORE));

        mGame.addPlayer(player);
        boolean areDuplicatePlayers = mDataHelper.checkPlayerDuplicates(mGame.getmPlayerArray());
        String playerName = player.getmName();

        if (areDuplicatePlayers) {
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSnackBar.dismiss();
                }
            };

            mGame.removePlayer(mGame.size() - 1);

            mSnackBar = Snackbar.make(mRelativeLayout, R.string.duplicates_message, Snackbar.LENGTH_SHORT)
                    .setAction("Dismiss", onClickListener);
            mSnackBar.show();
        }

        if (playerName.equals("") || playerName.equals(" ")) {
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSnackBar.dismiss();
                }
            };

            mGame.removePlayer(mGame.size() - 1);

            mSnackBar = Snackbar.make(mRelativeLayout, R.string.must_have_name, Snackbar.LENGTH_SHORT)
                    .setAction("Dismiss", onClickListener);
            mSnackBar.show();

        } else if (!areDuplicatePlayers && !playerName.equals("") && !playerName.equals(" ")) {

            updateGame();

        }
    }

    @Override
    public void onPlayerRemove(int position) {
        mGame.removePlayer(position);
    }
}


