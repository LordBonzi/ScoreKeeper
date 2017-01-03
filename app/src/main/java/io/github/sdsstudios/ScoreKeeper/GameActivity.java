package io.github.sdsstudios.ScoreKeeper;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import io.github.sdsstudios.ScoreKeeper.Helper.DataHelper;
import io.github.sdsstudios.ScoreKeeper.Helper.TimeHelper;
import io.github.sdsstudios.ScoreKeeper.Option.OptionID;

public class GameActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener, DialogInterface.OnShowListener, Stopwatch.OnChronometerTickListener
        , Game.GameListener, SetGridViewAdapter.OnScoreClickListener {

    private static final String STATE_GAMEID = "GAME_ID";
    public static int GAME_ID;
    private boolean mWon = false;
    private String mWinnerString;
    private Button mButtonP1, mButtonP2;
    private TextView mTextViewP1, mTextViewP2;
    private int mP1Score, mP2Score;
    private FloatingActionButton mFabChronometer;
    private RecyclerView mPlayerList;
    private boolean mFinished = false;
    private String TAG = "GameActivity.class";
    private DataHelper mDataHelper;
    private Intent mHomeIntent;
    private GameDBAdapter mDbHelper;
    private Stopwatch mStopwatch;
    private TimeHelper mTimeHelper;
    private boolean mClassicTheme = false;
    private View mDialogView;
    private AlertDialog mAlertDialog;
    private long mTimeWhenStopped = 0L;
    private boolean mPaused = false;
    private MenuItem mMenuItemDiceNum;
    private SharedPreferences mSharedPreferences;
    private RelativeLayout mBaseLayout, mBigLayout, mNormalLayout;
    private CoordinatorLayout mCoordinatorLayout;
    private ViewGroup.LayoutParams mParams;
    private TabLayout mTabLayout;
    private Game mGame;
    private GridView mSetGridView;
    private int mMaxNumDice, mMinNumDice;
    private int mStartingScore;
    private int mScoreInterval;
    private boolean mReverseScoring;
    private int mMaxScore;
    private int mAccentColor, mPrimaryColor;
    private String mTimeLimit;
    private Random mRandom = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        GAME_ID = extras.getInt("GAME_ID");

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mAccentColor = mSharedPreferences.getInt("prefAccentColor", Themes.DEFAULT_ACCENT_COLOR);
        mPrimaryColor = mSharedPreferences.getInt("prefPrimaryColor", Themes.DEFAULT_PRIMARY_COLOR(this));

        loadObjects();

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

        mDbHelper.open().updateGame(mGame);

    }

    public void populateSetGridView() {
        mSetGridView.setNumColumns(mGame.size());
        SetGridViewAdapter setGridViewAdapter = new SetGridViewAdapter(mGame.getmPlayerArray(), this, this);
        mSetGridView.setAdapter(setGridViewAdapter);
    }

    public void loadObjects() {

        mDataHelper = new DataHelper();
        mTimeHelper = new TimeHelper();

        mDbHelper = new GameDBAdapter(this);
    }

    public void loadGame() {

        int mScoreDiffToWin = mGame.getInt(Option.OptionID.SCORE_DIFF_TO_WIN);
        mScoreInterval = mGame.getInt(Option.OptionID.SCORE_INTERVAL);
        mStartingScore = mGame.getInt(Option.OptionID.STARTING_SCORE);
        mReverseScoring = mGame.isChecked(Option.OptionID.REVERSE_SCORING);
        mMaxScore = mGame.getInt(Option.OptionID.WINNING_SCORE);
        mMaxNumDice = mGame.getInt(Option.OptionID.DICE_MAX);
        mMinNumDice = mGame.getInt(Option.OptionID.DICE_MIN);

        if (mScoreInterval == 0) {
            mScoreInterval = 1;
        }

        if (mScoreDiffToWin == 0) {
            mScoreDiffToWin = 1;
        }

        mHomeIntent = new Intent(this, Home.class);

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        mBaseLayout = (RelativeLayout) findViewById(R.id.content);

        mButtonP1 = (Button) findViewById(R.id.buttonP1);
        mButtonP1.setOnClickListener(this);
        mButtonP1.setOnLongClickListener(this);

        mButtonP2 = (Button) findViewById(R.id.buttonP2);
        mButtonP2.setOnClickListener(this);
        mButtonP2.setOnLongClickListener(this);

        if (!mClassicTheme) {
            ImageButton buttonEditP1 = (ImageButton) findViewById(R.id.buttonEditP1);
            buttonEditP1.setOnClickListener(this);

            ImageButton buttonEditP2 = (ImageButton) findViewById(R.id.buttonEditP2);
            buttonEditP2.setOnClickListener(this);
        }

        mStopwatch = new Stopwatch(this);

        mTextViewP1 = (TextView) findViewById(R.id.textViewP1);
        mTextViewP2 = (TextView) findViewById(R.id.textViewP2);

        mSetGridView = (GridView) findViewById(R.id.setGridView);

        if (!mClassicTheme) {

            mPlayerList = (RecyclerView) findViewById(R.id.bigGameList);

            mNormalLayout = (RelativeLayout) findViewById(R.id.layoutNormal);
            mBigLayout = (RelativeLayout) findViewById(R.id.layoutBig);

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
                            findViewById(R.id.gameRelativeLayout).setVisibility(View.VISIBLE);
                            mSetGridView.setVisibility(View.INVISIBLE);
                            break;

                        case 1:
                            findViewById(R.id.gameRelativeLayout).setVisibility(View.INVISIBLE);
                            mSetGridView.setVisibility(View.VISIBLE);
                            populateSetGridView();
                            break;

                        case 2:
                            Toast.makeText(GameActivity.this, "Coming Soon ;)", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });


        } else {

            Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/digitalfont.ttf");

            // Applying font
            mTextViewP1.setTypeface(tf);
            mTextViewP2.setTypeface(tf);
            mStopwatch.setTypeface(tf);
            mButtonP1.setTypeface(tf);
            mButtonP2.setTypeface(tf);
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

        mDbHelper.open().updateGame(mGame);
    }

    public void displayRecyclerView(boolean enabled) {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mPlayerList.setLayoutManager(mLayoutManager);
        RecyclerView.Adapter bigGameAdapter = new BigGameAdapter(mGame, mDbHelper, enabled, this);
        mPlayerList.setAdapter(bigGameAdapter);
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

    public void timeLimitDialog() {

        mButtonP1.setEnabled(false);
        mButtonP2.setEnabled(false);
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
                    relativeLayout.setVisibility(View.INVISIBLE);
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

                    mDbHelper.open().updateGame(mGame);

                    if (mGame.size() > 2) {
                        displayRecyclerView(true);
                    } else {
                        mP1Score = mPlayersArray.get(0).getmScore();
                        mP2Score = mPlayersArray.get(0).getmScore();

                        mButtonP1.setText(String.valueOf(mP1Score));
                        mButtonP2.setText(String.valueOf(mP2Score));
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

        mDbHelper.open().updateGame(mGame);
    }

    public void chronometerClick() {
        if (mGame.isChecked(OptionID.STOPWATCH)) {
            if (!mPaused) {
                mStopwatch.setBase(SystemClock.elapsedRealtime() + mTimeWhenStopped);
                mStopwatch.start();
                mFabChronometer.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.start)));
                mStopwatch.setTextColor(getResources().getColor(R.color.start));
                mFabChronometer.setImageResource(R.mipmap.ic_play_arrow_white_24dp);
            } else {
                mTimeWhenStopped = mStopwatch.getBase() - SystemClock.elapsedRealtime();
                mStopwatch.stop();
                mFabChronometer.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.stop)));
                mStopwatch.setTextColor(getResources().getColor(R.color.stop));
                mFabChronometer.setImageResource(R.mipmap.ic_pause_white_24dp);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonP1:
                onScoreButtonClick(mButtonP1);

                if (mGame.isChecked(OptionID.STOPWATCH)) {
                    mGame.setmLength(mStopwatch.getText().toString());
                }

                mDbHelper.open().updateGame(mGame);
                break;

            case R.id.buttonP2:
                onScoreButtonClick(mButtonP2);

                if (mGame.isChecked(OptionID.STOPWATCH)) {
                    mGame.setmLength(mStopwatch.getText().toString());
                }

                mDbHelper.open().updateGame(mGame);

                break;

            case R.id.fabChronometer:
                if (!mFinished) {
                    mPaused = !mPaused;
                    chronometerClick();
                }
                break;

            case R.id.fabChronometerBig:
                if (!mFinished) {
                    mPaused = !mPaused;
                    chronometerClick();
                }
                break;

            case R.id.buttonEditP1:
                playerDialog(mGame.getPlayer(0), 0, Dialog.EDIT_PLAYER, 0);
                break;

            case R.id.buttonEditP2:
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
                    mDbHelper.open().updateGame(mGame);

                    startActivity(mHomeIntent);

                }
            });

            builder.setPositiveButton(R.string.complete_game, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    if (mGame.isChecked(OptionID.STOPWATCH)) {
                        mGame.setmLength(mStopwatch.getText().toString());
                    }

                    mGame.setmCompleted(true);

                    mDbHelper.open().updateGame(mGame);

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

    public void winnerDialog(String winner) {

        setFullScreen(false);

        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

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

                    mButtonP1.setEnabled(true);
                    mButtonP2.setEnabled(true);
                    mWon = false;

                    mDbHelper.open().updateGame(mGame);

                    if (mGame.size() == 2) {
                        mP1Score = 0;
                        mP2Score = 0;
                        mButtonP1.setText(String.valueOf(mP1Score));
                        mButtonP2.setText(String.valueOf(mP2Score));
                    } else {
                        displayRecyclerView(true);
                    }

                }
            });

            builder.setNeutralButton(R.string.complete_later, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    if (mGame.isChecked(OptionID.STOPWATCH)) {
                        mGame.setmLength(mStopwatch.getText().toString());
                    }

                    mDbHelper.open().updateGame(mGame);
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

                    mDbHelper.open().updateGame(mGame);

                    startActivity(mHomeIntent);
                }
            });
        }

        mButtonP1.setEnabled(false);
        mButtonP2.setEnabled(false);

        dialog = builder.create();

        dialog.show();

        mFinished = true;
        mPaused = true;
        chronometerClick();

        mWon = true;

    }

    public void onScoreButtonClick(Button button) {

        if (button == mButtonP1) {
            if (mReverseScoring) {
                mP1Score -= mScoreInterval;
            } else {
                mP1Score += mScoreInterval;
            }

            button.setText(String.valueOf(mP1Score));

        } else {

            if (mReverseScoring) {
                mP2Score -= mScoreInterval;
            } else {
                mP2Score += mScoreInterval;
            }

            button.setText(String.valueOf(mP2Score));
        }

        updateScores();
        mGame.setGameListener(this);
        mGame.isGameWon();

    }

    public void onScoreButtonLongClick(Button button) {

        if (button == mButtonP1 && mP1Score != 0) {
            if (mReverseScoring) {
                mP1Score += mScoreInterval;
            } else {
                mP1Score -= mScoreInterval;
            }

            button.setText(String.valueOf(mP1Score));

        } else if (button == mButtonP2 && mP2Score != 0) {
            if (mReverseScoring) {
                mP2Score += mScoreInterval;
            } else {
                mP2Score -= mScoreInterval;
            }

            button.setText(String.valueOf(mP2Score));

        }

        updateScores();
    }

    public void updateScores() {
        mGame.getPlayer(0).setmScore(mP1Score);
        mGame.getPlayer(1).setmScore(mP2Score);

        mDbHelper.open().updateGame(mGame);
    }

    @Override
    public boolean onLongClick(View v) {

        switch (v.getId()) {
            case R.id.buttonP1:
                onScoreButtonLongClick(mButtonP1);
                break;

            case R.id.buttonP2:
                onScoreButtonLongClick(mButtonP2);
                break;
        }

        return true;
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
                                mDbHelper.updateGame(mGame);

                                mTimeLimit = timeLimitString;

                                timeLimitReached();
                                mButtonP1.setEnabled(true);
                                mButtonP2.setEnabled(true);
                                displayRecyclerView(true);
                                mAlertDialog.dismiss();
                                mFabChronometer.setEnabled(true);
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

        mDbHelper.open().updateGame(mGame);
    }

    @Override
    public void onChronometerTick(Stopwatch chronometer) {
        timeLimitReached();
    }

    @Override
    public void onGameWon(String winner) {
        this.mWinnerString = winner;

        if (mGame.size() == 2) {
            updateScores();
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

        mDbHelper.open().updateGame(mGame);

        selectLayout();
        chronometerClick();
    }

    @Override
    public void editPlayer(int position) {
        playerDialog(mGame.getPlayer(position), position, Dialog.EDIT_PLAYER, 0);
    }

    public void playerDialog(final Player player, final int position, final Dialog type, final int setPosition) {
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

                            mDbHelper.open().updateGame(mGame);
                            mDbHelper.close();

                            populateSetGridView();
                            selectLayout();
                            mPaused = true;
                            chronometerClick();

                            mGame.isGameWon();
                        }

                    }

                });
            }

        });
        mAlertDialog.show();
    }

    private void selectLayout() {
        if (mGame.size() > 2) {
            mBigLayout.setVisibility(View.VISIBLE);
            mNormalLayout.setVisibility(View.INVISIBLE);

            mStopwatch = (Stopwatch) findViewById(R.id.chronometerBig);
            mFabChronometer = (FloatingActionButton) findViewById(R.id.fabChronometerBig);
            mFabChronometer.setOnClickListener(this);

            try {
                displayRecyclerView(true);

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.toString());
            }

        } else {

            if (!mClassicTheme) {
                mNormalLayout.setVisibility(View.VISIBLE);
                mBigLayout.setVisibility(View.INVISIBLE);
            }

            mP1Score = mGame.getPlayer(0).getmScore();
            mP2Score = mGame.getPlayer(1).getmScore();

            mButtonP1.setText(String.valueOf(mP1Score));
            mButtonP2.setText(String.valueOf(mP2Score));
            mTextViewP1.setText(String.valueOf(mGame.getPlayer(0).getmName()));
            mTextViewP2.setText(String.valueOf(mGame.getPlayer(1).getmName()));
            mStopwatch = (Stopwatch) findViewById(R.id.chronometer);
            mFabChronometer = (FloatingActionButton) findViewById(R.id.fabChronometer);
            mFabChronometer.setOnClickListener(this);
        }

        if (mGame.isChecked(OptionID.STOPWATCH)) {

            try {

                if (mGame.getmTimeLimit() != null) {
                    mTimeLimit = mGame.getmTimeLimit().getmTime();
                }

                if (mGame.getmLength() == null || mGame.getmLength().equals("") && mGame.isChecked(OptionID.STOPWATCH)) {
                    mGame.setmLength("00:00:00:0");

                } else if (mTimeWhenStopped != 0L) {
                    //when coming back from the app running in the background treat it as unpausing the mStopwatch
                    mStopwatch.setBase(SystemClock.elapsedRealtime() + mTimeWhenStopped);

                } else {
                    mStopwatch.setBase((-(3600000 + mTimeHelper.convertToLong(mGame.getmLength()) - SystemClock.elapsedRealtime())));
                }

                timeLimitReached();

                if (!mFinished) {
                    mStopwatch.start();
                    mFabChronometer.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.start)));
                    mStopwatch.setTextColor(getResources().getColor(R.color.start));
                    mFabChronometer.setImageResource(R.mipmap.ic_play_arrow_white_24dp);
                }

                mDbHelper.open().updateGame(mGame);

            } catch (Exception e) {
                e.printStackTrace();
                Snackbar snackbar;
                snackbar = Snackbar.make(mNormalLayout, "conversion to long error. invalid time type", Snackbar.LENGTH_LONG);
                mFabChronometer.setEnabled(false);
                mButtonP1.setEnabled(false);
                mButtonP2.setEnabled(false);
                snackbar.show();
            }


        } else {
            if (mGame.size() > 2) {
                CardView cardView = (CardView) findViewById(R.id.buttonChronometerBig);
                cardView.setVisibility(View.INVISIBLE);
                mPlayerList.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

            } else {
                CardView cardView = (CardView) findViewById(R.id.buttonChronometer);
                cardView.setVisibility(View.INVISIBLE);
            }
            mStopwatch.setVisibility(View.INVISIBLE);
            mFabChronometer.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onScoreClick(Player player, int position, int setPosition) {
        playerDialog(player, position, Dialog.CHANGE_SET, setPosition);
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
            container.setVisibility(View.INVISIBLE);
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
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.game);
                case 1:
                    return getString(R.string.sets);
                case 2:
                    return getString(R.string.time_line);

            }
            return null;
        }
    }
}

