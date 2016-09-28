package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
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
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener, DialogInterface.OnShowListener, Stopwatch.OnChronometerTickListener
        , BigGameAdapter.GameListener {

    private boolean isWon = false;
    private String winner;
    private int scoreInterval;
    public static int gameID;
    private Button buttonP1;
    private Button buttonP2;
    private TextView textViewP1, textViewP2;
    private ImageButton buttonEditP1, buttonEditP2;
    private boolean ft1, ft2;
    private int P1Score, P2Score;
    private FloatingActionButton fabChronometer;
    private RecyclerView bigGameList;
    private boolean finished = false;
    private String TAG = "MainActivity.class";
    private int gameSize;
    private List<Player> mPlayersArray;
    public static DataHelper dataHelper;
    private Intent homeIntent;
    ScoreDBAdapter dbHelper;
    private RecyclerView.Adapter bigGameAdapter;
    private Stopwatch stopwatch;
    private TimeHelper timeHelper;
    private String timeLimitString = null;
    private boolean classicTheme = false;
    private View dialogView;
    private LayoutInflater inflter = null;
    private AlertDialog alertDialog;
    private long timeWhenStopped = 0;
    private boolean isPaused = false;
    private MenuItem menuItemDiceNum;
    private SharedPreferences sharedPreferences;
    private int maxNumDice;
    private int maxScore;
    private boolean reverseScoring;
    private int diffToWin;
    private RecyclerView.LayoutManager mLayoutManager;
    private RelativeLayout content, big, normal;
    private CoordinatorLayout coordinatorLayout;
    private int contentTopMargin;
    private Player newPlayer = null;
    private boolean stopwatchBoolean = false;
    private ViewGroup.LayoutParams params;
    private AlertDialog.Builder builder;
    private SetAdapter mSetAdapter;
    private int mNumSets;
    private static final String STATE_GAMEID = "gameID";
    private TabLayout mTabLayout;

    private GridView mSetGridView;

    //tabsStuff
    private MainActivity.SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar;

        Bundle extras = getIntent().getExtras();
        gameID = extras.getInt("gameID");
        sharedPreferences = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
// Restore value of members from saved state
        sharedPreferences = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
        int accentColor = sharedPreferences.getInt("prefAccent", R.style.AppTheme);
        int primaryColor = sharedPreferences.getInt("prefPrimaryColor", getResources().getColor(R.color.primaryIndigo));
        int primaryDarkColor = sharedPreferences.getInt("prefPrimaryDarkColor", getResources().getColor(R.color.primaryIndigoDark));
        dataHelper = new DataHelper();
        dbHelper = new ScoreDBAdapter(this).open();
        classicTheme = sharedPreferences.getBoolean("prefClassicTheme", false) && dataHelper.getArrayById(ScoreDBAdapter.KEY_PLAYERS, gameID, dbHelper).size() == 2;
        maxNumDice = sharedPreferences.getInt("maxNumDice", 6);
        boolean colorNavBar = sharedPreferences.getBoolean("prefColorNavBar", false);

        setTheme(accentColor);
        if (classicTheme) {
            setContentView(R.layout.activity_main_classic);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(getResources().getColor(R.color.black));
            }

        } else {

            setContentView(R.layout.activity_main);
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setBackgroundColor(primaryColor);
            setSupportActionBar(toolbar);
            getSupportActionBar();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(primaryDarkColor);
            }

            if (colorNavBar && !classicTheme) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setNavigationBarColor(primaryDarkColor);
                }
            }
        }

        AdView mAdView;
        if (gameSize > 2) {
            mAdView = (AdView) findViewById(R.id.adViewHome2);
        }else{
            mAdView = (AdView) findViewById(R.id.adViewHome);
        }

        AdCreator adCreator = new AdCreator(mAdView, this);
        adCreator.createAd();

        loadObjects();
        loadGame();

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        String time = sdfDate.format(now);
        dbHelper.open();
        dbHelper.updateGame(time,0, ScoreDBAdapter.KEY_TIME, gameID);
        dbHelper.close();

        if (savedInstanceState != null) {
            gameID = savedInstanceState.getInt(STATE_GAMEID);
        }

    }

    public void populateSetGridView(){
        mSetGridView.setNumColumns(mPlayersArray.size());
        SetGridViewAdapter setGridViewAdapter = new SetGridViewAdapter(mPlayersArray, this);
        mSetGridView.setAdapter(setGridViewAdapter);
    }

    public void loadObjects() {
        builder = new AlertDialog.Builder(this);

        dataHelper = new DataHelper();
        timeHelper = new TimeHelper();

        dbHelper = new ScoreDBAdapter(this);
        dbHelper.open();
    }

    public void loadGame() {
        timeLimitString = dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_TIMER, dbHelper);
        maxScore = dataHelper.getIntByID(gameID, ScoreDBAdapter.KEY_MAX_SCORE, dbHelper);
        scoreInterval = dataHelper.getIntByID(gameID, ScoreDBAdapter.KEY_SCORE_INTERVAL, dbHelper);
        diffToWin = dataHelper.getIntByID(gameID, ScoreDBAdapter.KEY_DIFF_TO_WIN, dbHelper);

        if (dataHelper.getIntByID(gameID, ScoreDBAdapter.KEY_STOPWATCH, dbHelper) ==1){
            stopwatchBoolean = true;
        }
        if (scoreInterval == 0) {
            scoreInterval = 1;
        }

        if (diffToWin == 0) {
            diffToWin = 1;
        }

        int i = dataHelper.getIntByID(gameID, ScoreDBAdapter.KEY_REVERSE_SCORING, dbHelper);
        reverseScoring = i == 1;

        homeIntent = new Intent(this, Home.class);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        content = (RelativeLayout) findViewById(R.id.content);

        buttonP1 = (Button) findViewById(R.id.buttonP1);
        buttonP1.setOnClickListener(this);
        buttonP1.setOnLongClickListener(this);

        buttonP2 = (Button) findViewById(R.id.buttonP2);
        buttonP2.setOnClickListener(this);
        buttonP2.setOnLongClickListener(this);

        if (!classicTheme) {
            buttonEditP1 = (ImageButton) findViewById(R.id.buttonEditP1);
            buttonEditP1.setOnClickListener(this);

            buttonEditP2 = (ImageButton) findViewById(R.id.buttonEditP2);
            buttonEditP2.setOnClickListener(this);
        }

        stopwatch = new Stopwatch(this);

        textViewP1 = (TextView) findViewById(R.id.textViewP1);
        textViewP2 = (TextView) findViewById(R.id.textViewP2);

        mSetGridView = (GridView)findViewById(R.id.setGridView);

        mPlayersArray = new ArrayList();
        mPlayersArray = dataHelper.getPlayerArray( gameID, dbHelper);

        mNumSets = dataHelper.getIntByID(gameID, ScoreDBAdapter.KEY_NUM_SETS, dbHelper);

        gameSize = mPlayersArray.size();

        if (!classicTheme) {
            bigGameList = (RecyclerView) findViewById(R.id.bigGameList);

            normal = (RelativeLayout) findViewById(R.id.layoutNormal);
            big = (RelativeLayout) findViewById(R.id.layoutBig);

            mSectionsPagerAdapter = new MainActivity.SectionsPagerAdapter(getSupportFragmentManager());

            // Set up the ViewPager with the sections adapter.
            mViewPager = (ViewPager) findViewById(R.id.container);
            mViewPager.setAdapter(mSectionsPagerAdapter);

            mTabLayout = (TabLayout) findViewById(R.id.tabs);
            mTabLayout.setupWithViewPager(mViewPager);

            mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {

                    switch (position){
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
                            Toast.makeText(MainActivity.this, "Coming Soon ;)", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });


        }else{
            Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/digitalfont.ttf");

            // Applying font
            textViewP1.setTypeface(tf);
            textViewP2.setTypeface(tf);
            stopwatch.setTypeface(tf);
            buttonP1.setTypeface(tf);
            buttonP2.setTypeface(tf);
        }

        selectLayout();

        stopwatch.setOnChronometerTickListener(this);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("lastplayedgame", gameID);
        editor.apply();

        for (Player p : mPlayersArray) {
            if (maxScore < 0) {
                if (p.getmScore() <= maxScore
                        && scoreDifference(p.getmScore())) {

                    gameWon(p.getmName());
                }

            } else if (maxScore >= 0) {
                if (p.getmScore() >= maxScore
                        && scoreDifference(p.getmScore())) {
                    gameWon(p.getmName());
                }

            }

        }


    }

    private int numSetsPlayed(){
        int num = 0;
        for (Player p:mPlayersArray){
            num += p.getmSetScores().size();
        }

        return num;
    }

    private boolean scoreDifference(int score) {
        boolean b = false;
        for (Player p : mPlayersArray) {
            if (maxScore != 0) {
                if (Math.abs(score - p.getmScore()) >= diffToWin) {
                    b = true;
                }
            }
        }
        return b;
    }

    private boolean timeLimitReached(Stopwatch chronometer) {
        boolean b = false;
        if (timeLimitString != null) {
            if (chronometer.getText().toString().equalsIgnoreCase(timeLimitString)) {
                finished = true;
                b = true;
                timeLimitDialog();
            }
        }

        return b;
    }

    @Override
    protected void onStop() {
        super.onStop();
        chronometerClick();
        dbHelper.open();

        if (stopwatchBoolean) {
            dbHelper.updateGame(stopwatch.getText().toString(), 0, ScoreDBAdapter.KEY_CHRONOMETER, gameID);
        }
        dbHelper.close();
    }

    public void displayRecyclerView(boolean enabled) {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        bigGameList.setLayoutManager(mLayoutManager);

        bigGameAdapter = new BigGameAdapter(mPlayersArray, dbHelper, gameID, enabled, maxScore, this, reverseScoring, scoreInterval, diffToWin);
        bigGameList.setAdapter(bigGameAdapter);
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
        menuItemDiceNum = menu.findItem(R.id.action_dice_num);
        return true;
    }

    public void timeLimitDialog() {

        buttonP1.setEnabled(false);
        buttonP2.setEnabled(false);
        displayRecyclerView(false);

        if (!isPaused) {
            isPaused = true;
            chronometerClick();
        }

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        dialogView = inflater.inflate(R.layout.create_time_limit, null);

        final EditText editTextHour = (EditText) dialogView.findViewById(R.id.editTextHour);
        final EditText editTextMinute = (EditText) dialogView.findViewById(R.id.editTextMinute);
        final EditText editTextSecond = (EditText) dialogView.findViewById(R.id.editTextSeconds);
        final CheckBox checkBoxExtend = (CheckBox) dialogView.findViewById(R.id.checkBoxExtend);
        checkBoxExtend.setVisibility(View.VISIBLE);
        final RelativeLayout relativeLayout = (RelativeLayout) dialogView.findViewById(R.id.relativeLayout2);
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

        dialogBuilder.setView(dialogView);
        alertDialog = dialogBuilder.create();
        alertDialog.setOnShowListener(this);

        alertDialog.show();
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
            isPaused = true;
            chronometerClick();

            AlertDialog dialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle(R.string.reset_game_question);

            builder.setMessage(R.string.reset_game_message);

            builder.setPositiveButton(R.string.reset, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    for (Player p : mPlayersArray) {
                        p.setmScore(0);
                    }

                    if (stopwatchBoolean) {
                        dbHelper.updateGame(stopwatch.getText().toString(), 0, ScoreDBAdapter.KEY_CHRONOMETER, gameID);
                    }
                    dbHelper.close();
                    if (gameSize > 2) {
                        displayRecyclerView(true);
                    } else {
                        P1Score = mPlayersArray.get(0).getmScore();
                        P2Score = mPlayersArray.get(0).getmScore();
                        ft1 = true;
                        ft2 = true;

                        buttonP1.setText(String.valueOf(P1Score));
                        buttonP2.setText(String.valueOf(P2Score));
                    }

                    stopwatch.setBase(SystemClock.elapsedRealtime());
                    timeWhenStopped = 0;


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
            toggleFullScreen(true);

        }

        if (id == R.id.action_dice) {
            if (!menuItemDiceNum.isVisible()) {
                menuItemDiceNum.setVisible(true);
                Random rand = new Random();
                int randomNum = rand.nextInt((maxNumDice - 1) + 1) + 1;
                menuItemDiceNum.setTitle(String.valueOf(randomNum));
            }
            Random rand = new Random();
            int randomNum = rand.nextInt((maxNumDice - 1) + 1) + 1;
            menuItemDiceNum.setTitle(String.valueOf(randomNum));
        }

        if (id == R.id.action_add) {
            addPlayerDialog();
        }


        return super.onOptionsItemSelected(item);
    }



    public void addPlayerDialog() {
        isPaused = true;
        chronometerClick();
        final View dialogView;

        LayoutInflater inflter = LayoutInflater.from(this);
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogView = inflter.inflate(R.layout.create_preset_fragment, null);
        final EditText editTextPresetTitle = (EditText) dialogView.findViewById(R.id.editTextPresetTitle);
        editTextPresetTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                addPlayer(alertDialog);

                return false;
            }
        });
        dialogBuilder.setPositiveButton(R.string.create, null);

        dialogBuilder.setTitle(getResources().getString(R.string.add_player));

        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialogBuilder.setView(dialogView);

        alertDialog = dialogBuilder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                editTextPresetTitle.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        newPlayer = new Player(charSequence.toString(), 0, new ArrayList<Integer>());

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                });

                Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        addPlayer(alertDialog);

                    }
                });
            }

        });
        alertDialog.show();

    }

    private void addPlayer(AlertDialog alertDialog){
        mPlayersArray.add(newPlayer);
        if (dataHelper.checkPlayerDuplicates(mPlayersArray)){
            mPlayersArray.remove(mPlayersArray.size()-1);
            dbHelper.open().updatePlayers(mPlayersArray, gameID);
            dbHelper.close();
            Toast.makeText(this, R.string.duplicates_message, Toast.LENGTH_SHORT).show();

        }else {

            mPlayersArray.get(mPlayersArray.size() - 1).createNewSet(mPlayersArray.get(0).getmSetScores().size());

            dbHelper.open().updatePlayers(mPlayersArray, gameID);

            alertDialog.dismiss();

            if (stopwatchBoolean) {
                dbHelper.open().updateGame(stopwatch.getText().toString(), 0, ScoreDBAdapter.KEY_CHRONOMETER, gameID);
            }

            dbHelper.close();
            gameSize = mPlayersArray.size();
            selectLayout();
            isPaused = true;
            chronometerClick();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putInt(STATE_GAMEID, gameID);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
        chronometerClick();
        dbHelper.open();

        if (stopwatchBoolean) {
            dbHelper.updateGame(stopwatch.getText().toString(), 0, ScoreDBAdapter.KEY_CHRONOMETER, gameID);
        }
        dbHelper.close();
    }

    public void chronometerClick() {
        if (stopwatchBoolean) {
            if (!isPaused) {
                stopwatch.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                stopwatch.start();
                fabChronometer.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.start)));
                stopwatch.setTextColor(getResources().getColor(R.color.start));
                fabChronometer.setImageResource(R.mipmap.ic_play_arrow_white_24dp);
            } else {
                timeWhenStopped = stopwatch.getBase() - SystemClock.elapsedRealtime();
                stopwatch.stop();
                fabChronometer.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.stop)));
                stopwatch.setTextColor(getResources().getColor(R.color.stop));
                fabChronometer.setImageResource(R.mipmap.ic_pause_white_24dp);
            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonP1:
                onScoreButtonClick(buttonP1);
                dbHelper.open();

                if (stopwatchBoolean) {
                    dbHelper.updateGame(stopwatch.getText().toString(), 0, ScoreDBAdapter.KEY_CHRONOMETER, gameID);
                }
                dbHelper.close();
                break;

            case R.id.buttonP2:
                onScoreButtonClick(buttonP2);
                dbHelper.open();

                if (stopwatchBoolean) {
                    dbHelper.updateGame(stopwatch.getText().toString(), 0, ScoreDBAdapter.KEY_CHRONOMETER, gameID);
                }
                dbHelper.close();
                break;

            case R.id.fabChronometer:
                if (!finished) {
                    isPaused = !isPaused;
                    chronometerClick();
                }
                break;

            case R.id.fabChronometerBig:
                if (!finished) {
                    isPaused = !isPaused;
                    chronometerClick();
                }
                break;

            case R.id.buttonEditP1:
                editPlayerDialog(0);
                break;

            case R.id.buttonEditP2:
                editPlayerDialog(1);

                break;

        }

    }

    public boolean isFullScreen(){
        return !getSupportActionBar().isShowing();
    }

    @Override
    public void onBackPressed() {

        if (!finished && !isFullScreen()) {
            if (!isPaused) {
                isPaused = true;
                chronometerClick();
            }
            AlertDialog dialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle(R.string.quit_game);

            builder.setMessage(R.string.quit_game_message);



            builder.setNeutralButton(R.string.complete_later, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dbHelper.open();
                    if (stopwatchBoolean) {
                        dbHelper.updateGame(stopwatch.getText().toString(), 0, ScoreDBAdapter.KEY_CHRONOMETER, gameID);
                    }
                    dbHelper.updateGame("0", 0, ScoreDBAdapter.KEY_COMPLETED, gameID);
                    dbHelper.close();
                    startActivity(homeIntent);

                }
            });

            builder.setPositiveButton(R.string.complete_game, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dbHelper.open();
                    if (stopwatchBoolean) {
                        dbHelper.updateGame(stopwatch.getText().toString(), 0, ScoreDBAdapter.KEY_CHRONOMETER, gameID);
                    }

                    dbHelper.updateGame("1", 0, ScoreDBAdapter.KEY_COMPLETED, gameID);
                    dbHelper.close();
                    startActivity(homeIntent);
                }
            });

            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });

            dialog = builder.create();

            dialog.show();

        }else if (isWon && !isFullScreen()) {
            winnerDialog(winner);
        }else if (timeLimitReached(stopwatch) && !isFullScreen()) {
            timeLimitDialog();
        }else{
            toggleFullScreen(false);

        }
    }

    public void winnerDialog(String winner) {

        toggleFullScreen(false);

        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(winner + " " + getString(R.string.has_won));

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        if (mNumSets > 1 && numSetsPlayed() / mPlayersArray.size() < mNumSets) {
            for (int i = 0; i < mPlayersArray.size(); i++){
                Player p = mPlayersArray.get(i);
            }

            dbHelper.open().updatePlayers(mPlayersArray, gameID);

            builder.setPositiveButton(R.string.new_set, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finished = false;
                    isPaused = false;
                    chronometerClick();
                    buttonP1.setEnabled(true);
                    buttonP2.setEnabled(true);
                    isWon = false;

                    for (Player p : mPlayersArray){
                        p.addSet(p.getmScore());
                        p.setmScore(0);
                    }

                    dbHelper.open().updatePlayers(mPlayersArray, gameID);

                    if (mPlayersArray.size() == 2){
                        P1Score = 0;
                        P2Score = 0;
                        buttonP1.setText(String.valueOf(P1Score));
                        buttonP2.setText(String.valueOf(P2Score));
                    }else{
                        displayRecyclerView(true);
                    }

                }
            });

            builder.setNeutralButton(R.string.complete_game, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (stopwatchBoolean) {
                        dbHelper.open().updateGame(stopwatch.getText().toString(), 0, ScoreDBAdapter.KEY_CHRONOMETER, gameID);
                    }
                    dbHelper.open().updateGame("1", 0, ScoreDBAdapter.KEY_COMPLETED, gameID);
                    dbHelper.close();
                    startActivity(homeIntent);
                }
            });

        }else{

            builder.setPositiveButton(R.string.complete_game, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dbHelper.open();
                    if (stopwatchBoolean) {
                        dbHelper.open().updateGame(stopwatch.getText().toString(), 0, ScoreDBAdapter.KEY_CHRONOMETER, gameID);
                    }
                    dbHelper.updateGame("1", 0, ScoreDBAdapter.KEY_COMPLETED, gameID);
                    dbHelper.close();
                    startActivity(homeIntent);
                }
            });
        }

        dialog = builder.create();

        dialog.show();

        finished = true;
        isPaused = true;
        chronometerClick();
        buttonP1.setEnabled(false);
        buttonP2.setEnabled(false);
        isWon = true;

    }

    public void onScoreButtonClick(Button button) {

        if (button == buttonP1) {
            if (reverseScoring) {
                P1Score -= scoreInterval;
            } else {
                P1Score += scoreInterval;
            }
            button.setText(String.valueOf(P1Score));
        } else {
            if (reverseScoring) {
                P2Score -= scoreInterval;
            } else {
                P2Score += scoreInterval;
            }
            button.setText(String.valueOf(P2Score));
        }
        if (button == buttonP1) {
            ft1 = false;
        } else {
            ft2 = false;
        }

        if (maxScore != 0 && Math.abs(P1Score - P2Score) >= diffToWin) {
            if (P1Score >= maxScore || P2Score >= maxScore) {
                if (P1Score == maxScore) {
                    winner = mPlayersArray.get(0).getmName();
                } else {
                    winner = mPlayersArray.get(1).getmName();

                }
                isWon = true;
                updateScores();
                winnerDialog(winner);
            }
        }

        updateScores();
    }

    public void onScoreButtonLongClick(Button button) {

        if (button == buttonP1 && !ft1 && P1Score != 0) {
            if (reverseScoring) {
                P1Score += scoreInterval;
            } else {
                P1Score -= scoreInterval;
            }

            button.setText(String.valueOf(P1Score));
        } else if (button == buttonP2 && !ft2 && P2Score != 0) {
            if (reverseScoring) {
                P2Score += scoreInterval;
            } else {
                P2Score -= scoreInterval;
            }
            button.setText(String.valueOf(P2Score));
        }

        if (button == buttonP1) {
            ft1 = false;
        } else {
            ft2 = false;

        }

        updateScores();
    }

    public void updateScores() {
        mPlayersArray.get(0).setmScore(P1Score);
        mPlayersArray.get(1).setmScore(P2Score);

        dbHelper.open().updatePlayers(mPlayersArray, gameID);
    }

    @Override
    public boolean onLongClick(View v) {

        switch (v.getId()) {
            case R.id.buttonP1:
                onScoreButtonLongClick(buttonP1);
                break;

            case R.id.buttonP2:
                onScoreButtonLongClick(buttonP2);
                break;
        }

        return true;
    }

    @Override
    public void onShow(final DialogInterface dialogInterface) {
        Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                final CheckBox checkBoxExtend = (CheckBox) dialogView.findViewById(R.id.checkBoxExtend);

                if (checkBoxExtend.isChecked()) {
                    final EditText editTextHour = (EditText) dialogView.findViewById(R.id.editTextHour);
                    final EditText editTextMinute = (EditText) dialogView.findViewById(R.id.editTextMinute);
                    final EditText editTextSecond = (EditText) dialogView.findViewById(R.id.editTextSeconds);
                    String hour = editTextHour.getText().toString().trim();
                    String minute = editTextMinute.getText().toString().trim();
                    String seconds = editTextSecond.getText().toString().trim();

                    String oldTimeLimit = stopwatch.getText().toString();
                    String[] timeLimitSplit = oldTimeLimit.split(":");

                    String oldHour = timeLimitSplit[0];
                    String oldMinute = timeLimitSplit[1];
                    String oldSeconds = timeLimitSplit[2];

                    timeLimitString = "";

                    if (TextUtils.isEmpty(hour)) {
                        editTextHour.setError("Can't be empty");
                        return;
                    } else if (TextUtils.isEmpty(minute)) {
                        editTextMinute.setError("Can't be empty");
                        return;
                    } else if (TextUtils.isEmpty(seconds)) {
                        editTextSecond.setError("Can't be empty");
                        return;
                    } else {

                        if (Integer.valueOf(hour) + Integer.valueOf(oldHour) >= 24) {
                            editTextHour.setError("Hour must be less than " + String.valueOf(24 - Integer.valueOf(oldHour)));
                        } else if (Integer.valueOf(minute) + Integer.valueOf(oldMinute) >= 60) {
                            editTextMinute.setError("Minute must be less than " + String.valueOf(60 - Integer.valueOf(oldMinute)));

                        } else if (Integer.valueOf(seconds) + Integer.valueOf(oldSeconds) >= 60) {
                            editTextSecond.setError("Seconds must be less than " + String.valueOf(60 - Integer.valueOf(oldSeconds)));

                        } else {
                            hour = String.valueOf(Integer.valueOf(hour) + Integer.valueOf(oldHour));
                            minute = String.valueOf(Integer.valueOf(minute) + Integer.valueOf(oldMinute));
                            seconds = String.valueOf(Integer.valueOf(seconds) + Integer.valueOf(oldSeconds));

                            try {
                                if (hour.length() == 1 && !hour.equals("0")) {
                                    hour = ("0" + hour);

                                }
                                if (minute.length() == 1 && !minute.equals("0")) {
                                    minute = ("0" + minute);
                                }
                                if (seconds.length() == 1 && !seconds.equals("0")) {
                                    seconds = ("0" + seconds);
                                }

                                if (hour.equals("0")) {
                                    hour = "00";
                                }

                                if (minute.equals("0")) {
                                    minute = "00";
                                }

                                if (seconds.equals("0")) {
                                    seconds = "00";
                                }

                                timeLimitString += hour + ":";
                                timeLimitString += minute + ":";
                                timeLimitString += seconds + ":";
                                timeLimitString += "0";

                                if (!timeLimitString.equals("00:00:00:0")) {

                                    dbHelper.open();
                                    dbHelper.updateGame(timeLimitString, 0, ScoreDBAdapter.KEY_TIMER, gameID);
                                    dbHelper.close();

                                    timeLimitReached(stopwatch);
                                    buttonP1.setEnabled(true);
                                    buttonP2.setEnabled(true);
                                    displayRecyclerView(true);
                                    alertDialog.dismiss();
                                    fabChronometer.setEnabled(true);
                                    finished = false;

                                } else {
                                    finished = true;
                                    alertDialog.dismiss();
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast toast = Toast.makeText(getBaseContext(), R.string.invalid_time, Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                    }

                } else {
                    dbHelper.open();
                    dbHelper.updateGame("1", 0, ScoreDBAdapter.KEY_COMPLETED, gameID);
                    dbHelper.close();
                    startActivity(homeIntent);

                }
            }
        });
    }

    public void toggleFullScreen(boolean fullscreen) {

        if (fullscreen) {
            params = content.getLayoutParams();
            getSupportActionBar().hide();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

                coordinatorLayout.setFitsSystemWindows(false);

            }

            CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(
                    CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                    CoordinatorLayout.LayoutParams.WRAP_CONTENT
            );

            params.setMargins(0, mTabLayout.getHeight(), 0, 0);
            content.setLayoutParams(params);

        } else {
            if (!classicTheme) {
                getSupportActionBar().show();

                boolean colorNavBar = sharedPreferences.getBoolean("prefColorNavBar", false);
                int primaryDarkColor = sharedPreferences.getInt("prefPrimaryDarkColor", getResources().getColor(R.color.primaryIndigoDark));

                if (colorNavBar && !classicTheme) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().setNavigationBarColor(primaryDarkColor);
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

                    coordinatorLayout.setFitsSystemWindows(true);
                }

                if (params != null) {
                    content.setLayoutParams(params);
                }

                mTabLayout.setBackgroundColor(getResources().getColor(R.color.transparent));

            }
        }

        Log.e(TAG, String.valueOf(getSupportActionBar().isShowing()));
    }

    @Override
    public void onChronometerTick(Stopwatch chronometer) {
        timeLimitReached(stopwatch);
    }

    @Override
    public void gameWon(String winner) {
        this.winner = winner;
        finished = true;
        isPaused = true;
        chronometerClick();
        isWon = true;
        winnerDialog(winner);
        displayRecyclerView(false);

        Log.e(TAG, "GameWon()");

    }

    @Override
    public void deletePlayer(int position) {
        isPaused = true;
        chronometerClick();
        if (mPlayersArray.size() > 2) {
            mPlayersArray.remove(position);

        } else {
            Toast.makeText(this, R.string.more_than_two_players, Toast.LENGTH_SHORT).show();
        }

        dbHelper.open().updatePlayers(mPlayersArray, gameID);

        if (stopwatchBoolean) {
            dbHelper.open().updateGame(stopwatch.getText().toString(), 0, ScoreDBAdapter.KEY_CHRONOMETER, gameID);
            dbHelper.close();
        }
        gameSize = mPlayersArray.size();
        selectLayout();
        chronometerClick();
    }

    @Override
    public void editPlayer(int position) {
        editPlayerDialog(position);
    }

    public void editPlayerDialog(final int position){
        isPaused = true;
        chronometerClick();
        final View dialogView;

        LayoutInflater inflter = LayoutInflater.from(this);
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogView = inflter.inflate(R.layout.edit_player_fragment, null);
        final EditText editTextPlayer = (EditText) dialogView.findViewById(R.id.editTextPlayer);
        final EditText editTextScore = (EditText) dialogView.findViewById(R.id.editTextScore);
        editTextPlayer.setHint(String.valueOf(mPlayersArray.get(position).getmName()));
        editTextScore.setHint(String.valueOf(mPlayersArray.get(position).getmScore()));

        dialogBuilder.setPositiveButton(R.string.done, null);

        dialogBuilder.setTitle(getResources().getString(R.string.edit_player));

        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialogBuilder.setView(dialogView);

        alertDialog = dialogBuilder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                editTextPlayer.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        newPlayer = new Player(charSequence.toString(), 0, new ArrayList<Integer>());

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                });

                editTextScore.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        newPlayer.setmScore(Integer.valueOf(charSequence.toString()));
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                });

                Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        Player oldPlayer = mPlayersArray.get(position);

                        if (newPlayer == null || newPlayer.equals("")) {
                            newPlayer = oldPlayer;
                        }
                        mPlayersArray.set(position, newPlayer);
                        if (dataHelper.checkPlayerDuplicates(mPlayersArray)) {
                            mPlayersArray.set(position, oldPlayer);
                            dbHelper.open().updatePlayers(mPlayersArray, gameID);
                            dbHelper.close();
                            Toast.makeText(MainActivity.this, R.string.duplicates_message, Toast.LENGTH_SHORT).show();
                        } else {

                            dbHelper.open().updatePlayers(mPlayersArray, gameID);
                            dbHelper.close();
                            alertDialog.dismiss();

                            if (stopwatchBoolean) {
                                dbHelper.open().updateGame(stopwatch.getText().toString(), 0, ScoreDBAdapter.KEY_CHRONOMETER, gameID);
                            }

                            dbHelper.close();
                            gameSize = mPlayersArray.size();
                            selectLayout();
                            isPaused = true;
                            chronometerClick();
                        }

                    }

                });
            }

        });
        alertDialog.show();
    }
    private void selectLayout(){
        if (gameSize > 2) {
            big.setVisibility(View.VISIBLE);
            normal.setVisibility(View.INVISIBLE);

            stopwatch = (Stopwatch) findViewById(R.id.chronometerBig);
            fabChronometer = (FloatingActionButton) findViewById(R.id.fabChronometerBig);
            fabChronometer.setOnClickListener(this);

            try {
                displayRecyclerView(true);

            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.toString());
            }

        } else {

            if (!classicTheme) {
                normal.setVisibility(View.VISIBLE);
                big.setVisibility(View.INVISIBLE);
            }
            P1Score = mPlayersArray.get(0).getmScore();
            P2Score = mPlayersArray.get(1).getmScore();
            ft1 = true;
            ft2 = true;

            buttonP1.setText(String.valueOf(P1Score));
            buttonP2.setText(String.valueOf(P2Score));
            textViewP1.setText(String.valueOf(mPlayersArray.get(0).getmName()));
            textViewP2.setText(String.valueOf(mPlayersArray.get(1).getmName()));
            stopwatch = (Stopwatch) findViewById(R.id.chronometer);
            fabChronometer = (FloatingActionButton) findViewById(R.id.fabChronometer);
            fabChronometer.setOnClickListener(this);
        }

        if (stopwatchBoolean) {
            try {
                if (dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_CHRONOMETER, dbHelper) == null
                        || dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_CHRONOMETER, dbHelper).equals("") && stopwatchBoolean) {
                    dbHelper.updateGame("00:00:00:0", 0, ScoreDBAdapter.KEY_CHRONOMETER, gameID);
                    dbHelper.close();
                }

                if (dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_CHRONOMETER, dbHelper) != null) {
                    stopwatch.setBase((-(3600000 + timeHelper.convertToLong(dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_CHRONOMETER, dbHelper)))
                            + SystemClock.elapsedRealtime()));
                }

                timeLimitReached(stopwatch);

                if (finished) {

                } else {
                    stopwatch.start();
                    fabChronometer.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.start)));
                    stopwatch.setTextColor(getResources().getColor(R.color.start));
                    fabChronometer.setImageResource(R.mipmap.ic_play_arrow_white_24dp);
                }

            }catch(Exception e){
                e.printStackTrace();
                Snackbar snackbar;
                snackbar = Snackbar.make(normal, "conversion to long error. invalid time type", Snackbar.LENGTH_LONG);
                fabChronometer.setEnabled(false);
                buttonP1.setEnabled(false);
                buttonP2.setEnabled(false);
                snackbar.show();
            }


        }else{
            if (gameSize > 2) {
                CardView cardView = (CardView)findViewById(R.id.buttonChronometerBig);
                cardView.setVisibility(View.INVISIBLE);
                bigGameList.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

            }else{
                CardView cardView = (CardView)findViewById(R.id.buttonChronometer);
                cardView.setVisibility(View.INVISIBLE);
            }
            stopwatch.setVisibility(View.INVISIBLE);
            fabChronometer.setVisibility(View.INVISIBLE);
        }
    }

    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */

        public static MainActivity.PlaceholderFragment newInstance(int sectionNumber) {
            MainActivity.PlaceholderFragment fragment = new MainActivity.PlaceholderFragment();
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
            return MainActivity.PlaceholderFragment.newInstance(position + 1);
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

