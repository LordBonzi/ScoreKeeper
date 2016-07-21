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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener, DialogInterface.OnShowListener, Stopwatch.OnChronometerTickListener
                    ,BigGameAdapter.GameListener{

    private boolean isWon = false;
    private String winner;
    private int scoreInterval;
    public static int gameID;
    private Button buttonP1;
    private Button buttonP2;
    private TextView textViewP1, textViewP2;
    private boolean ft1, ft2;
    private int P1Score, P2Score;
    private FloatingActionButton fabChronometer;
    private RecyclerView bigGameList;
    private boolean finished = false;
    String TAG = "MainActivity.class";
    private int gameSize;
    private ArrayList playersArray;
    private ArrayList scoresArray;
    public static DataHelper dataHelper;
    private Intent homeIntent;
    ScoreDBAdapter dbHelper;
    private RecyclerView.Adapter bigGameAdapter;
    private Stopwatch stopwatch;
    private TimeHelper timeHelper;
    private ArrayList<BigGameModel> bigGameModels;
    private BigGameModel gameModel;
    private String timeLimitString = null;
    private boolean classicTheme = false;
    private boolean fullScreen = false;
    private View dialogView;
    private LayoutInflater inflter = null;
    private AlertDialog alertDialog;
    private boolean extend = false;
    private long timeWhenStopped = 0;
    private boolean isPaused = false;
    private MenuItem menuItemDiceNum;
    private SharedPreferences sharedPreferences;
    private int maxNumDice;
    private int maxScore;
    private boolean reverseScrolling;
    private int diffToWin;

    private AlertDialog.Builder builder;

    private static final String STATE_GAMEID = "gameID";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        gameID = extras.getInt("gameID");
        sharedPreferences = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);

        Toolbar toolbar;
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            sharedPreferences = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
            int accentColor = sharedPreferences.getInt("prefAccent", R.style.AppTheme);
            int primaryColor = sharedPreferences.getInt("prefPrimaryColor", getResources().getColor(R.color.primaryIndigo));
            int primaryDarkColor = sharedPreferences.getInt("prefPrimaryDarkColor", getResources().getColor(R.color.primaryIndigoDark));
            classicTheme = sharedPreferences.getBoolean("prefClassicTheme", false);
            maxNumDice = sharedPreferences.getInt("maxNumDice", 6);
            boolean colorNavBar = sharedPreferences.getBoolean("prefColorNavBar", false);
            if (colorNavBar && !classicTheme){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setNavigationBarColor(primaryDarkColor);
                }
            }
            setTheme(accentColor);
            if(classicTheme){
                setContentView(R.layout.activity_main_classic);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(getResources().getColor(R.color.black));
                }
            }else {
                setContentView(R.layout.activity_main);
                toolbar = (Toolbar)findViewById(R.id.toolbar);
                toolbar.setBackgroundColor(primaryColor);
                setSupportActionBar(toolbar);
                getSupportActionBar();
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(primaryDarkColor);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setNavigationBarColor(primaryDarkColor);
                }
            }
            AdView mAdView = (AdView) findViewById(R.id.adViewHome);
            AdCreator adCreator = new AdCreator(mAdView, this);
            adCreator.createAd();

            gameID = savedInstanceState.getInt(STATE_GAMEID);
            loadObjects();
            loadGame();
        } else {
            sharedPreferences = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
            int accentColor = sharedPreferences.getInt("prefAccent", R.style.AppTheme);
            int primaryColor = sharedPreferences.getInt("prefPrimaryColor", getResources().getColor(R.color.primaryIndigo));
            int primaryDarkColor = sharedPreferences.getInt("prefPrimaryDarkColor", getResources().getColor(R.color.primaryIndigoDark));
            classicTheme = sharedPreferences.getBoolean("prefClassicTheme", false);
            maxNumDice = sharedPreferences.getInt("maxNumDice", 6);


            setTheme(accentColor);
            if(classicTheme){
                setContentView(R.layout.activity_main_classic);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(getResources().getColor(R.color.black));
                }
            }else {
                setContentView(R.layout.activity_main);
                toolbar = (Toolbar)findViewById(R.id.toolbar);
                toolbar.setBackgroundColor(primaryColor);
                setSupportActionBar(toolbar);
                getSupportActionBar();
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getWindow().setStatusBarColor(primaryDarkColor);
                }
            }
            loadObjects();
            loadGame();
        }

    }

    public void loadObjects(){
        builder = new AlertDialog.Builder(this);

        dataHelper = new DataHelper();
        timeHelper = new TimeHelper();

        gameModel = new BigGameModel(dbHelper);

        dbHelper = new ScoreDBAdapter(this);
        dbHelper.open();
    }

    public void loadGame(){
        timeLimitString = dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_TIMER, dbHelper);
        maxScore = dataHelper.getIntByID(gameID, ScoreDBAdapter.KEY_MAX_SCORE, dbHelper);
        scoreInterval = dataHelper.getIntByID(gameID, ScoreDBAdapter.KEY_SCORE_INTERVAL, dbHelper);
        diffToWin = dataHelper.getIntByID(gameID, ScoreDBAdapter.KEY_DIFF_TO_WIN, dbHelper);
        if(scoreInterval == 0){
            scoreInterval = 1;
        }if(diffToWin == 0){
            diffToWin = 1;
        }
        int i = dataHelper.getIntByID(gameID, ScoreDBAdapter.KEY_REVERSE_SCORING, dbHelper);
        reverseScrolling = i == 1;

        homeIntent = new Intent(this, Home.class);

        buttonP1 = (Button) findViewById(R.id.buttonP1);
        buttonP1.setOnClickListener(this);
        buttonP1.setOnLongClickListener(this);

        buttonP2 = (Button) findViewById(R.id.buttonP2);
        buttonP2.setOnClickListener(this);
        buttonP2.setOnLongClickListener(this);

        stopwatch = new Stopwatch(this);

        textViewP1 = (TextView) findViewById(R.id.textViewP1);
        textViewP2 = (TextView) findViewById(R.id.textViewP2);

        bigGameList = (RecyclerView) findViewById(R.id.bigGameList);

        RelativeLayout normal = (RelativeLayout) findViewById(R.id.layoutNormal);
        RelativeLayout big = (RelativeLayout) findViewById(R.id.layoutBig);

        playersArray = new ArrayList();
        playersArray = dataHelper.getArrayById(ScoreDBAdapter.KEY_PLAYERS, gameID, dbHelper);

        scoresArray = new ArrayList();
        scoresArray = dataHelper.getArrayById(ScoreDBAdapter.KEY_SCORE, gameID, dbHelper);

        gameSize = playersArray.size();

        if (gameSize > 2) {
            big.setVisibility(View.VISIBLE);

            stopwatch = (Stopwatch) findViewById(R.id.chronometerBig);
            fabChronometer = (FloatingActionButton) findViewById(R.id.fabChronometerBig);
            fabChronometer.setOnClickListener(this);

            try {
                displayRecyclerView(true);

            }catch (Exception e){
                e.printStackTrace();
                Log.e(TAG, e.toString());
            }


        } else {

            normal.setVisibility(View.VISIBLE);
            P1Score = Integer.valueOf(scoresArray.get(0).toString());
            P2Score = Integer.valueOf(scoresArray.get(1).toString());
            ft1 = true;
            ft2 = true;

            buttonP1.setText(String.valueOf(P1Score));
            buttonP2.setText(String.valueOf(P2Score));
            textViewP1.setText(String.valueOf(playersArray.get(0)));
            textViewP2.setText(String.valueOf(playersArray.get(1)));
            stopwatch = (Stopwatch) findViewById(R.id.chronometer);
            fabChronometer = (FloatingActionButton) findViewById(R.id.fabChronometer);
            fabChronometer.setOnClickListener(this);
        }

        try {
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

        } catch (Exception e) {
            e.printStackTrace();
            Snackbar snackbar;
            snackbar = Snackbar.make(normal, "conversion to long error. invalid time type", Snackbar.LENGTH_LONG);
            fabChronometer.setEnabled(false);
            buttonP1.setEnabled(false);
            buttonP2.setEnabled(false);
            snackbar.show();
        }

        if(classicTheme){
            Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/digitalfont.ttf");

            // Applying font
            textViewP1.setTypeface(tf);
            textViewP2.setTypeface(tf);
            stopwatch.setTypeface(tf);
            buttonP1.setTypeface(tf);
            buttonP2.setTypeface(tf);
        }

        stopwatch.setOnChronometerTickListener(this);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("lastplayedgame", gameID);
        editor.apply();

        for (int a = 0; a < scoresArray.size(); a++) {
            if (maxScore < 0) {
                if (Integer.valueOf(String.valueOf(scoresArray.get(a))) <= maxScore
                        && scoreDifference(Integer.valueOf(String.valueOf(scoresArray.get(a))))){

                    gameWon(String.valueOf(playersArray.get(a)));
                }

            } else if (maxScore >= 0) {
                if (Integer.valueOf(String.valueOf(scoresArray.get(a))) >= maxScore
                        && scoreDifference(Integer.valueOf(String.valueOf(scoresArray.get(a))))) {
                    gameWon(String.valueOf(playersArray.get(a)));
                }

            }

        }
    }

    private boolean scoreDifference(int score){
        boolean b = false;
        for (int i = 0; i < scoresArray.size(); i++){
            if (maxScore != 0) {
                if (Math.abs(score - Integer.valueOf(String.valueOf(scoresArray.get(i)))) >= diffToWin) {
                    b = true;
                }
            }
        }
        return  b;
    }

    private boolean timeLimitReached(Stopwatch chronometer){
        boolean b=false;
        if (timeLimitString != null) {
            if (chronometer.getText().toString().equalsIgnoreCase(timeLimitString)) {
                finished = true;
                b = true;
                timeLimitDialog();
            }
        }

        return  b;
    }

    @Override
    protected void onStop() {
        super.onStop();
        chronometerClick();
        dbHelper.open();
        dbHelper.updateGame(null, stopwatch.getText().toString(),0, ScoreDBAdapter.KEY_CHRONOMETER, gameID);
        dbHelper.close();
    }

    public void displayRecyclerView(boolean enabled){
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        scoresArray = dataHelper.getArrayById(ScoreDBAdapter.KEY_SCORE, gameID, dbHelper);
        bigGameList.setLayoutManager(mLayoutManager);
        bigGameModels = BigGameModel.createGameModel(playersArray.size(), playersArray,  scoresArray);

        bigGameAdapter = new BigGameAdapter(bigGameModels, scoresArray, dbHelper, gameID, enabled, maxScore, this, reverseScrolling, scoreInterval, diffToWin);
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
        menuItemDiceNum = menu.findItem(R.id.action_dice_num);
        return true;
    }

    public void timeLimitDialog(){

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
        final CheckBox checkBoxExtend = (CheckBox)dialogView.findViewById(R.id.checkBoxExtend);
        checkBoxExtend.setVisibility(View.VISIBLE);
        final RelativeLayout relativeLayout = (RelativeLayout)dialogView.findViewById(R.id.relativeLayout2);
        editTextHour.setText("0");
        editTextMinute.setText("0");
        editTextSecond.setText("0");

        checkBoxExtend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBoxExtend.isChecked()){
                    relativeLayout.setVisibility(View.VISIBLE);
                    extend = true;
                }else{
                    relativeLayout.setVisibility(View.INVISIBLE);
                    extend = false;
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

                    for (int i = 0; i < scoresArray.size(); i++) {
                        scoresArray.set(i, 0);
                    }

                    if (gameSize > 2) {
                        displayRecyclerView(true);
                    } else {
                        P1Score = Integer.valueOf(scoresArray.get(0).toString());
                        P2Score = Integer.valueOf(scoresArray.get(1).toString());
                        ft1 = true;
                        ft2 = true;

                        buttonP1.setText(String.valueOf(P1Score));
                        buttonP2.setText(String.valueOf(P2Score));                    }

                    stopwatch.setBase(SystemClock.elapsedRealtime());
                    timeWhenStopped = 0;

                    dbHelper.open();
                    dbHelper.updateGame(scoresArray, null,0, ScoreDBAdapter.KEY_SCORE, gameID);
                    dbHelper.updateGame(null, String.valueOf(stopwatch.getTimeElapsed()),0, ScoreDBAdapter.KEY_CHRONOMETER, gameID);
                    dbHelper.close();
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
            fullScreen = !fullScreen;
            fullScreen();

            }

        if (id == R.id.action_dice) {
            if (!menuItemDiceNum.isVisible()){
                menuItemDiceNum.setVisible(true);
                Random rand = new Random();
                int randomNum = rand.nextInt((maxNumDice - 1) + 1) + 1;
                menuItemDiceNum.setTitle(String.valueOf(randomNum));
            }
                Random rand = new Random();
                int randomNum = rand.nextInt((maxNumDice - 1) + 1) + 1;
                menuItemDiceNum.setTitle(String.valueOf(randomNum));
            }


        return super.onOptionsItemSelected(item);
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
        dbHelper.updateGame(null, stopwatch.getText().toString(),0, ScoreDBAdapter.KEY_CHRONOMETER, gameID);
        dbHelper.close();
    }

    public void chronometerClick(){
        if (!isPaused) {
            stopwatch.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
            stopwatch.start();
            fabChronometer.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.start)));
            stopwatch.setTextColor(getResources().getColor(R.color.start));
            fabChronometer.setImageResource(R.mipmap.ic_play_arrow_white_24dp);
        }else {
            timeWhenStopped = stopwatch.getBase() - SystemClock.elapsedRealtime();
            stopwatch.stop();
            fabChronometer.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.stop)));
            stopwatch.setTextColor(getResources().getColor(R.color.stop));
            fabChronometer.setImageResource(R.mipmap.ic_pause_white_24dp);
        }

        }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonP1:
                onScoreButtonClick(buttonP1);
                dbHelper.open();
                dbHelper.updateGame(null, stopwatch.getText().toString(),0, ScoreDBAdapter.KEY_CHRONOMETER, gameID);
                dbHelper.close();
                break;

            case R.id.buttonP2:
                onScoreButtonClick(buttonP2);
                dbHelper.open();
                dbHelper.updateGame(null, stopwatch.getText().toString(),0, ScoreDBAdapter.KEY_CHRONOMETER, gameID);
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

        }

    }

    @Override
    public void onBackPressed() {
        fullScreen = false;
        fullScreen();

        if (!finished) {
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
                    dbHelper.updateGame(null, stopwatch.getText().toString(),0, ScoreDBAdapter.KEY_CHRONOMETER, gameID);
                    dbHelper.updateGame(null, "0",0, ScoreDBAdapter.KEY_COMPLETED, gameID);
                    dbHelper.close();
                    startActivity(homeIntent);

                }
            });

            builder.setPositiveButton(R.string.complete_game, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dbHelper.open();
                    dbHelper.updateGame(null, stopwatch.getText().toString(),0, ScoreDBAdapter.KEY_CHRONOMETER, gameID);
                    dbHelper.updateGame(null, "1",0, ScoreDBAdapter.KEY_COMPLETED, gameID);
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

        }

        if(isWon){
            winnerDialog(winner);
        }
        if (timeLimitReached(stopwatch)){
            timeLimitDialog();
            }
    }

    public void winnerDialog(String winner){
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(winner + " " + getString(R.string.has_won));

        builder.setPositiveButton(R.string.complete_game, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dbHelper.open();
                dbHelper.updateGame(null, stopwatch.getText().toString(),0, ScoreDBAdapter.KEY_CHRONOMETER, gameID);
                dbHelper.updateGame(null, "1",0, ScoreDBAdapter.KEY_COMPLETED, gameID);
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
    }

    public void onScoreButtonClick(Button button){

        if (button == buttonP1) {
            if (reverseScrolling){
                P1Score -= scoreInterval;
            }else {
                P1Score += scoreInterval;
            }
            button.setText(String.valueOf(P1Score));
        }else{
            if (reverseScrolling){
                P2Score -= scoreInterval;
            }else {
                P2Score += scoreInterval;
            }
            button.setText(String.valueOf(P2Score));
        }
        if (button == buttonP1) {
            ft1 = false;
        }else{
            ft2 = false;
        }
        if (maxScore != 0  && Math.abs(P1Score-P2Score) >= diffToWin) {
            if (P1Score >= maxScore || P2Score >= maxScore) {
                finished = true;
                isPaused = true;
                chronometerClick();
                buttonP1.setEnabled(false);
                buttonP2.setEnabled(false);
                if (P1Score == maxScore) {
                    winner = playersArray.get(0).toString();
                } else {
                    winner = playersArray.get(1).toString();

                }
                isWon = true;
                winnerDialog(winner);
            }
        }

        updateScores();
    }
    public void onScoreButtonLongClick(Button button){

        if (button == buttonP1 && !ft1 && P1Score != 0){
            if (reverseScrolling){
                P1Score += scoreInterval;
            }else {
                P1Score -= scoreInterval;
            }

            button.setText(String.valueOf(P1Score));
        }else if (button == buttonP2 && !ft2 && P2Score != 0){
            if (reverseScrolling){
                P2Score += scoreInterval;
            }else {
                P2Score -= scoreInterval;
            }
            button.setText(String.valueOf(P2Score));
        }

        if (button == buttonP1){
            ft1 = false;
        }else {
            ft2 = false;

        }

        updateScores();
    }

    public void updateScores(){
        scoresArray.set(0, String.valueOf(P1Score));
        scoresArray.set(1, String.valueOf(P2Score));

        dbHelper.open();
        dbHelper.updateGame(scoresArray, null,0, ScoreDBAdapter.KEY_SCORE, gameID);
        dbHelper.close();
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
                final CheckBox checkBoxExtend = (CheckBox)dialogView.findViewById(R.id.checkBoxExtend);

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
                        } else if (Integer.valueOf(minute)+ Integer.valueOf(oldMinute) >= 60) {
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
                                    dbHelper.updateGame(null, timeLimitString,0, ScoreDBAdapter.KEY_TIMER, gameID);
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

                }else{
                    dbHelper.open();
                    dbHelper.updateGame(null, "1",0, ScoreDBAdapter.KEY_COMPLETED, gameID);
                    dbHelper.close();
                    startActivity(homeIntent);

                }
            }
        });
    }

    public void fullScreen() {
        if (fullScreen) {
            getSupportActionBar().hide();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {

                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

            }
        }else{
            if (!classicTheme) {
                getSupportActionBar().show();
            }

            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);


        }
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

    }
}

