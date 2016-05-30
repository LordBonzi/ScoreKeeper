package io.github.sdsstudios.ScoreKeeper;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class NewGame extends AppCompatActivity
        implements View.OnClickListener {

    public static CoordinatorLayout newGameCoordinatorLayout;
    public static PlayerListAdapter playerListAdapter;
    Snackbar snackbar;
    private CursorHelper cursorHelper;
    private String time;
    private String TAG = "Home";
    private EditText editTextPlayer;
    private Button buttonNewGame, buttonAddPlayer;
    private RecyclerView playerList;
    private String player;
    private ArrayList<String> players;
    private ArrayList<String> score = new ArrayList<>();
    private Intent mainActivityIntent;
    private Integer gameID;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ScoreDBAdapter dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //TODO delete LOGS

        dbHelper = new ScoreDBAdapter(this);
        dbHelper.open();

        cursorHelper = new CursorHelper();

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        time = sdfDate.format(now);

        players = new ArrayList<>();
        playerList = (RecyclerView)findViewById(R.id.historyList);

        newGameCoordinatorLayout = (CoordinatorLayout)findViewById(R.id.newGameLayout);

        buttonNewGame = (Button)findViewById(R.id.buttonNewGame);
        buttonNewGame.setOnClickListener(this);

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

    }

    public void updateArray(){
        players = cursorHelper.getArrayById(ScoreDBAdapter.KEY_PLAYERS, gameID, dbHelper);
    }

    public void displayRecyclerView(){
        playerListAdapter = new PlayerListAdapter(players, dbHelper, gameID);
        playerList.setAdapter(playerListAdapter);

    }

    public void addPlayers(){
        player = editTextPlayer.getText().toString();
        Log.i(TAG, String.valueOf(players.size()));

        if (players.size() >= 1) {
            players.add(players.size(), player);

            dbHelper.updateGame(players, time , ScoreDBAdapter.KEY_PLAYERS, gameID);

        }else{
            players.add(players.size(), player);

            dbHelper.createGame(players, time, score);
            gameID = Integer.valueOf(dbHelper.getNewestGame());
        }

        editTextPlayer.setText("");

        // specify an adapter (see also next example)
        displayRecyclerView();
        updateArray();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        finish();

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonAddPlayer: {
                addPlayers();
                break;
            }

            case R.id.buttonNewGame: {
                mainActivityIntent = new Intent(this, MainActivity.class);

                try{
                    updateArray();
                    for (int i = 0; i < players.size(); i ++){
                        score.add(i, "0");
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }

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
                    dbHelper.updateGame(score, time, ScoreDBAdapter.KEY_SCORE, gameID);
                    startActivity(mainActivityIntent);
                }

                break;
            }
        }
    }
}
