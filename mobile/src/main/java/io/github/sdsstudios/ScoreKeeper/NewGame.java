package io.github.sdsstudios.ScoreKeeper;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.crash.FirebaseCrash;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class NewGame extends AppCompatActivity
        implements View.OnClickListener {

    public static CoordinatorLayout newGameCoordinatorLayout;
    public static PlayerListAdapter playerListAdapter;
    Snackbar snackbar;
    private CursorHelper cursorHelper;
    private String time = null;
    private String TAG = "Home";
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

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                FirebaseCrash.report(new Exception(t.toString()));

            }
        });

        dbHelper = new ScoreDBAdapter(this);
        dbHelper.open();

        dbHelper.createGame(players, time, score, 0);
        gameID = dbHelper.getNewestGame();
        Log.e(TAG + "id", ""+gameID);


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
            dbHelper.updateGame(null, time, ScoreDBAdapter.KEY_TIME, gameID);
            players = new ArrayList<>();
            displayRecyclerView();
        }

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

            dbHelper.updateGame(players, null, ScoreDBAdapter.KEY_PLAYERS,gameID);
            dbHelper.updateGame(null, "00:00:0", ScoreDBAdapter.KEY_CHRONOMETER,gameID);

            // specify an adapter (see also next example)
            playerListAdapter.notifyItemInserted(players.size());
            playerListAdapter.notifyDataSetChanged();

        }

    }

    @Override
    protected void onStop() {
        if (stop){
            dbHelper.open();
            dbHelper.deleteGame(gameID);
            players = null;
            score = null;
            dbHelper.close();
        }
        super.onStop();

    }

    @Override
    protected void onPause() {
        onStop();
        playerListAdapter.closeDB();
        dbHelper.close();
        super.onPause();

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

        dbHelper.updateGame(score, null, ScoreDBAdapter.KEY_SCORE, gameID);

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


}
