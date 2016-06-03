package io.github.sdsstudios.ScoreKeeper;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener{

    public static int gameID;
    public static Button buttonP1;
    public static Button buttonP2;
    public static boolean firstTime = true;
    Button buttonChronometer;
    FloatingActionButton fabChronometer;
    TextView textViewP1;
    TextView textViewP2;
    RecyclerView bigGameList;
    String TAG = "MainActivity.class";
    int gameSize;
    RelativeLayout normal, big;
    ArrayList playersArray;
    ArrayList scoresArray;
    CursorHelper cursorHelper;
    SmallLayout smallLayout;
    Intent homeIntent;
    ScoreDBAdapter dbHelper;
    private RecyclerView.Adapter bigGameAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    //savedinstancestate stuff
    static final String STATE_T = "t";
    static final String STATE_SECS = "secs";
    static final String STATE_MINS = "mins";
    static final String STATE_MILLISECS = "millis";
    static final String STATE_P1 = "scoreP1";
    static final String STATE_P2 = "scoreP2";
    static final String STATE_GAMEID = "gameId";
    static final String STATE_FT1 = "ft1";
    static final String STATE_FT2 = "ft2";
    static final String STATE_SCORES = "scores";

    //chronometer
    long starttime;
    long timeInMilliseconds;
    long timeSwapBuff;
    long updatedtime;
    int t ;
    int secs ;
    int mins ;
    int milliseconds ;
    String s;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        cursorHelper = new CursorHelper();

        dbHelper = new ScoreDBAdapter(this);
        dbHelper.open();
        smallLayout = new SmallLayout();

        gameID = Integer.valueOf(dbHelper.getNewestGame());

        homeIntent = new Intent(this, Home.class);

        buttonP1 = (Button) findViewById(R.id.buttonP1);
        buttonP1.setOnClickListener(this);
        buttonP1.setOnLongClickListener(this);

        buttonP2 = (Button) findViewById(R.id.buttonP2);
        buttonP2.setOnClickListener(this);
        buttonP2.setOnLongClickListener(this);

        textViewP1 = (TextView)findViewById(R.id.textViewP1);
        textViewP2 = (TextView)findViewById(R.id.textViewP2);

        bigGameList = (RecyclerView)findViewById(R.id.bigGameList);

        buttonChronometer = (Button) findViewById(R.id.buttonChronometer);
        buttonChronometer.setOnClickListener(this);

        fabChronometer = (FloatingActionButton) findViewById(R.id.fabChronometer);
        fabChronometer.setOnClickListener(this);

        normal = (RelativeLayout)findViewById(R.id.layoutNormal);
        big = (RelativeLayout)findViewById(R.id.layoutBig);

        playersArray = new ArrayList();
        playersArray = cursorHelper.getArrayById(ScoreDBAdapter.KEY_PLAYERS, gameID, dbHelper);

        scoresArray = new ArrayList();
        scoresArray = cursorHelper.getArrayById(ScoreDBAdapter.KEY_SCORE, gameID, dbHelper);

        Log.i(TAG, "player array is " + playersArray);
        Log.i(TAG, "score array is " + scoresArray);

        gameSize = playersArray.size();

        if (savedInstanceState != null) {
            // Restore value of members from saved state
            t = savedInstanceState.getInt(STATE_T);
            SmallLayout.P1Score = savedInstanceState.getInt(STATE_P1);
            SmallLayout.P2Score = savedInstanceState.getInt(STATE_P2);
            gameID = savedInstanceState.getInt(STATE_GAMEID);
            secs = savedInstanceState.getInt(STATE_SECS);
            mins = savedInstanceState.getInt(STATE_MINS);
            milliseconds = savedInstanceState.getInt(STATE_MILLISECS);
            SmallLayout.ft1 = savedInstanceState.getBoolean(STATE_FT1);
            SmallLayout.ft2 = savedInstanceState.getBoolean(STATE_FT2);
            SmallLayout.scoresArray = savedInstanceState.getIntegerArrayList(STATE_SCORES);

        }else {
            smallLayout.onCreate(buttonP1,  buttonP2, dbHelper, gameID);
            starttime = 0L;
            timeInMilliseconds = 0L;
            timeSwapBuff = 0L;
            updatedtime = 0L;
            t = 1;
            secs = 0;
            mins = 0;
            milliseconds = 0;

        }

        if (gameSize > 2) {
            big.setVisibility(View.VISIBLE);
            mLayoutManager = new LinearLayoutManager(this);
            bigGameList.setLayoutManager(mLayoutManager);

            ArrayList<BigGameModel> bigGameModels = BigGameModel.createGameModel(playersArray.size(), playersArray,  scoresArray,  dbHelper);

            bigGameAdapter = new BigGameAdapter(bigGameModels, scoresArray, dbHelper, gameID);
            bigGameList.setAdapter(bigGameAdapter);
        }else{
            normal.setVisibility(View.VISIBLE);
            textViewP1.setText(String.valueOf(playersArray.get(0)));
            textViewP2.setText(String.valueOf(playersArray.get(1)));

        }

        chronometerClick();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_about).setVisible(true);
        return true;
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.updateGame(null, s, ScoreDBAdapter.KEY_CHRONOMETER, gameID);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state

        savedInstanceState.putInt(STATE_GAMEID, gameID);
        savedInstanceState.putInt(STATE_SECS, secs);
        savedInstanceState.putInt(STATE_MINS, mins);
        savedInstanceState.putInt(STATE_MILLISECS, milliseconds);
        savedInstanceState.putInt(STATE_T, t);
        savedInstanceState.putInt(STATE_P1, SmallLayout.P1Score);
        savedInstanceState.putInt(STATE_P2, SmallLayout.P2Score);
        savedInstanceState.putBoolean(STATE_FT1, SmallLayout.ft1);
        savedInstanceState.putBoolean(STATE_FT2, SmallLayout.ft2);
        savedInstanceState.putIntegerArrayList(STATE_SCORES, SmallLayout.scoresArray);
        dbHelper.updateGame(null, s, ScoreDBAdapter.KEY_CHRONOMETER, gameID);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public void chronometerClick(){
        if (t == 1) {
            starttime = SystemClock.uptimeMillis();
            handler.postDelayed(updateTimer, 0);
            fabChronometer.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.start)));
            buttonChronometer.setTextColor(getResources().getColor(R.color.start));
            fabChronometer.setImageResource(R.mipmap.ic_play_arrow_white_24dp);

            t = 0;
        } else {
            timeSwapBuff += timeInMilliseconds;
            handler.removeCallbacks(updateTimer);
            fabChronometer.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.stop)));
            buttonChronometer.setTextColor(getResources().getColor(R.color.stop));
            fabChronometer.setImageResource(R.mipmap.ic_pause_white_24dp);

            t = 1;

        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.buttonP1:
                smallLayout.onClick(buttonP1, dbHelper, gameID);
                break;

            case R.id.buttonP2:
                smallLayout.onClick(buttonP2, dbHelper, gameID);
                break;

            case R.id.fabChronometer:
                chronometerClick();
                break;
        }
    }

    public Runnable updateTimer = new Runnable() {

        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - starttime;

            updatedtime = timeSwapBuff + timeInMilliseconds;

            secs = (int) (updatedtime / 1000);
            mins = secs / 60;
            secs = secs % 60;
            milliseconds = (int) (updatedtime % 1000);

            s = "" + mins + ":" + String.format("%02d", secs) + ":"
                    + String.format("%03d", milliseconds);

            buttonChronometer.setText(s);
            handler.postDelayed(this, 0);

        }

    };

    @Override
    public void onBackPressed() {
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.quit_game);

        builder.setMessage(R.string.quit_game_message);

        builder.setNeutralButton(R.string.complete_later, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dbHelper.updateGame(null, s, ScoreDBAdapter.KEY_CHRONOMETER, gameID);
                dbHelper.updateGame(null, "0", ScoreDBAdapter.KEY_COMPLETED, gameID);
                startActivity(homeIntent);
            }
        });

        builder.setPositiveButton(R.string.complete_game, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dbHelper.updateGame(null, "1", ScoreDBAdapter.KEY_COMPLETED, gameID);
                dbHelper.updateGame(null, s, ScoreDBAdapter.KEY_CHRONOMETER, gameID);
                startActivity(homeIntent);
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                chronometerClick();
            }
        });

        dialog = builder.create();
        chronometerClick();
        dialog.show();

    }

    @Override
    public boolean onLongClick(View v) {

        switch (v.getId()) {
            case R.id.buttonP1:
                smallLayout.onLongClick(buttonP1, dbHelper, gameID);
                break;

            case R.id.buttonP2:
                smallLayout.onLongClick(buttonP2, dbHelper, gameID);

                break;
        }

        return true;
    }
}

class SmallLayout extends Activity{
    public static Integer P1Score , P2Score;
    public static ArrayList scoresArray;
    public static boolean ft1;
    public static boolean ft2;


    public void onCreate(Button b1, Button b2, ScoreDBAdapter dbHelper, int id){
        scoresArray = new ArrayList();
        P1Score = 0;
        P2Score = 0;
        ft1 = MainActivity.firstTime;
        ft2 = MainActivity.firstTime;
        scoresArray.add(0, String.valueOf(P1Score));
        scoresArray.add(1, String.valueOf(P2Score));
        updateScores(dbHelper, id);

        b1.setText(String.valueOf(P1Score));
        b2.setText(String.valueOf(P2Score));
    }

    public void onClick(Button button, ScoreDBAdapter dbHelper, int id){

        if (button == MainActivity.buttonP1){
            P1Score += 1;
            button.setText(String.valueOf(P1Score));
        }else {
            P2Score += 1;
            button.setText(String.valueOf(P2Score));
        }

        if (button == MainActivity.buttonP1){
            ft1 = false;
        }else {
            ft2 = false;

        }

        updateScores(dbHelper, id);
    }
    public void onLongClick(Button button, ScoreDBAdapter dbHelper, int id){

        if (button == MainActivity.buttonP1 && !ft1 && P1Score != 0){
            P1Score -= 1;
            button.setText(String.valueOf(P1Score));
        }else if (button == MainActivity.buttonP2 && !ft2 && P2Score != 0){
            P2Score -= 1;
            button.setText(String.valueOf(P2Score));
        }

        if (button == MainActivity.buttonP1){
            ft1 = false;
        }else {
            ft2 = false;

        }

        updateScores(dbHelper, id);
    }

    public void updateScores(ScoreDBAdapter dbHelper, int id){
        scoresArray.set(0, String.valueOf(P1Score));
        scoresArray.set(1, String.valueOf(P2Score));

        dbHelper.updateGame(scoresArray, null, ScoreDBAdapter.KEY_SCORE, id);
    }

}
