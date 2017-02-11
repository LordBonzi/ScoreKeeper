package io.github.sdsstudios.ScoreKeeper;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
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
import io.github.sdsstudios.ScoreKeeper.Options.Option;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static io.github.sdsstudios.ScoreKeeper.Options.Option.NOTES;

public class GameActivity extends ScoreKeeperTabActivity
        implements View.OnClickListener, Stopwatch.OnChronometerTickListener
        , GameListener, ViewTreeObserver.OnGlobalLayoutListener, ButtonPlayerListener, ViewPager.OnPageChangeListener {

    private static final String STATE_GAMEID = "GAME_ID";
    private static int GAME_ID;

    private final int PLAYER_1 = 0;
    private final int PLAYER_2 = 1;
    private final int STOPWATCH_DELAY = 300;
    private TypedValue mTypedValue = new TypedValue();

    private boolean mFinished = false;
    private boolean mPaused = false;
    private boolean mWon = false;

    private String mWinnerString;
    private RecyclerView mPlayerRecyclerView;
    private CardView mCardViewStopwatch;
    private Stopwatch mStopwatch;
    private View mDialogView;
    private long mTimeWhenStopped = 0L;
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

        game = dataHelper.getGame(GAME_ID, gameDBAdapter);
        game.setGameListener(this);

        AdView mAdView = (AdView) findViewById(R.id.adViewHome);

        AdCreator adCreator = new AdCreator(mAdView, this);
        adCreator.createAd();

        loadGame();

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String time = sdfDate.format(now);
        game.setmTime(time);

        if (savedInstanceState != null) {
            GAME_ID = savedInstanceState.getInt(STATE_GAMEID);
        }

        saveGameToDatabase();
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

        mParams = mMainContent.getLayoutParams();

    }

    @Override
    public void chooseTab(int layout) {
        if (layout == GAME_LAYOUT) {
            showPlayerLayout(VISIBLE);

            if (getOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
                showStopwatch(VISIBLE);
            }

            setGridView.setVisibility(INVISIBLE);
        } else {
            showPlayerLayout(INVISIBLE);

            if (getOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
                showStopwatch(INVISIBLE);
            }

            setGridView.setVisibility(View.VISIBLE);
        }

        tabLayout.getTabAt(layout).select();

    }

    @Override
    public Activity getActivity() {
        return Activity.GAME_ACTIVITY;
    }

    private boolean TWO_PLAYER_GAME() {
        return game.size() == 2;
    }

    private void createButtonsPlayerList() {
        if (TWO_PLAYER_GAME()) {
            mButtonsPlayerList = ButtonPlayer.createButtonPlayerList(game, this, this);
            mButtonsPlayerList.get(0).getmButton().getViewTreeObserver().addOnGlobalLayoutListener(this);
        }
    }

    private void loadGame() {

        int mScoreDiffToWin = game.getInt(Option.SCORE_DIFF_TO_WIN);
        mScoreInterval = game.getInt(Option.SCORE_INTERVAL);
        mStartingScore = game.getInt(Option.STARTING_SCORE);
        mMaxNumDice = game.getInt(Option.DICE_MAX);
        mMinNumDice = game.getInt(Option.DICE_MIN);

        if (mScoreInterval == 0) {
            mScoreInterval = 1;
        }

        if (mScoreDiffToWin == 0) {
            mScoreDiffToWin = 1;
        }

        homeIntent = new Intent(this, Home.class);

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

        game.setGameListener(this);
        game.isGameWon();

        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (!sharedPreferences.getBoolean("completed_tutorial", false)) {
            new MaterialShowcaseView.Builder(this)
                    .setTarget(findViewById(R.id.buttonP1))
                    .setDismissText("GOT IT")
                    .setContentText(getString(R.string.player_button_tutorial))
                    .singleUse("buttonP1")
                    .show();

            editor.putBoolean("completed_tutorial", true);

        }

        editor.putInt("lastplayedgame", GAME_ID);
        editor.apply();

    }

    private boolean timeLimitReached() {
        boolean b = false;

        if (game.getmTimeLimit() != null) {

            if (mStopwatch.getText().equals(mTimeLimit)) {

                if (!mFinished) {
                    timeLimitDialog();
                }

                game.setmCompleted(true);
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
        saveGameToDatabase();
    }

    public void displayRecyclerView(boolean enabled) {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mPlayerRecyclerView.setLayoutManager(mLayoutManager);
        RecyclerView.Adapter bigGameAdapter = new BigGameAdapter(game, enabled, this);
        mPlayerRecyclerView.setAdapter(bigGameAdapter);

        game.updatePlayerColors();

    }

    @Override
    protected boolean inEditableMode() {
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_reset).setVisible(true);
        menu.findItem(R.id.action_notes).setVisible(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            menu.findItem(R.id.action_fullscreen).setVisible(true);
        }

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

    private void notesDialog() {
        pauseStopwatch();

        mDialogView = layoutInflater.inflate(R.layout.edit_text_fragment, null);

        final EditText editText = (EditText) mDialogView.findViewById(R.id.editText);

        editText.setText(game.getString(NOTES));
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                game.setString(NOTES, s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        showCustomAlertDialog(getString(R.string.notes), null, getString(R.string.done), null, null, null, mDialogView);

    }

    private void timeLimitDialog() {

        enablePlayerButtons(false);
        displayRecyclerView(false);

        pauseStopwatch();

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

        DialogInterface.OnClickListener positiveClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final CheckBox checkBoxExtend = (CheckBox) mDialogView.findViewById(R.id.checkBoxExtend);

                if (checkBoxExtend.isChecked()) {
                    try {

                        String timeLimitString = TimeLimit.updateTimeLimit(mDialogView, game.getmTimeLimit().getmTime());

                        if (timeLimitString != null) {

                            if (!timeLimitString.equals("00:00:00:0")) {

                                game.setmTimeLimit(new TimeLimit(dataHelper.createTimeLimitCondensed(timeLimitString), timeLimitString));
                                saveGameToDatabase();

                                mTimeLimit = timeLimitString;

                                timeLimitReached();
                                enablePlayerButtons(true);
                                displayRecyclerView(true);
                                dialog.dismiss();
                                mCardViewStopwatch.setEnabled(true);
                                mFinished = false;

                            } else {
                                mFinished = true;
                                dialog.dismiss();
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast toast = Toast.makeText(getBaseContext(), R.string.invalid_length, Toast.LENGTH_SHORT);
                        toast.show();
                    }

                } else {
                    game.setmCompleted(true);
                    startActivity(homeIntent);

                }
            }
        };

        showCustomAlertDialog(getString(R.string.time_limit_reached), getString(R.string.time_limit_question)
                , getString(R.string.done), positiveClickListener, getString(R.string.cancel), dismissDialogListener, mDialogView);

        saveGameToDatabase();

    }

    private void resetGame() {
        game.reset();

        /** if the user resets the game after it has completed, unlock the buttons **/
        mFinished = false;

        saveStopwatchTime();
        saveGameToDatabase();
        chooseTab(GAME_LAYOUT);

        mStopwatch.setBase(SystemClock.elapsedRealtime());
        mTimeWhenStopped = 0L;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_reset:
                pauseStopwatch();

                showAlertDialog(getString(R.string.reset_game_question), getString(R.string.reset_game_message), getString(R.string.reset),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                resetGame();
                            }
                        }, getString(R.string.cancel), dismissDialogListener);

                break;

            case R.id.action_fullscreen:
                setFullScreen(true);
                break;

            case R.id.action_dice:
                if (!mMenuItemDiceNum.isVisible()) {
                    mMenuItemDiceNum.setVisible(true);
                }

                int randomNum = mRandom.nextInt((mMaxNumDice - mMinNumDice) + 1) + mMinNumDice;
                mMenuItemDiceNum.setTitle(String.valueOf(randomNum));
                break;

            case R.id.action_add:
                if (!mFinished) {
                    addPlayerDialog();
                } else {
                    Toast.makeText(this, "Game has completed, you can't add more players", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.action_notes:
                notesDialog();
                break;
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

        pauseStopwatch();

        saveGameToDatabase();
    }

    private void chronometerClick() {
        if (game.isChecked(Option.STOPWATCH)) {
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
                } else {
                    Toast.makeText(this, "The game has ended", Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    public boolean isFullScreen() {
        return !getSupportActionBar().isShowing();
    }

    private void setFullScreen(boolean fullscreen) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

            View fullScreenTabs = null;
            int oldSelectedTab = tabLayout.getSelectedTabPosition();

            if (getOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
                fullScreenTabs = findViewById(R.id.tabs_fullscreen);
            }

            if (fullscreen) {

                CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(
                        CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                        CoordinatorLayout.LayoutParams.WRAP_CONTENT
                );

                params.setMargins(0, tabLayout.getHeight(), 0, 0);

                mCoordinatorLayout.setFitsSystemWindows(false);

                hideSystemWindows();

                getSupportActionBar().hide();

                /** use tabs which arent nested in toolbar when full screen. the toolbar will be hidden **/
                if (getOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
                    fullScreenTabs.setVisibility(VISIBLE);
                    tabLayout = (TabLayout) fullScreenTabs;
                    loadTabs();
                }

                mMainContent.setLayoutParams(params);

            } else {

                if (getOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
                    fullScreenTabs.setVisibility(GONE);
                    tabLayout = (TabLayout) findViewById(R.id.tabs);
                    loadTabs();
                }

                mCoordinatorLayout.setFitsSystemWindows(true);

                showSystemWindows();

                if (mParams != null) {
                    mMainContent.setLayoutParams(mParams);
                }

                getSupportActionBar().show();

                tabLayout.setBackgroundColor(getResources().getColor(R.color.transparent));

            }

            /** choose correct tab after changing which view is tabLayout **/
            chooseTab(oldSelectedTab);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showSystemWindows() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void hideSystemWindows() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION /** hide nav bar **/
                        | View.SYSTEM_UI_FLAG_FULLSCREEN /** hide status bar **/
        );

    }

    @Override
    public void onBackPressed() {

        if (!mFinished && !isFullScreen()) {
            if (!mPaused) {
                pauseStopwatch();
            }

            showAlertDialog(getString(R.string.quit_game), getString(R.string.quit_game_message)
                    , getString(R.string.complete_game), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            saveStopwatchTime();

                            game.setmCompleted(true);

                            saveGameToDatabase();

                            startActivity(homeIntent);
                        }
                    }, getString(R.string.complete_later), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            saveStopwatchTime();

                            game.setmCompleted(false);
                            saveGameToDatabase();

                            startActivity(homeIntent);

                        }
                    }, getString(R.string.cancel), dismissDialogListener);


        } else if (mWon && !isFullScreen()) {

            winnerDialog(mWinnerString);

        } else if (timeLimitReached() && !isFullScreen()) {

            timeLimitDialog();

        } else {
            setFullScreen(false);

        }
    }

    private void winnerDialog(String winner) {

        AlertDialog dialog;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (game.numSetsPlayed() == game.numSets()) {

            builder.setTitle(winner + " " + getString(R.string.has_won) + " overall");

        } else {

            builder.setTitle(winner + " " + getString(R.string.has_won));

        }

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                onDialogClose();

            }
        });

        builder.setNegativeButton(R.string.cancel, dismissDialogListener);

        if (game.numSets() > 1 && game.numSetsPlayed() < game.numSets()) {

            builder.setPositiveButton(R.string.new_set, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int id) {

                    game.startNewSet();

                    mFinished = false;
                    mPaused = false;

                    chronometerClick();
                    enablePlayerButtons(true);
                    mWon = false;
                    saveGameToDatabase();

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

                    saveGameToDatabase();
                    startActivity(homeIntent);
                }
            });

        } else {

            builder.setPositiveButton(R.string.complete_game, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    saveStopwatchTime();

                    game.setmCompleted(true);

                    saveGameToDatabase();

                    startActivity(homeIntent);
                }
            });
        }

        enablePlayerButtons(false);
        displayRecyclerView(false);

        dialog = builder.create();

        dialog.show();

        mFinished = true;
        mWon = true;

    }

    private void saveStopwatchTime() {
        if (game.isChecked(Option.STOPWATCH)) {
            game.setmLength(mStopwatch.getText().toString());
        }
    }

    @Override
    public void onChronometerTick(Stopwatch chronometer) {
        timeLimitReached();
    }

    private void reloadPlayerButtons() {
        if (mButtonsPlayerList.size() == 0) {
            createButtonsPlayerList();
        } else {
            for (int i = 0; i < game.size(); i++) {
                mButtonsPlayerList.get(i).reload(game.getPlayer(i));
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
        pauseStopwatch();
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
        game.onPlayerClick(playerIndex);
        game.isGameWon();
    }

    @Override
    public void onScoreLongClick(int playerIndex) {
        game.onPlayerLongClick(playerIndex);
        game.isGameWon();
    }

    @Override
    public void editPlayer(int position) {
        playerDialog(game.getPlayer(position), position, Dialog.EDIT_PLAYER, 0);
    }

    private void showStopwatch(int visible) {
        if (visible == VISIBLE) {
            if (game.isChecked(Option.STOPWATCH)) {
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
                displayRecyclerView(!mFinished);

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

        if (game.isChecked(Option.STOPWATCH)) {

            try {

                if (game.getmTimeLimit() != null) {
                    mTimeLimit = game.getmTimeLimit().getmTime();
                }

                if (game.getmLength() == null || game.getmLength().equals("") && game.isChecked(Option.STOPWATCH)) {
                    game.setmLength("00:00:00:0");

                } else if (mTimeWhenStopped != 0L) {
                    /** when coming back from the app running in the background treat it as unpausing the mStopwatch **/
                    mStopwatch.setBase(SystemClock.elapsedRealtime() + mTimeWhenStopped);

                } else {
                    mStopwatch.setBase((-(3600000 + timeHelper.convertToLong(game.getmLength()) - SystemClock.elapsedRealtime())));
                }

                timeLimitReached();

                if (!mFinished) {
                    mStopwatch.start();
                    mStopwatch.setTextColor(getResources().getColor(R.color.start));
                }

                saveGameToDatabase();

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
    public void onDialogClose() {
        if (isFullScreen()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                hideSystemWindows();
            }
        }
    }

    private void pauseStopwatch() {
        if (!mPaused) {
            mPaused = true;
            chronometerClick();
            saveStopwatchTime();
        }
    }

    @Override
    public void playerDialog(Player player, int position, Dialog type, int setPosition) {

        /** pauses stopwatch before opening dialog **/
        pauseStopwatch();

        super.playerDialog(player, position, type, setPosition);

    }


}

