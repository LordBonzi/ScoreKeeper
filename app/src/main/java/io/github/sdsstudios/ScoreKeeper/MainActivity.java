package io.github.sdsstudios.ScoreKeeper;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener, DialogInterface.OnShowListener, Stopwatch.OnChronometerTickListener{

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
    public static DataHelper dataHelper;
    SmallLayout smallLayout;
    Intent homeIntent;
    ScoreDBAdapter dbHelper;
    private boolean immersive = false;
    private RecyclerView.Adapter bigGameAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Stopwatch stopwatch;
    private TimeHelper timeHelper;
    private ArrayList<BigGameModel> bigGameModels;
    private BigGameModel gameModel;
    private String timeLimitString = null;
    private boolean finished;

    private View dialogView;
    private LayoutInflater inflter = null;
    private AlertDialog alertDialog;

    private boolean extend = false;

    long timeWhenStopped = 0;
    boolean isPaused = false;

    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        builder = new AlertDialog.Builder(this);

        dataHelper = new DataHelper();
        timeHelper = new TimeHelper();


        dbHelper = new ScoreDBAdapter(this);
        dbHelper.open();

        gameModel = new BigGameModel(dbHelper);

        Bundle extras = getIntent().getExtras();
        gameID = extras.getInt("gameID");

        timeLimitString = dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_TIMER, dbHelper);

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
        playersArray = dataHelper.getArrayById(ScoreDBAdapter.KEY_PLAYERS, gameID, dbHelper);

        scoresArray = new ArrayList();
        scoresArray = dataHelper.getArrayById(ScoreDBAdapter.KEY_SCORE, gameID, dbHelper);

        gameSize = playersArray.size();

        smallLayout.onCreate(buttonP1,  buttonP2, dbHelper, gameID, dataHelper);

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
            stopwatch.setBase((-(3600000 + timeHelper.convertToLong(dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_CHRONOMETER,dbHelper)))
                    + SystemClock.elapsedRealtime())) ;

            timeLimitReached(stopwatch);

            if (finished){

            }else{
                stopwatch.start();
                fabChronometer.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.start)));
                stopwatch.setTextColor(getResources().getColor(R.color.start));
                fabChronometer.setImageResource(R.mipmap.ic_play_arrow_white_24dp);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
            FirebaseCrash.report(new Exception(e.toString() + ", time:  " + dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_CHRONOMETER,dbHelper)));
            Snackbar snackbar;
            snackbar = Snackbar.make(normal, "conversion to long error. invalid time type", Snackbar.LENGTH_LONG);
            snackbar.show();
        }

        stopwatch.setOnChronometerTickListener(this);
    }

    private void timeLimitReached(Stopwatch chronometer){
        if (timeLimitString != null) {
            if (chronometer.getText().toString().equalsIgnoreCase(timeLimitString)) {
                timeLimitDialog();
            }
        }

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

        bigGameAdapter = new BigGameAdapter(bigGameModels, scoresArray, dbHelper, gameID, true);
        bigGameList.setAdapter(bigGameAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_about).setVisible(true);
        menu.findItem(R.id.action_reset).setVisible(true);
        menu.findItem(R.id.action_fullscreen).setVisible(true);
        return true;
    }

    public void timeLimitDialog(){
        finished = true;
        isPaused = true;
        fabChronometer.setEnabled(false);
        buttonP1.setEnabled(false);
        buttonP2.setEnabled(false);
        bigGameAdapter = new BigGameAdapter(bigGameModels, scoresArray, dbHelper, gameID, false);
        bigGameList.setAdapter(bigGameAdapter);
        chronometerClick();

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
                isPaused = true;
                chronometerClick();
                dialog.dismiss();
                immersiveMode();
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
                        displayRecyclerView();
                    } else {
                        smallLayout.onCreate(buttonP1, buttonP2, dbHelper, gameID, dataHelper);
                    }

                    stopwatch.setBase(SystemClock.elapsedRealtime());
                    timeWhenStopped = 0;

                    dbHelper.open();
                    dbHelper.updateGame(scoresArray, null, ScoreDBAdapter.KEY_SCORE, gameID);
                    dbHelper.updateGame(null, String.valueOf(stopwatch.getTimeElapsed()), ScoreDBAdapter.KEY_CHRONOMETER, gameID);
                    dbHelper.close();
                }
            });

            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    immersiveMode();
                }
            });

            dialog = builder.create();

            dialog.show();
            return true;
        }
        if (id == R.id.action_fullscreen) {
            Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT);
            immersiveMode();
            }


        return super.onOptionsItemSelected(item);
    }

    public void immersiveMode(){

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void showSystemBars(){
        getSupportActionBar().show();
        Window w = this.getWindow();

        w.setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

        w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        immersive = !immersive;

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void hideSystemBars(){
        getSupportActionBar().hide();
        Window w = this.getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        immersive = !immersive;
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
                dbHelper.open();
                dbHelper.updateGame(null, stopwatch.getText().toString(), ScoreDBAdapter.KEY_CHRONOMETER, gameID);
                dbHelper.close();
                break;

            case R.id.buttonP2:
                smallLayout.onClick(buttonP2, dbHelper, gameID);
                dbHelper.open();
                dbHelper.updateGame(null, stopwatch.getText().toString(), ScoreDBAdapter.KEY_CHRONOMETER, gameID);
                dbHelper.close();
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
        immersiveMode();

        if (fabChronometer.isEnabled()) {
            isPaused = true;
            AlertDialog dialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle(R.string.quit_game);

            builder.setMessage(R.string.quit_game_message);

            builder.setNeutralButton(R.string.complete_later, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dbHelper.open();
                    dbHelper.updateGame(null, stopwatch.getText().toString(), ScoreDBAdapter.KEY_CHRONOMETER, gameID);
                    dbHelper.updateGame(null, "0", ScoreDBAdapter.KEY_COMPLETED, gameID);
                    dbHelper.close();
                    startActivity(homeIntent);
                }
            });

            builder.setPositiveButton(R.string.complete_game, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dbHelper.open();
                    dbHelper.updateGame(null, stopwatch.getText().toString(), ScoreDBAdapter.KEY_CHRONOMETER, gameID);
                    dbHelper.updateGame(null, "1", ScoreDBAdapter.KEY_COMPLETED, gameID);
                    dbHelper.close();
                    startActivity(homeIntent);
                }
            });

            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    immersiveMode();
                }
            });

            dialog = builder.create();
            fabChronometer.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.stop)));
            stopwatch.setTextColor(getResources().getColor(R.color.stop));
            fabChronometer.setImageResource(R.mipmap.ic_pause_white_24dp);
            chronometerClick();
            dialog.show();

        }else{
            timeLimitDialog();
            }
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
                                    dbHelper.updateGame(null, timeLimitString, ScoreDBAdapter.KEY_TIMER, gameID);
                                    dbHelper.close();

                                    isPaused = false;
                                    chronometerClick();
                                    timeLimitReached(stopwatch);
                                    fabChronometer.setEnabled(true);
                                    buttonP1.setEnabled(true);
                                    buttonP2.setEnabled(true);
                                    bigGameAdapter = new BigGameAdapter(bigGameModels, scoresArray, dbHelper, gameID, true);
                                    bigGameList.setAdapter(bigGameAdapter);
                                    alertDialog.dismiss();
                                    immersiveMode();

                                } else {
                                    alertDialog.dismiss();
                                    immersiveMode();
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e(TAG, e.toString());
                                Toast toast = Toast.makeText(getBaseContext(), R.string.invalid_time, Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                    }

                }else{
                    immersiveMode();
                    dbHelper.open();
                    dbHelper.updateGame(null, "1", ScoreDBAdapter.KEY_COMPLETED, gameID);
                    dbHelper.close();
                    startActivity(homeIntent);

                }
            }
        });
    }

    @Override
    public void onChronometerTick(Stopwatch chronometer) {
        timeLimitReached(stopwatch);
    }


}

class SmallLayout{
    public static Integer P1Score , P2Score;
    public static ArrayList scoresArray;
    public static boolean ft1;
    public static boolean ft2;

    public void onCreate(Button b1, Button b2, ScoreDBAdapter dbHelper, int gameID, DataHelper dataHelper){
        scoresArray = dataHelper.getArrayById(ScoreDBAdapter.KEY_SCORE,gameID, dbHelper);
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

        dbHelper.open();
        dbHelper.updateGame(scoresArray, null, ScoreDBAdapter.KEY_SCORE, id);
        dbHelper.close();
    }

}