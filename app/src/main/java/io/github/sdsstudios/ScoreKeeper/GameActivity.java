package io.github.sdsstudios.ScoreKeeper;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import io.github.sdsstudios.ScoreKeeper.Activity.Activity;
import io.github.sdsstudios.ScoreKeeper.Activity.ScoreKeeperTabActivity;
import io.github.sdsstudios.ScoreKeeper.Adapters.BigGameAdapter;
import io.github.sdsstudios.ScoreKeeper.Listeners.ButtonPlayerListener;
import io.github.sdsstudios.ScoreKeeper.Listeners.GameListener;
import io.github.sdsstudios.ScoreKeeper.Options.Option.OptionID;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class GameActivity extends ScoreKeeperTabActivity
        implements View.OnClickListener, DialogInterface.OnShowListener, Stopwatch.OnChronometerTickListener
        , GameListener, ViewTreeObserver.OnGlobalLayoutListener, ButtonPlayerListener, ViewPager.OnPageChangeListener {

    private static final String STATE_GAMEID = "GAME_ID";
    public static int GAME_ID;
    private final int PLAYER_1 = 0;
    private final int PLAYER_2 = 1;
    private final int STOPWATCH_DELAY = 300;
    private TypedValue mTypedValue = new TypedValue();

    private boolean mWon = false;
    private String mWinnerString;
    private RecyclerView mPlayerRecyclerView;
    private boolean mFinished = false;
    private CardView mCardViewStopwatch;
    private Stopwatch mStopwatch;
    private View mDialogView;
    private long mTimeWhenStopped = 0L;
    private boolean mPaused = false;
    private MenuItem mMenuItemDiceNum;
    private RelativeLayout mMainContent;
    private View mNormalLayout;
    private CoordinatorLayout mCoordinatorLayout;
    private ViewGroup.LayoutParams mParams;
    private int mMaxNumDice, mMinNumDice, mStartingScore, mScoreInterval;
    private String mTimeLimit;
    private Random mRandom = new Random();
    private List<ButtonPlayer> mButtonsPlayerList = new ArrayList<>();

    private Handler mPausedHandler = new Handler();
    private Runnable mPausedRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Themes.themeActivity(this, R.layout.activity_main, true);
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        GAME_ID = extras.getInt("GAME_ID");

        mGame = mDataHelper.getGame(GAME_ID, mDbHelper);
        mGame.setGameListener(this);

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

        updateGameInDatabase();
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
    public void chooseTab(int layout) {
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

        mTabLayout.getTabAt(layout).select();

    }

    @Override
    public Activity getActivity() {
        return Activity.GAME_ACTIVITY;
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
        mMainContent = (RelativeLayout) findViewById(R.id.activity_main_content);

        createButtonsPlayerList();

        mCardViewStopwatch = (CardView) findViewById(R.id.stopwatchCardview);
        mCardViewStopwatch.setOnClickListener(this);

        mStopwatch = (Stopwatch) findViewById(R.id.stopwatch);

        mPlayerRecyclerView = (RecyclerView) findViewById(R.id.playerRecyclerView);

        mNormalLayout = findViewById(R.id.layoutNormal);

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

        saveStopwatchTime();

        updateGameInDatabase();
    }

    public void displayRecyclerView(boolean enabled) {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mPlayerRecyclerView.setLayoutManager(mLayoutManager);
        RecyclerView.Adapter bigGameAdapter = new BigGameAdapter(mGame, enabled, this);
        mPlayerRecyclerView.setAdapter(bigGameAdapter);

        mGame.updatePlayerColors();

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

    private void resetGame() {
        mGame.reset();

        saveStopwatchTime();
        updateGameInDatabase();
        chooseTab(GAME_LAYOUT);

        mStopwatch.setBase(SystemClock.elapsedRealtime());
        mTimeWhenStopped = 0L;
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
                    resetGame();
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
                addPlayerDialog();
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

        saveStopwatchTime();

        updateGameInDatabase();
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

        }

    }

    public boolean isFullScreen() {
        return !getSupportActionBar().isShowing();
    }

    private void setFullScreen(boolean fullscreen) {

        if (fullscreen) {
            mParams = mMainContent.getLayoutParams();
            getSupportActionBar().hide();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION /** hide nav bar **/
                                | View.SYSTEM_UI_FLAG_FULLSCREEN /** show navbar and status bar **/
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

                mCoordinatorLayout.setFitsSystemWindows(false);

            }

            CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(
                    CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                    CoordinatorLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(0, mTabLayout.getHeight(), 0, 0);
            mMainContent.setLayoutParams(params);

        } else {

            getSupportActionBar().show();

            boolean colorNavBar = mSharedPreferences.getBoolean("prefColorNavBar", false);
            int primaryDarkColor = mSharedPreferences.getInt("prefPrimaryDarkColor", Themes.DEFAULT_PRIMARY_DARK_COLOR(this));

            if (colorNavBar) {
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
                mMainContent.setLayoutParams(mParams);
            }

            mTabLayout.setBackgroundColor(getResources().getColor(R.color.transparent));

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

                    saveStopwatchTime();

                    mGame.setmCompleted(false);
                    updateGameInDatabase();

                    startActivity(mHomeIntent);

                }
            });

            builder.setPositiveButton(R.string.complete_game, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    saveStopwatchTime();

                    mGame.setmCompleted(true);

                    updateGameInDatabase();

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
                    updateGameInDatabase();

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

                    saveStopwatchTime();

                    updateGameInDatabase();
                    startActivity(mHomeIntent);
                }
            });

        } else {

            builder.setPositiveButton(R.string.complete_game, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    saveStopwatchTime();

                    mGame.setmCompleted(true);

                    updateGameInDatabase();

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

    private void saveStopwatchTime() {
        if (mGame.isChecked(OptionID.STOPWATCH)) {
            mGame.setmLength(mStopwatch.getText().toString());
        }
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
                                updateGameInDatabase();

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

        updateGameInDatabase();
    }

    @Override
    public void onChronometerTick(Stopwatch chronometer) {
        timeLimitReached();
    }

    private void reloadPlayerButtons() {
        if (mButtonsPlayerList.size() == 0) {
            createButtonsPlayerList();
        } else {
            for (int i = 0; i < mGame.size(); i++) {
                mButtonsPlayerList.get(i).reload(mGame.getPlayer(i));
            }
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
        displayRecyclerView(false);

        winnerDialog(winner);

    }

    @Override
    public void deletePlayer(int position) {

        super.deletePlayer(position);

        if (TWO_PLAYER_GAME()) {
            reloadPlayerButtons();
        }

        goToCurrentSelectedTab();
    }

    @Override
    public void onScoreClick(int playerIndex) {
        mGame.onPlayerClick(playerIndex);
        mGame.isGameWon();
    }

    @Override
    public void onScoreLongClick(int playerIndex) {
        mGame.onPlayerLongClick(playerIndex);
        mGame.isGameWon();
    }

    @Override
    public void editPlayer(int position) {
        playerDialog(mGame.getPlayer(position), position, Dialog.EDIT_PLAYER, 0);
    }

    private void showStopwatch(int visible) {
        if (visible == VISIBLE) {
            if (mGame.isChecked(OptionID.STOPWATCH)) {
                mCardViewStopwatch.setVisibility(visible);
            }
        } else {
            mCardViewStopwatch.setVisibility(visible);
        }

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

            mNormalLayout.setVisibility(visible);
            mPlayerRecyclerView.setVisibility(GONE);

            setButtonParams();
            reloadPlayerButtons();

        }

    }

    private void selectLayout() {

        goToCurrentSelectedTab();

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

                updateGameInDatabase();

            } catch (Exception e) {
                e.printStackTrace();
                Snackbar.make(mNormalLayout, "conversion to long error. invalid time type", Snackbar.LENGTH_LONG).show();
                showStopwatch(INVISIBLE);
                enablePlayerButtons(false);
            }

        } else {

            showStopwatch(GONE);

        }

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
    public void playerDialog(Player player, int position, Dialog type, int setPosition) {

        /** pauses stopwatch before opening dialog **/
        mPaused = true;
        chronometerClick();
        saveStopwatchTime();

        super.playerDialog(player, position, type, setPosition);

    }


}

