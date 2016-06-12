package io.github.sdsstudios.ScoreKeeper;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener{

    public static int gameID;
    public static Button buttonP1;
    public static Button buttonP2;
    public static boolean firstTime = true;
    FloatingActionButton fabChronometer;
    TextView textViewP1;
    TextView textViewP2;
    RecyclerView bigGameList;
    String TAG = "MainActivity.class";
    private String time;
    int gameSize;
    RelativeLayout normal, big;
    ArrayList playersArray;
    ArrayList scoresArray;
    public static CursorHelper cursorHelper;
    SmallLayout smallLayout;
    Intent homeIntent;
    ScoreDBAdapter dbHelper;
    private RecyclerView.Adapter bigGameAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Stopwatch stopwatch;
    private TimeHelper timeHelper;
    private ArrayList<BigGameModel> bigGameModels;
    private BigGameModel gameModel;

    long timeWhenStopped = 0;
    boolean isPaused = false;
    long milliseconds ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        cursorHelper = new CursorHelper();
        timeHelper = new TimeHelper();

        dbHelper = new ScoreDBAdapter(this);
        dbHelper.open();

        gameModel = new BigGameModel(dbHelper);

        Bundle extras = getIntent().getExtras();
        gameID = extras.getInt("gameID");

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        time = sdfDate.format(now);
        dbHelper.updateGame(null, time, ScoreDBAdapter.KEY_TIME, gameID);

        smallLayout = new SmallLayout();

        homeIntent = new Intent(this, Home.class);

        buttonP1 = (Button) findViewById(R.id.buttonP1);
        buttonP1.setOnClickListener(this);
        buttonP1.setOnLongClickListener(this);

        buttonP2 = (Button) findViewById(R.id.buttonP2);
        buttonP2.setOnClickListener(this);
        buttonP2.setOnLongClickListener(this);

        stopwatch = new Stopwatch(this);

        textViewP1 = (TextView)findViewById(R.id.textViewP1);
        textViewP2 = (TextView)findViewById(R.id.textViewP2);

        bigGameList = (RecyclerView)findViewById(R.id.bigGameList);

        normal = (RelativeLayout)findViewById(R.id.layoutNormal);
        big = (RelativeLayout)findViewById(R.id.layoutBig);

        playersArray = new ArrayList();
        playersArray = cursorHelper.getArrayById(ScoreDBAdapter.KEY_PLAYERS, gameID, dbHelper);

        scoresArray = new ArrayList();
        scoresArray = cursorHelper.getArrayById(ScoreDBAdapter.KEY_SCORE, gameID, dbHelper);

        gameSize = playersArray.size();

        smallLayout.onCreate(buttonP1,  buttonP2, dbHelper, gameID, cursorHelper);

        if (gameSize > 2) {
            big.setVisibility(View.VISIBLE);

            stopwatch = (Stopwatch) findViewById(R.id.chronometerBig);
            fabChronometer = (FloatingActionButton) findViewById(R.id.fabChronometerBig);
            fabChronometer.setOnClickListener(this);

            displayRecyclerView();

        }else{
            normal.setVisibility(View.VISIBLE);

            textViewP1.setText(String.valueOf(playersArray.get(0)));
            textViewP2.setText(String.valueOf(playersArray.get(1)));
            stopwatch = (Stopwatch) findViewById(R.id.chronometer);
            fabChronometer = (FloatingActionButton) findViewById(R.id.fabChronometer);
            fabChronometer.setOnClickListener(this);

        }

        try {
            stopwatch.setBase((-(3600000 + timeHelper.convertToLong(cursorHelper.getStringById(gameID, ScoreDBAdapter.KEY_CHRONOMETER,dbHelper)))
                    + SystemClock.elapsedRealtime())) ;

            stopwatch.start();
            fabChronometer.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.start)));
            stopwatch.setTextColor(getResources().getColor(R.color.start));
            fabChronometer.setImageResource(R.mipmap.ic_play_arrow_white_24dp);
            Log.e("long",  " "+timeHelper.convertToLong(cursorHelper.getStringById(gameID, ScoreDBAdapter.KEY_CHRONOMETER,dbHelper)));
        } catch (ParseException e) {
            e.printStackTrace();
            Snackbar snackbar;
            snackbar = Snackbar.make(normal, "conversion to long error. invalid time type", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
        Log.e("base ", "base: " + stopwatch.getBase()) ;
        Log.e("systemclock ", "" + SystemClock.elapsedRealtime()) ;

    }


    @Override
    protected void onStop() {
        super.onStop();
        chronometerClick();
    }


    public void displayRecyclerView(){
        mLayoutManager = new LinearLayoutManager(this);
        bigGameList.setLayoutManager(mLayoutManager);
        bigGameModels = BigGameModel.createGameModel(playersArray.size(), playersArray,  scoresArray);

        bigGameAdapter = new BigGameAdapter(bigGameModels, scoresArray, dbHelper, gameID);
        bigGameList.setAdapter(bigGameAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_about).setVisible(true);
        menu.findItem(R.id.action_reset).setVisible(true);
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
        }if (id == R.id.action_reset) {
            isPaused = true;

            AlertDialog dialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle(R.string.reset_game_question);

            builder.setMessage(R.string.reset_game_message);

            builder.setPositiveButton(R.string.reset, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    for (int i = 0; i < scoresArray.size(); i++){
                        scoresArray.set(i, 0);
                    }

                    if (gameSize > 2){
                        displayRecyclerView();
                    }else{
                        smallLayout.onCreate(buttonP1, buttonP2, dbHelper, gameID, cursorHelper);
                    }

                    stopwatch.setBase(SystemClock.elapsedRealtime());
                    timeWhenStopped = 0;

                    dbHelper.updateGame(scoresArray, null, ScoreDBAdapter.KEY_SCORE, gameID);
                    dbHelper.updateGame(null, String.valueOf(stopwatch.getTimeElapsed()), ScoreDBAdapter.KEY_CHRONOMETER, gameID);
                }
            });

            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    isPaused = false;
                    chronometerClick();
                }
            });

            dialog = builder.create();
            fabChronometer.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.stop)));
            stopwatch.setTextColor(getResources().getColor(R.color.stop));
            fabChronometer.setImageResource(R.mipmap.ic_pause_white_24dp);

            dialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        dbHelper.open();

    }
    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
        chronometerClick();
        dbHelper.updateGame(null, stopwatch.getText().toString(), ScoreDBAdapter.KEY_CHRONOMETER, gameID);
        gameModel.closeDB();
        dbHelper.close();
    }

    public void chronometerClick(){
        if (!isPaused) {
            stopwatch.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
            stopwatch.start();
            fabChronometer.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.start)));
            stopwatch.setTextColor(getResources().getColor(R.color.start));
            fabChronometer.setImageResource(R.mipmap.ic_play_arrow_white_24dp);
        }else{
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
                smallLayout.onClick(buttonP1, dbHelper, gameID);
                dbHelper.updateGame(null, stopwatch.getText().toString(), ScoreDBAdapter.KEY_CHRONOMETER, gameID);
                break;

            case R.id.buttonP2:
                smallLayout.onClick(buttonP2, dbHelper, gameID);
                dbHelper.updateGame(null, stopwatch.getText().toString(), ScoreDBAdapter.KEY_CHRONOMETER, gameID);
                break;

            case R.id.fabChronometer:
                isPaused = !isPaused;
                chronometerClick();
                break;

            case R.id.fabChronometerBig:
                isPaused = !isPaused;
                chronometerClick();
                break;
        }
    }


    @Override
    public void onBackPressed() {
        isPaused = true;
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.quit_game);

        builder.setMessage(R.string.quit_game_message);

        builder.setNeutralButton(R.string.complete_later, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dbHelper.updateGame(null, stopwatch.getText().toString(), ScoreDBAdapter.KEY_CHRONOMETER, gameID);
                dbHelper.updateGame(null, "0", ScoreDBAdapter.KEY_COMPLETED, gameID);
                startActivity(homeIntent);
            }
        });

        builder.setPositiveButton(R.string.complete_game, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dbHelper.updateGame(null, "1", ScoreDBAdapter.KEY_COMPLETED, gameID);
                dbHelper.updateGame(null, String.valueOf(stopwatch.getTimeElapsed()), ScoreDBAdapter.KEY_CHRONOMETER, gameID);
                finish();
                startActivity(homeIntent);
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                isPaused = false;
                chronometerClick();
            }
        });

        dialog = builder.create();
        fabChronometer.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.stop)));
        stopwatch.setTextColor(getResources().getColor(R.color.stop));
        fabChronometer.setImageResource(R.mipmap.ic_pause_white_24dp);
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

    public void onCreate(Button b1, Button b2, ScoreDBAdapter dbHelper, int gameID, CursorHelper cursorHelper){
        scoresArray = cursorHelper.getArrayById(ScoreDBAdapter.KEY_SCORE,gameID, dbHelper);
        P1Score = Integer.valueOf(scoresArray.get(0).toString());
        P2Score = Integer.valueOf(scoresArray.get(1).toString());
        ft1 = MainActivity.firstTime;
        ft2 = MainActivity.firstTime;

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
