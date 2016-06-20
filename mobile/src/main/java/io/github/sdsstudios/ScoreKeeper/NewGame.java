package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NewGame extends AppCompatActivity
        implements View.OnClickListener, AdapterView.OnItemSelectedListener, DialogInterface.OnShowListener{

    public static CoordinatorLayout newGameCoordinatorLayout;
    public static PlayerListAdapter playerListAdapter;
    Snackbar snackbar;
    private CursorHelper cursorHelper;
    private String time = null;
    private String TAG = "NewGame";
    private EditText editTextPlayer;
    private Button buttonNewGame, buttonAddPlayer, buttonQuit;
    private RecyclerView playerList;
    private String player;
    private ArrayList<String> players = new ArrayList<>();
    private ArrayList<String> score = new ArrayList<>();
    private Intent mainActivityIntent;
    private Integer gameID;
    private Intent homeIntent;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ScoreDBAdapter dbHelper;
    private boolean stop = true;
    private Spinner spinnerTimeLimit;
    private String timeLimit = null;
    private ArrayList timeLimitArray;
    private String timeLimitCondensed = "";
    private ArrayAdapter<String> adapter;
    private AlertDialog alertDialog;
    private View dialogView;

    static final String STATE_PLAYERS = "playersArray";
    static final String STATE_PLAYER_NAME = "player";
    static final String STATE_TIME = "time";
    static final String STATE_GAMEID = "gameid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        spinnerTimeLimit = (Spinner)findViewById(R.id.spinnerTimeLimit);




        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
                FirebaseCrash.report(new Exception(e.toString()));

            }
        });

        dbHelper = new ScoreDBAdapter(this);
        dbHelper.open();

        dbHelper.createGame(players, time, score, 0, timeLimit);
        gameID = dbHelper.getNewestGame();
        dbHelper.close();


        cursorHelper = new CursorHelper();
        homeIntent = new Intent(this, Home.class);

        playerList = (RecyclerView)findViewById(R.id.playerList);
        newGameCoordinatorLayout = (CoordinatorLayout)findViewById(R.id.newGameLayout);

        buttonNewGame = (Button)findViewById(R.id.buttonNewGame);
        buttonNewGame.setOnClickListener(this);

        buttonQuit = (Button)findViewById(R.id.buttonQuit);
        buttonQuit.setOnClickListener(this);

        buttonAddPlayer = (Button) findViewById(R.id.buttonAddPlayer);
        buttonAddPlayer.setOnClickListener(this);

        editTextPlayer = (EditText) findViewById(R.id.editTextPlayer);
        playerList = (RecyclerView) findViewById(R.id.playerList);

        if (savedInstanceState != null) {
            // Restore value of members from saved state
            players = savedInstanceState.getStringArrayList(STATE_PLAYERS);
            gameID = savedInstanceState.getInt(STATE_GAMEID);
            time = savedInstanceState.getString(STATE_TIME);
            player = savedInstanceState.getString(STATE_PLAYER_NAME);
            displayRecyclerView();
        } else {
            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
            Date now = new Date();
            time = sdfDate.format(now);
            dbHelper.open();
            dbHelper.updateGame(null, time, ScoreDBAdapter.KEY_TIME, gameID);
            dbHelper.close();
            players = new ArrayList<>();
            displayRecyclerView();
        }

        editTextPlayer.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    addPlayers();
                    return true;
                }
                return false;
            }
        });

        //RecyclerView Stuff
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        playerList.setLayoutManager(mLayoutManager);

        //Shared Preferences stuff
        final String PREFS_NAME = "scorekeeper";

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        if (settings.getBoolean("my_first_time", true)) {

            timeLimitArray = new ArrayList();
            timeLimitArray.add(0, "No Time Limit");
            timeLimitArray.add(1, "1 Minute");
            timeLimitArray.add(2, "5 Minutes");
            timeLimitArray.add(3, "30 Minutes");
            timeLimitArray.add(4, "90 Minutes");
            timeLimitArray.add(5, "Create...");
            saveSharedPrefs();
            settings.edit().putBoolean("my_first_time", false).commit();
        }else {
            SharedPreferences sharedPref = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
            timeLimitArray = convertToArray(sharedPref.getString("timelimitarray", null));

        }

        displaySpinner();
    }

    public ArrayList convertToArray(String s){
        ArrayList arrayList = null;

        String[] strValues = s.split(",");
        arrayList = new ArrayList<>(Arrays.asList(strValues));

        return arrayList;
    }

    public String convertToString(List<String> array) {

        String str = TextUtils.join(",", array);

        return str;
    }


    public void saveSharedPrefs(){
        SharedPreferences sharedPref = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("timelimitarray", convertToString(timeLimitArray));

        editor.apply();
    }

    public void displaySpinner(){
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, timeLimitArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimeLimit.setAdapter(adapter);
        spinnerTimeLimit.setOnItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        dbHelper.open();
    }
    public boolean checkDuplicates(ArrayList arrayList){
        boolean duplicate = false;

        Set<Integer> set = new HashSet<Integer>(arrayList);

        if(set.size() < arrayList.size()){
            duplicate = true;
        }

        return duplicate;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_delete).setVisible(false);
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

    public void displayRecyclerView(){
        playerListAdapter = new PlayerListAdapter(players, score, dbHelper, gameID, 1, 0);
        playerList.setAdapter(playerListAdapter);

    }

    public void addPlayers(){
        player = editTextPlayer.getText().toString();
        boolean duplicates;
        players.add(players.size(), player);
        duplicates = checkDuplicates(players);

        if (duplicates){
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                }
            };
            players.remove(players.size() -1);

            snackbar = Snackbar.make(newGameCoordinatorLayout, R.string.duplicates_message, Snackbar.LENGTH_SHORT)
                    .setAction("Dismiss", onClickListener);
            snackbar.show();
        }

        if (player.equals("")){
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                }
            };

            players.remove(players.size() -1);

            snackbar = Snackbar.make(newGameCoordinatorLayout, R.string.must_have_name, Snackbar.LENGTH_SHORT)
                    .setAction("Dismiss", onClickListener);
            snackbar.show();
        }else if (!duplicates){

            editTextPlayer.setText("");

            dbHelper.open();
            dbHelper.updateGame(players, null, ScoreDBAdapter.KEY_PLAYERS,gameID);
            dbHelper.updateGame(null, "00:00:00:0", ScoreDBAdapter.KEY_CHRONOMETER,gameID);
            dbHelper.close();

            // specify an adapter (see also next example)
            playerListAdapter.notifyItemInserted(players.size());
            playerListAdapter.notifyDataSetChanged();

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (stop){
            dbHelper.open();
            dbHelper.deleteGame(gameID);
            dbHelper.close();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        onStop();
        playerListAdapter.closeDB();
        dbHelper.close();

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state

        if (gameID != null) {
            savedInstanceState.putInt(STATE_GAMEID, gameID);
            savedInstanceState.putStringArrayList(STATE_PLAYERS, players);
            savedInstanceState.putString(STATE_PLAYER_NAME, player);
            savedInstanceState.putString(STATE_TIME, time);
        }

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.quit_setup_question);

        builder.setMessage(R.string.quit_setup_message);

        builder.setPositiveButton(R.string.quit_setup, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                stop = true;
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

    public void createScoreArray(){
        score.clear();

        while (score.size() < players.size()){
            score.add("0");
        }

        dbHelper.open();
        dbHelper.updateGame(score, null, ScoreDBAdapter.KEY_SCORE, gameID);
        dbHelper.close();

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonAddPlayer: {
                addPlayers();
                break;
            }

            case R.id.buttonQuit:{
                onBackPressed();
                break;
            }

            case R.id.buttonNewGame: {
                mainActivityIntent = new Intent(this, MainActivity.class);
                stop = false;

                //snackbar must have 2 or more players

                if (players.size() < 2) {

                    View.OnClickListener onClickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                        }
                    };

                    snackbar = Snackbar.make(NewGame.newGameCoordinatorLayout, R.string.more_than_two_players, Snackbar.LENGTH_SHORT)
                            .setAction("Dismiss", onClickListener);
                    snackbar.show();
                }else{
                    if (checkDuplicates(players)){

                        View.OnClickListener onClickListener = new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                snackbar.dismiss();
                            }
                        };

                        snackbar = Snackbar.make(NewGame.newGameCoordinatorLayout, R.string.duplicates_message, Snackbar.LENGTH_SHORT)
                                .setAction("Dismiss", onClickListener);
                        snackbar.show();
                    }else{
                        createScoreArray();
                        mainActivityIntent.putExtra("gameID", gameID);
                        startActivity(mainActivityIntent);
                        finish();
                    }


                }

                break;

            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        dbHelper.open();
        switch (position){
            case 0:
                timeLimit = null;
                dbHelper.updateGame(null, timeLimit, ScoreDBAdapter.KEY_TIMER, gameID);
                dbHelper.close();
                break;
            case 1:
                timeLimit = "00:01:00:0";
                dbHelper.updateGame(null, timeLimit, ScoreDBAdapter.KEY_TIMER, gameID);
                dbHelper.close();
                break;
            case 2:
                timeLimit = "00:05:00:0";
                dbHelper.updateGame(null, timeLimit, ScoreDBAdapter.KEY_TIMER, gameID);
                dbHelper.close();
                break;
            case 3:
                timeLimit = "00:30:00:0";
                dbHelper.updateGame(null, timeLimit, ScoreDBAdapter.KEY_TIMER, gameID);
                dbHelper.close();
                break;

            case 4:
                timeLimit = "00:90:00:0";
                dbHelper.updateGame(null, timeLimit, ScoreDBAdapter.KEY_TIMER, gameID);
                dbHelper.close();
                break;

        }

        if (position == timeLimitArray.size() -1){
            final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            dialogView = inflater.inflate(R.layout.content_time_limit, null);
            final EditText editTextHour = (EditText) dialogView.findViewById(R.id.editTextHour);
            final EditText editTextMinute = (EditText) dialogView.findViewById(R.id.editTextMinute);
            final EditText editTextSecond = (EditText) dialogView.findViewById(R.id.editTextSeconds);
            editTextHour.setText("0");
            editTextMinute.setText("0");
            editTextSecond.setText("0");
            dialogBuilder.setPositiveButton(R.string.create, null);
            dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            dialogBuilder.setView(dialogView);
            alertDialog = dialogBuilder.create();

            alertDialog.setOnShowListener(this);

            dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            alertDialog.show();

            dbHelper.close();

        }
    }

    private String createTimeLimitCondensed(String timeLimit){
        Pattern p = Pattern.compile("^\\d+$");
        Matcher m = p.matcher(timeLimit);
        timeLimitCondensed = "";
        StringBuilder stringBuilder = new StringBuilder();

        if(m.matches()){
            timeLimit.replaceAll("^0+", "");
        }


        String[] timeLimitSplit = timeLimit.split(":");

        String hour = timeLimitSplit[0];
        String minute = timeLimitSplit[1];
        String second = timeLimitSplit[2];
        Log.e(TAG, hour+ minute+ second);

        if (!hour.equals("00")){
            stringBuilder.append(" , " + Integer.valueOf(hour).toString()).append(" Hours ");
            timeLimitCondensed = stringBuilder.toString();

        }

        if(!minute.equals("00")){
            if (!timeLimitCondensed.equals("")){
                stringBuilder.append(" , " + Integer.valueOf(minute).toString()).append(", Minutes ");

            }else{
                stringBuilder.append(Integer.valueOf(minute).toString()).append(" Minutes ");

            }
            timeLimitCondensed = stringBuilder.toString();


        }

        if(!second.equals("00")){
            Log.e(TAG, "seconds");

            if (!timeLimitCondensed.equals("")){
                stringBuilder.append(" , " + Integer.valueOf(second).toString()).append(" Seconds ");

            }else{
                stringBuilder.append(Integer.valueOf(second).toString()).append(" Seconds ");

            }
            timeLimitCondensed = stringBuilder.toString();

        }


        Log.e(TAG, timeLimitCondensed);

        return timeLimitCondensed;

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
            timeLimit = null;
        dbHelper.open();
        dbHelper.updateGame(null, timeLimit, ScoreDBAdapter.KEY_TIMER, gameID);
        dbHelper.close();

    }

    @Override
    public void onShow(DialogInterface dialog) {

        Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
            final EditText editTextHour = (EditText) dialogView.findViewById(R.id.editTextHour);
            final EditText editTextMinute = (EditText) dialogView.findViewById(R.id.editTextMinute);
            final EditText editTextSecond = (EditText) dialogView.findViewById(R.id.editTextSeconds);
            String hour = editTextHour.getText().toString().trim();
            String minute = editTextMinute.getText().toString().trim();
            String seconds = editTextSecond.getText().toString().trim();
            String timeLimitString = "";
            DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss:S");

            if(TextUtils.isEmpty(hour)) {
                editTextHour.setError("Can't be empty");
                return;
            }else if(TextUtils.isEmpty(minute)) {
                editTextMinute.setError("Can't be empty");
                return;
            }else if(TextUtils.isEmpty(seconds)) {
                editTextSecond.setError("Can't be empty");
                return;
            }else {


                if (Integer.valueOf(hour) >= 24) {
                    editTextHour.setError("Hour must be less than 24");
                } else if (Integer.valueOf(minute) >= 60) {
                    editTextMinute.setError("Minute must be less than 60");

                } else if (Integer.valueOf(seconds) >= 60) {
                    editTextSecond.setError("Seconds must be less than 60");

                } else {

                    try {
                        if (hour.length() == 1 && !hour.equals("0")) {
                            Log.e(TAG, "hour");
                            hour = ("0" + hour);
                        }
                        if (minute.length() == 1 && !minute.equals("0")) {
                            Log.e(TAG, "minute");
                            minute = ("0" + minute);
                        }
                        if (seconds.length() == 1&& !seconds.equals("0")) {
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
                        dbHelper.open();
                        dbHelper.updateGame(null, timeLimitString, ScoreDBAdapter.KEY_TIMER, gameID);
                        dbHelper.close();
                        Toast toast = Toast.makeText(getBaseContext(), timeLimitString, Toast.LENGTH_SHORT);
                        toast.show();
                        timeLimit = timeLimitString;
                        timeLimitArray.add(timeLimitArray.size() - 1, createTimeLimitCondensed(timeLimitString));

                        if (checkDuplicates(timeLimitArray)){
                            timeLimitArray.remove(timeLimitArray.size()-2);
                            Snackbar snackbar = Snackbar.make(newGameCoordinatorLayout, "Already exists", Snackbar.LENGTH_SHORT);
                            snackbar.show();
                        }

                        adapter.notifyDataSetChanged();
                        alertDialog.dismiss();
                        spinnerTimeLimit.setSelection(timeLimitArray.size()-2);
                        saveSharedPrefs();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e(TAG, e.toString());
                        Toast toast = Toast.makeText(getBaseContext(), R.string.invalid_time, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            }
        }
        });
    }

}
