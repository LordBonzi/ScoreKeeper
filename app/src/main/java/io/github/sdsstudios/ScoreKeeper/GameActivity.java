package io.github.sdsstudios.ScoreKeeper;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import io.github.sdsstudios.ScoreKeeper.Listeners.ButtonPlayerListener;
import io.github.sdsstudios.ScoreKeeper.Listeners.GameListener;
import io.github.sdsstudios.ScoreKeeper.Options.Option.OptionID;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class GameActivity extends ScoreKeeperActivity
        implements View.OnClickListener, DialogInterface.OnShowListener, Stopwatch.OnChronometerTickListener
        , GameListener, SetGridViewAdapter.OnScoreClickListener, ViewTreeObserver.OnGlobalLayoutListener, ButtonPlayerListener {

    private static final String STATE_GAMEID = "GAME_ID";
    public static int GAME_ID;
    /**
     * Equal the index of the tab
     **/
    private final int SETS_LAYOUT = 1;
    private final int GAME_LAYOUT = 0;
    private final int PLAYER_1 = 0;
    private final int PLAYER_2 = 1;
    private final int STOPWATCH_DELAY = 300;
    private TypedValue mTypedValue = new TypedValue();

    private boolean mWon = false;
    private String mWinnerString;
    private RecyclerView mPlayerRecyclerView;
    private boolean mFinished = false;
    private String TAG = "GameActivity.class";
    private CardView mCardViewStopwatch;
    private Stopwatch mStopwatch;
    private boolean mClassicTheme = false;
    private View mDialogView;
    private AlertDialog mAlertDialog;
    private long mTimeWhenStopped = 0L;
    private boolean mPaused = false;
    private MenuItem mMenuItemDiceNum;
    private RelativeLayout mBaseLayout;
    private View mNormalLayout;
    private CoordinatorLayout mCoordinatorLayout;
    private ViewGroup.LayoutParams mParams;
    private TabLayout mTabLayout;
    private GridView mSetGridView;
    private int mMaxNumDice, mMinNumDice, mStartingScore, mScoreInterval;
    private int mAccentColor, mPrimaryColor;
    private String mTimeLimit;
    private Random mRandom = new Random();
    private SetGridViewAdapter mSetGridViewAdapter;
    private List<ButtonPlayer> mButtonsPlayerList = new ArrayList<>();

    private Handler mPausedHandler = new Handler();
    private Runnable mPausedRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        GAME_ID = extras.getInt("GAME_ID");

        mAccentColor = mSharedPreferences.getInt("prefAccentColor", Themes.DEFAULT_ACCENT_COLOR);
        mPrimaryColor = mSharedPreferences.getInt("prefPrimaryColor", Themes.DEFAULT_PRIMARY_COLOR(this));

        mGame = mDataHelper.getGame(GAME_ID, mDbHelper);
        mGame.setGameListener(this);

        mClassicTheme = mSharedPreferences.getBoolean("prefClassicScoreboard", false) && mGame.size() == 2;

        if (mClassicTheme) {
            setContentView(R.layout.activity_main_classic);
            setTheme(mAccentColor);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.black));
            }

        } else {

            Themes.themeActivity(this, R.layout.activity_main, true);

        }

        AdView mAdView = (AdView) findViewById(R.id.adViewHome);

        AdCreator adCreator = new AdCreator(mAdView, this);
        adCreator.createAd();

        loadGame();

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String time = sdfDate.format(now);
        mGame.setmTime(time);

        if (savedInstanceState != null) {
            GAME_ID = savedInstanceState.getInt(STATE_GAMEID);
        }

        updateGame();
        getTheme().resolveAttribute(android.R.attr.textColorSecondary, mTypedValue, true);

        mPausedRunnable = new Runnable() {
            boolean red = false;


            @Override
            public void run() {
                if (red) {
                    mStopwatch.setTextColor(getResources().getColor(R.color.transparent));
                } else {
                    mStopwatch.setTextColor(getResources().getColor(R.color.stop));
                }

                red = !red;
                mPausedHandler.postDelayed(this, STOPWATCH_DELAY);
            }
        };

    }

    @Override
    Activity getActivity() {
        return Activity.GAME_ACTIVITY;
    }

    private void populateSetGridView() {
        mSetGridView.setNumColumns(mGame.size());
        mSetGridViewAdapter = new SetGridViewAdapter(mGame.getmPlayerArray(), this, this);
        mSetGridView.setAdapter(mSetGridViewAdapter);
    }

    private boolean TWO_PLAYER_GAME() {
        return mGame.size() == 2;
    }

    private void createButtonsPlayerList() {
        if (TWO_PLAYER_GAME()) {
            mButtonsPlayerList = ButtonPlayer.createButtonPlayerList(mGame, this, this);
            mButtonsPlayerList.get(0).getmButton().getViewTreeObserver().addOnGlobalLayoutListener(this);
        }
    }

    private void loadGame() {

        int mScoreDiffToWin = mGame.getInt(OptionID.SCORE_DIFF_TO_WIN);
        mScoreInterval = mGame.getInt(OptionID.SCORE_INTERVAL);
        mStartingScore = mGame.getInt(OptionID.STARTING_SCORE);
        mMaxNumDice = mGame.getInt(OptionID.DICE_MAX);
        mMinNumDice = mGame.getInt(OptionID.DICE_MIN);

        if (mScoreInterval == 0) {
            mScoreInterval = 1;
        }

        if (mScoreDiffToWin == 0) {
            mScoreDiffToWin = 1;
        }

        mHomeIntent = new Intent(this, Home.class);

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        mBaseLayout = (RelativeLayout) findViewById(R.id.content);

        createButtonsPlayerList();

        mCardViewStopwatch = (CardView) findViewById(R.id.stopwatchCardview);
        mCardViewStopwatch.setOnClickListener(this);

        mStopwatch = (Stopwatch) findViewById(R.id.stopwatch);

        mSetGridView = (GridView) findViewById(R.id.setGridView);

        if (!mClassicTheme) {

            mPlayerRecyclerView = (RecyclerView) findViewById(R.id.playerRecyclerView);

            mNormalLayout = findViewById(R.id.layoutNormal);

            SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

            // Set up the ViewPager with the sections adapter.
            ViewPager mViewPager = (ViewPager) findViewById(R.id.option_tab_container);
            mViewPager.setAdapter(mSectionsPagerAdapter);

            mTabLayout = (TabLayout) findViewById(R.id.tabs);
            mTabLayout.setupWithViewPager(mViewPager);

            for (int i = 0; i < mTabLayout.getChildCount(); i++) {
                mTabLayout.getChildAt(i).setBackgroundColor(mPrimaryColor);
            }

            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

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
            });


        } else {

            Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/digitalfont.ttf");

            mStopwatch.setTypeface(tf);
        }

        selectLayout();

        mStopwatch.setOnChronometerTickListener(this);

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("lastplayedgame", GAME_ID);
        editor.apply();

        mGame.setGameListener(this);
        mGame.isGameWon();

    }

    private boolean timeLimitReached() {
        boolean b = false;

        if (mGame.getmTimeLimit() != null) {

            if (mStopwatch.getText().equals(mTimeLimit)) {

                if (!mFinished) {
                    timeLimitDialog();
                }

                mFinished = true;
                b = true;

            }
        }

        return b;

    }

    @Override
    protected void onStop() {
        super.onStop();
        chronometerClick();

        if (mGame.isChecked(OptionID.STOPWATCH)) {
            mGame.setmLength(mStopwatch.getText().toString());
        }

        updateGame();
    }

    private void displayRecyclerView(boolean enabled) {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mPlayerRecyclerView.setLayoutManager(mLayoutManager);
        RecyclerView.Adapter bigGameAdapter = new BigGameAdapter(mGame, mDbHelper, enabled, this);
        mPlayerRecyclerView.setAdapter(bigGameAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_reset).setVisible(true);
        menu.findItem(R.id.action_fullscreen).setVisible(true);
        menu.findItem(R.id.action_dice).setVisible(true);
        menu.findItem(R.id.action_add).setVisible(true);
        mMenuItemDiceNum = menu.findItem(R.id.action_dice_num);
        return true;
    }

    private void enablePlayerButtons(boolean enabled) {
        for (ButtonPlayer button : mButtonsPlayerList) {
            button.getmButton().setEnabled(enabled);
        }
    }

    private void timeLimitDialog() {

        enablePlayerButtons(false);
        displayRecyclerView(false);

        if (!mPaused) {
            mPaused = true;
            chronometerClick();
        }

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        mDialogView = inflater.inflate(R.layout.create_time_limit, null);

        final EditText editTextHour = (EditText) mDialogView.findViewById(R.id.editTextHour);
        final EditText editTextMinute = (EditText) mDialogView.findViewById(R.id.editTextMinute);
        final EditText editTextSecond = (EditText) mDialogView.findViewById(R.id.editTextSeconds);
        final CheckBox checkBoxExtend = (CheckBox) mDialogView.findViewById(R.id.checkBoxExtend);
        checkBoxExtend.setVisibility(View.VISIBLE);
        final RelativeLayout relativeLayout = (RelativeLayout) mDialogView.findViewById(R.id.relativeLayout2);
        editTextHour.setText("0");
        editTextMinute.setText("0");
        editTextSecond.setText("0");

        checkBoxExtend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBoxExtend.isChecked()) {
                    relativeLayout.setVisibility(View.VISIBLE);
                } else {
                    relativeLayout.setVisibility(INVISIBLE);
                }
            }
        });

        dialogBuilder.setTitle(R.string.time_limit_reached);
        dialogBuilder.setMessage(R.string.time_limit_question);
        dialogBuilder.setPositiveButton(R.string.done, null);
        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialogBuilder.setView(mDialogView);
        mAlertDialog = dialogBuilder.create();
        mAlertDialog.setOnShowListener(this);

        mAlertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        if (id == R.id.action_reset) {
            mPaused = true;
            chronometerClick();

            AlertDialog dialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle(R.string.reset_game_question);

            builder.setMessage(R.string.reset_game_message);

            builder.setPositiveButton(R.string.reset, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    List<Player> mPlayersArray = mGame.getmPlayerArray();

                    for (Player p : mPlayersArray) {
                        p.setmScore(0);
                    }

                    if (mGame.isChecked(OptionID.STOPWATCH)) {
                        mGame.setmLength(mStopwatch.getText().toString());
                    }

                    updateGame();

                    if (mGame.size() > 2) {

                        displayRecyclerView(true);

                    } else {

                        createButtonsPlayerList();
                    }

                    mStopwatch.setBase(SystemClock.elapsedRealtime());
                    mTimeWhenStopped = 0L;

                }
            });

            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });

            dialog = builder.create();
            dialog.show();

            return true;
        }

        if (id == R.id.action_fullscreen) {

            setFullScreen(true);

        }

        if (id == R.id.action_dice) {

            if (!mMenuItemDiceNum.isVisible()) {
                mMenuItemDiceNum.setVisible(true);
            }

            int randomNum = mRandom.nextInt((mMaxNumDice - mMinNumDice) + 1) + mMinNumDice;
            mMenuItemDiceNum.setTitle(String.valueOf(randomNum));
        }

        if (id == R.id.action_add) {
            if (!mFinished) {
                playerDialog(new Player("", mStartingScore), mGame.size(), Dialog.ADD_PLAYER, 0);
            } else {
                Toast.makeText(this, "Game has completed, you can't add more players", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putInt(STATE_GAMEID, GAME_ID);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mPaused = true;
        chronometerClick();

        if (mGame.isChecked(OptionID.STOPWATCH)) {
            mGame.setmLength(mStopwatch.getText().toString());
        }

        updateGame();
    }

    private void chronometerClick() {
        if (mGame.isChecked(OptionID.STOPWATCH)) {
            if (!mPaused) {
                mStopwatch.setBase(SystemClock.elapsedRealtime() + mTimeWhenStopped);
                mStopwatch.start();
                mPausedHandler.removeCallbacks(mPausedRunnable);
                mStopwatch.setTextColor(getResources().getColor(R.color.start));
            } else {
                mPausedHandler.removeCallbacks(mPausedRunnable);
                mPausedHandler.postDelayed(mPausedRunnable, 0);
                mTimeWhenStopped = mStopwatch.getBase() - SystemClock.elapsedRealtime();
                mStopwatch.stop();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.stopwatchCardview:
                if (!mFinished) {
                    mPaused = !mPaused;
                    chronometerClick();
                }
                break;

            case R.id.textViewP1:
                break;

            case R.id.textViewP2:
                playerDialog(mGame.getPlayer(1), 1, Dialog.EDIT_PLAYER, 0);

                break;

        }

    }

    public boolean isFullScreen() {
        return !mClassicTheme && !getSupportActionBar().isShowing();
    }

    public void setFullScreen(boolean fullscreen) {

        if (fullscreen) {
            mParams = mBaseLayout.getLayoutParams();
            getSupportActionBar().hide();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

                mCoordinatorLayout.setFitsSystemWindows(false);

            }

            CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(
                    CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                    CoordinatorLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(0, mTabLayout.getHeight(), 0, 0);
            mBaseLayout.setLayoutParams(params);

        } else {
            if (!mClassicTheme) {
                getSupportActionBar().show();

                boolean colorNavBar = mSharedPreferences.getBoolean("prefColorNavBar", false);
                int primaryDarkColor = mSharedPreferences.getInt("prefPrimaryDarkColor", getResources().getColor(R.color.primaryIndigoDark));

                if (colorNavBar && !mClassicTheme) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().setNavigationBarColor(primaryDarkColor);
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

                    mCoordinatorLayout.setFitsSystemWindows(true);
                }

                if (mParams != null) {
                    mBaseLayout.setLayoutParams(mParams);
                }

                mTabLayout.setBackgroundColor(getResources().getColor(R.color.transparent));

            }
        }

    }

    @Override
    public void onBackPressed() {

        if (!mFinished && !isFullScreen()) {
            if (!mPaused) {
                mPaused = true;
                chronometerClick();
            }
            AlertDialog dialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle(R.string.quit_game);

            builder.setMessage(R.string.quit_game_message);

            builder.setNeutralButton(R.string.complete_later, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    if (mGame.isChecked(OptionID.STOPWATCH)) {
                        mGame.setmLength(mStopwatch.getText().toString());
                    }

                    mGame.setmCompleted(false);
                    updateGame();

                    startActivity(mHomeIntent);

                }
            });

            builder.setPositiveButton(R.string.complete_game, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    if (mGame.isChecked(OptionID.STOPWATCH)) {
                        mGame.setmLength(mStopwatch.getText().toString());
                    }

                    mGame.setmCompleted(true);

                    updateGame();

                    startActivity(mHomeIntent);
                }
            });

            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });

            dialog = builder.create();

            dialog.show();

        } else if (mWon && !isFullScreen()) {

            winnerDialog(mWinnerString);

        } else if (timeLimitReached() && !isFullScreen()) {

            timeLimitDialog();

        } else {
            setFullScreen(false);

        }
    }

    private void winnerDialog(String winner) {

        setFullScreen(false);

        AlertDialog dialog;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (mGame.numSetsPlayed() == mGame.numSets()) {

            builder.setTitle(winner + " " + getString(R.string.has_won) + " overall");
        } else {

            builder.setTitle(winner + " " + getString(R.string.has_won));

        }

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        if (mGame.numSets() > 1 && mGame.numSetsPlayed() < mGame.numSets()) {

            builder.setPositiveButton(R.string.new_set, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {

                    mGame.startNewSet();

                    mFinished = false;
                    mPaused = false;

                    chronometerClick();
                    enablePlayerButtons(true);
                    mWon = false;
                    updateGame();

                    if (TWO_PLAYER_GAME()) {

                        for (ButtonPlayer buttonPlayer : mButtonsPlayerList) {
                            buttonPlayer.startNewSet();
                        }

                    } else {
                        displayRecyclerView(true);
                    }

                    populateSetGridView();

                }
            });

            builder.setNeutralButton(R.string.complete_later, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    if (mGame.isChecked(OptionID.STOPWATCH)) {
                        mGame.setmLength(mStopwatch.getText().toString());
                    }

                    updateGame();
                    startActivity(mHomeIntent);
                }
            });

        } else {

            builder.setPositiveButton(R.string.complete_game, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    if (mGame.isChecked(OptionID.STOPWATCH)) {
                        mGame.setmLength(mStopwatch.getText().toString());
                    }

                    mGame.setmCompleted(true);

                    updateGame();

                    startActivity(mHomeIntent);
                }
            });
        }

        enablePlayerButtons(false);

        dialog = builder.create();

        dialog.show();

        mFinished = true;
        mPaused = true;
        chronometerClick();

        mWon = true;

    }


    @Override
    public void onShow(final DialogInterface dialogInterface) {
        Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final CheckBox checkBoxExtend = (CheckBox) mDialogView.findViewById(R.id.checkBoxExtend);

                if (checkBoxExtend.isChecked()) {
                    try {

                        String timeLimitString = TimeLimit.updateTimeLimit(mDialogView, mGame.getmTimeLimit().getmTime());

                        if (timeLimitString != null) {

                            if (!timeLimitString.equals("00:00:00:0")) {

                                mGame.setmTimeLimit(new TimeLimit(mDataHelper.createTimeLimitCondensed(timeLimitString), timeLimitString));
                                updateGame();

                                mTimeLimit = timeLimitString;

                                timeLimitReached();
                                enablePlayerButtons(true);
                                displayRecyclerView(true);
                                mAlertDialog.dismiss();
                                mCardViewStopwatch.setEnabled(true);
                                mFinished = false;

                            } else {
                                mFinished = true;
                                mAlertDialog.dismiss();
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast toast = Toast.makeText(getBaseContext(), R.string.invalid_length, Toast.LENGTH_SHORT);
                        toast.show();
                    }

                } else {
                    mGame.setmCompleted(true);
                    startActivity(mHomeIntent);

                }
            }
        });

        updateGame();
    }

    @Override
    public void onChronometerTick(Stopwatch chronometer) {
        timeLimitReached();
    }

    private void reloadPlayerButtons() {
        for (int i = 0; i < mGame.size(); i++) {
            mButtonsPlayerList.get(i).reload(mGame.getPlayer(i));
        }
    }

    @Override
    public void onGameWon(String winner) {
        this.mWinnerString = winner;

        if (TWO_PLAYER_GAME()) {
            reloadPlayerButtons();
        }

        mFinished = true;
        mPaused = true;
        chronometerClick();
        mWon = true;

        if (!mClassicTheme) {
            displayRecyclerView(false);
        }

        winnerDialog(winner);

    }

    @Override
    public void deletePlayer(int position) {
        mPaused = true;
        chronometerClick();

        if (mGame.size() > 2) {
            mGame.removePlayer(position);

        } else {
            Toast.makeText(this, R.string.more_than_two_players, Toast.LENGTH_SHORT).show();
        }

        if (mGame.isChecked(OptionID.STOPWATCH)) {
            mGame.setmLength(mStopwatch.getText().toString());
        }

        updateGame();

        if (mGame.size() == 2) {
            createButtonsPlayerList();
        }

        selectLayout();
        chronometerClick();
    }

    @Override
    public void editPlayer(int position) {
        playerDialog(mGame.getPlayer(position), position, Dialog.EDIT_PLAYER, 0);
    }

    private void playerDialog(final Player player, final int position, final Dialog type, final int setPosition) {
        mPaused = true;
        chronometerClick();

        final Player oldPlayer = player;

        final int oldScore = (type == Dialog.CHANGE_SET)
                ? player.getmSetScores().get(setPosition)
                : player.getmScore();

        final View dialogView;

        LayoutInflater inflter = LayoutInflater.from(this);
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogView = inflter.inflate(R.layout.edit_player_fragment, null);

        final EditText editTextPlayer = (EditText) dialogView.findViewById(R.id.editTextPlayer);
        final EditText editTextScore = (EditText) dialogView.findViewById(R.id.editTextScore);

        editTextPlayer.setHint(player.getmName());

        switch (type) {

            case CHANGE_SET:
                editTextScore.setHint(String.valueOf(player.getmSetScores().get(setPosition)));
                break;

            default:
                editTextScore.setHint(String.valueOf(player.getmScore()));
                break;

        }

        dialogBuilder.setPositiveButton(R.string.done, null);

        switch (type) {

            case EDIT_PLAYER:
                dialogBuilder.setTitle(getResources().getString(R.string.edit_player));
                break;

            case CHANGE_SET:
                dialogBuilder.setTitle(getResources().getString(R.string.change_set_score));
                break;

            case ADD_PLAYER:
                dialogBuilder.setTitle(getResources().getString(R.string.add_player));
                break;

        }

        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (type != Dialog.ADD_PLAYER) {
                    mGame.setPlayer(oldPlayer, position);
                }
                dialog.dismiss();
            }
        });

        dialogBuilder.setView(dialogView);

        mAlertDialog = dialogBuilder.create();

        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {
                editTextPlayer.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        player.setmName(editable.toString());
                    }
                });

                editTextScore.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        if (type == Dialog.CHANGE_SET) {
                            if (editable.toString().equals("")) {
                                player.changeSetScore(setPosition, oldScore);
                            } else {
                                player.changeSetScore(setPosition, Integer.valueOf(editable.toString()));
                            }
                        } else {
                            if (editable.toString().equals("")) {
                                player.setmScore(oldScore);
                            } else {
                                player.setmScore(Integer.valueOf(editable.toString()));
                            }
                        }

                    }
                });

                Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        if (type == Dialog.ADD_PLAYER) {
                            mGame.addNewPlayer(player);
                        } else {
                            mGame.setPlayer(player, position);
                        }

                        if (mDataHelper.checkPlayerDuplicates(mGame.getmPlayerArray())) {
                            if (type == Dialog.ADD_PLAYER) {
                                mGame.removePlayer(position);
                            } else {
                                mGame.setPlayer(oldPlayer, position);
                            }
                            Toast.makeText(GameActivity.this, R.string.duplicates_message, Toast.LENGTH_SHORT).show();

                        } else if (player.getmName().equals("")) {

                            if (type == Dialog.ADD_PLAYER) {
                                mGame.removePlayer(position);
                            } else {
                                mGame.setPlayer(oldPlayer, position);
                            }

                            Toast.makeText(GameActivity.this, R.string.must_have_name, Toast.LENGTH_SHORT).show();

                        } else {

                            mAlertDialog.dismiss();

                            if (mGame.isChecked(OptionID.STOPWATCH)) {
                                mGame.setmLength(mStopwatch.getText().toString());
                            }

                            updateGame();

                            selectLayout();
                            mPaused = true;
                            chronometerClick();
                            mGame.isGameWon();

                            populateSetGridView();

                            switch (mTabLayout.getSelectedTabPosition()) {
                                case GAME_LAYOUT:
                                    chooseTab(GAME_LAYOUT);
                                    break;

                                case SETS_LAYOUT:
                                    chooseTab(SETS_LAYOUT);
                                    break;
                            }

                        }

                    }

                });
            }

        });
        mAlertDialog.show();
    }

    private void chooseTab(int layout) {
        if (layout == GAME_LAYOUT) {
            showPlayerLayout(VISIBLE);

            if (getOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
                showStopwatch(VISIBLE);
            }

            mSetGridView.setVisibility(INVISIBLE);
        } else {
            showPlayerLayout(INVISIBLE);

            if (getOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
                showStopwatch(INVISIBLE);
            }
            mSetGridView.setVisibility(View.VISIBLE);
        }
    }

    private void showStopwatch(int visible) {
        mCardViewStopwatch.setVisibility(visible);
    }

    private void showPlayerLayout(int visible) {
        if (!TWO_PLAYER_GAME()) {

            mPlayerRecyclerView.setVisibility(visible);

            mNormalLayout.setVisibility(GONE);

            try {
                displayRecyclerView(true);

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.toString());
            }

        } else {

            if (!mClassicTheme) {
                mNormalLayout.setVisibility(visible);
                mPlayerRecyclerView.setVisibility(GONE);

                setButtonParams();

                reloadPlayerButtons();
            }
        }

    }

    private void selectLayout() {

        showPlayerLayout(VISIBLE);

        mCardViewStopwatch.setOnClickListener(this);

        if (mGame.isChecked(OptionID.STOPWATCH)) {

            try {

                if (mGame.getmTimeLimit() != null) {
                    mTimeLimit = mGame.getmTimeLimit().getmTime();
                }

                if (mGame.getmLength() == null || mGame.getmLength().equals("") && mGame.isChecked(OptionID.STOPWATCH)) {
                    mGame.setmLength("00:00:00:0");

                } else if (mTimeWhenStopped != 0L) {
                    /** when coming back from the app running in the background treat it as unpausing the mStopwatch **/
                    mStopwatch.setBase(SystemClock.elapsedRealtime() + mTimeWhenStopped);

                } else {
                    mStopwatch.setBase((-(3600000 + mTimeHelper.convertToLong(mGame.getmLength()) - SystemClock.elapsedRealtime())));
                }

                timeLimitReached();

                if (!mFinished) {
                    mStopwatch.start();
                    mStopwatch.setTextColor(getResources().getColor(R.color.start));
                }

                updateGame();

            } catch (Exception e) {
                e.printStackTrace();
                Snackbar.make(mNormalLayout, "conversion to long error. invalid time type", Snackbar.LENGTH_LONG).show();
                showStopwatch(INVISIBLE);
                enablePlayerButtons(false);
            }

        } else {

            showStopwatch(INVISIBLE);

            if (!TWO_PLAYER_GAME()) {
                mPlayerRecyclerView.setLayoutParams(
                        new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT
                                , RelativeLayout.LayoutParams.MATCH_PARENT));
            }

        }
    }

    private int getOrientation() {
        return getResources().getConfiguration().orientation;
    }

    private void setButtonParams() {

        LinearLayout.LayoutParams params = null;

        if (getOrientation() == Configuration.ORIENTATION_PORTRAIT) {

            int height = (findViewById(R.id.linearLayout1).getWidth() / 2) - 16;
            params = new LinearLayout.LayoutParams(height - 16, height);

            params.setMargins(16, 8, 16, 8);

            getPlayerButton(PLAYER_1).setLayoutParams(params);
            getPlayerButton(PLAYER_2).setLayoutParams(params);

        }
    }

    @Override
    public void onScoreClick(Player player, int position, int setPosition) {
        playerDialog(player, position, Dialog.CHANGE_SET, setPosition);
    }

    private Button getPlayerButton(int player) {
        return mButtonsPlayerList.get(player).getmButton();
    }

    private void setTextSize(float textSize) {
        mStopwatch.setTextSize(textSize);

        for (ButtonPlayer button : mButtonsPlayerList) {
            button.getmButton().setTextSize(textSize);
        }
    }

    @Override
    public void onGlobalLayout() {
        setButtonParams();
        removeOnGlobalLayoutListener(getPlayerButton(PLAYER_1), this);

        setTextSize(findViewById(R.id.toolbar).getHeight() / 2);

        mStopwatch.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mStopwatch.getLineCount() > 1) {
                    setTextSize(findViewById(R.id.toolbar).getHeight() / 3);
                }

                removeOnGlobalLayoutListener(mStopwatch, this);
            }
        });
    }

    @Override
    public void onScoreChange(int playerIndex, int score) {
        mGame.getPlayer(playerIndex).setmScore(score);

        mGame.setGameListener(this);
        mGame.isGameWon();
        updateGame();

    }

    @Override
    public void changePlayerName(int playerIndex) {
        playerDialog(mGame.getPlayer(playerIndex), playerIndex, Dialog.EDIT_PLAYER, 0);
    }

    public static class GameActivityTabFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public GameActivityTabFragment() {
        }

        public static GameActivityTabFragment newInstance(int sectionNumber) {
            GameActivityTabFragment fragment = new GameActivityTabFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = null;
            container.setVisibility(INVISIBLE);
            return rootView;
        }

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a GameActivityTabFragment (defined as a static inner class below).
            return GameActivityTabFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.game);
                case 1:
                    return getString(R.string.sets);

            }
            return null;
        }
    }
}

