package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NewGame extends AppCompatActivity
        implements View.OnClickListener, PresetListener{

    public static PlayerListAdapter playerListAdapter;
    private Snackbar snackbar;
    private TimeLimitAdapter timeLimitAdapter;
    private PresetDBAdapter presetDBAdapter;
    private DataHelper dataHelper;
    private String time = null;
    private String TAG = "NewGame";
    private EditText editTextPlayer;
    private Button buttonNewGame, buttonAddPlayer, buttonQuit, buttonCreatePreset;
    private CheckBox checkBoxNoTimeLimit;
    private RecyclerView playerList;
    private String player;
    private ArrayList players = new ArrayList<>();
    private ArrayList<String> score = new ArrayList<>();
    private Integer gameID;
    private Intent homeIntent, mainActivityIntent;
    private RecyclerView.LayoutManager mLayoutManager;
    private ScoreDBAdapter dbHelper;
    private boolean stop = true;
    public static Spinner spinnerTimeLimit;
    public static Spinner spinnerPreset;
    private String timeLimit = null;
    private List timeLimitArray;
    private List timeLimitArrayNum;
    private String timeLimitCondensed = "";
    static final String STATE_PLAYERS = "playersArray";
    static final String STATE_PLAYER_NAME = "player";
    static final String STATE_TIME = "time";
    static final String STATE_GAMEID = "gameid";

    public static RelativeLayout relativeLayout, relativeLayoutCustomGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_new_game);

        relativeLayoutCustomGame = (RelativeLayout)findViewById(R.id.relativeLayoutCustomGame);
        relativeLayout = (RelativeLayout)findViewById(R.id.newGameLayout);
        spinnerTimeLimit = (Spinner)findViewById(R.id.spinnerTimeLimit);
        spinnerPreset = (Spinner)findViewById(R.id.spinnerPreset);

        buttonCreatePreset = (Button)findViewById(R.id.buttonCreatePreset);
        buttonCreatePreset.setOnClickListener(this);
        buttonCreatePreset.setVisibility(View.VISIBLE);

        presetDBAdapter = new PresetDBAdapter(this);
        dbHelper = new ScoreDBAdapter(this);

        dbHelper.open();
        dbHelper.createGame(players, time, score, 0, timeLimit);
        gameID = dbHelper.getNewestGame();
        dbHelper.close();

        dataHelper = new DataHelper();
        homeIntent = new Intent(this, Home.class);

        playerList = (RecyclerView)findViewById(R.id.playerList);

        buttonNewGame = (Button)findViewById(R.id.buttonNewGame);
        buttonNewGame.setOnClickListener(this);

        checkBoxNoTimeLimit = (CheckBox) findViewById(R.id.checkBoxNoTimeLimit);
        checkBoxNoTimeLimit.setOnClickListener(this);
        checkBoxNoTimeLimit.setChecked(false);


        spinnerTimeLimit.setEnabled(false);
        timeLimit = null;
        dbHelper.open();
        dbHelper.updateGame(null, timeLimit, ScoreDBAdapter.KEY_TIMER, gameID);
        dbHelper.close();

        buttonQuit = (Button)findViewById(R.id.buttonQuit);
        buttonQuit.setOnClickListener(this);

        buttonAddPlayer = (Button) findViewById(R.id.buttonAddPlayer);
        buttonAddPlayer.setOnClickListener(this);

        editTextPlayer = (EditText) findViewById(R.id.editTextPlayer);

        if (savedInstanceState != null) {
            // Restore value of members from saved state
            players = savedInstanceState.getStringArrayList(STATE_PLAYERS);
            gameID = savedInstanceState.getInt(STATE_GAMEID);
            time = savedInstanceState.getString(STATE_TIME);
            player = savedInstanceState.getString(STATE_PLAYER_NAME);
            displayRecyclerView(players);
        } else {
            SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
            Date now = new Date();
            time = sdfDate.format(now);
            dbHelper.open();
            dbHelper.updateGame(null, time, ScoreDBAdapter.KEY_TIME, gameID);
            dbHelper.close();
            players = new ArrayList<>();
            displayRecyclerView(players);
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


        //Shared Preferences stuff

        SharedPreferences sharedPref = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
        String timeLimit = sharedPref.getString("timelimitarray", null);
        String timeLimitnum = sharedPref.getString("timelimitarraynum", null);

        if (timeLimit != null && timeLimitnum !=null) {

            timeLimitArray = dataHelper.convertToArray(timeLimit);
            timeLimitArrayNum = dataHelper.convertToArray(timeLimitnum);
        }else{
            timeLimitArray = new ArrayList();
            timeLimitArray.add(0, "Create...");

            timeLimitArrayNum = new ArrayList();
            timeLimitArrayNum.add(0, "Create...");
            dataHelper.saveSharedPrefs(timeLimitArray, timeLimitArrayNum, this);
        }


        spinnerTimeLimit.setVisibility(View.INVISIBLE);
        displaySpinner(true);
        displaySpinner(false);
    }



    public void displaySpinner(boolean timelimitspinner){

        if (timelimitspinner) {
            timeLimitAdapter = new TimeLimitAdapter(this, timeLimitArray, timeLimitArrayNum, dbHelper, gameID);
            spinnerTimeLimit.setAdapter(timeLimitAdapter);
        }else {
            ArrayList titleArrayList = new ArrayList();

            for (int i = 1; i <= presetDBAdapter.open().numRows(); i++){
                presetDBAdapter.open();
                titleArrayList.add(dataHelper.getPresetStringByID(i, PresetDBAdapter.KEY_TITLE, presetDBAdapter));
                presetDBAdapter.close();
            }

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, titleArrayList);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinnerPreset.setAdapter(dataAdapter);
            if (presetDBAdapter.numRows() == 0){
                NewGame.spinnerPreset.setVisibility(View.INVISIBLE);
            }
            spinnerPreset.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    loadGame(i+1);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    players = new ArrayList();
                    timeLimit = null;
                    displayRecyclerView(players);
                    displaySpinner(true);

                }
            });
        }

    }

    public void loadGame(int position){
        dataHelper = new DataHelper();
        presetDBAdapter.open();
        ArrayList presetPlayers = new ArrayList();
        String presetTimeLimit;
        presetPlayers = dataHelper.getPresetPlayerArrayByID(position, presetDBAdapter);
        presetTimeLimit = dataHelper.getPresetStringByID(position, PresetDBAdapter.KEY_TIME_LIMIT, presetDBAdapter);
        presetDBAdapter.close();

        updateEditText(presetPlayers, presetTimeLimit);

    }

    @Override
    protected void onResume() {
        super.onResume();
        dbHelper.open();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_delete).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_edit_presets).setVisible(true);
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
        }if (id == R.id.action_edit_presets){
            onEditPresetsClick();
        }if (id == R.id.action_edit_presets){
            deletePresetsDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    public void onEditPresetsClick(){

    }

    public void displayRecyclerView(ArrayList players){
        mLayoutManager = new LinearLayoutManager(this);
        playerList.setLayoutManager(mLayoutManager);
        playerListAdapter = new PlayerListAdapter(players, score, dbHelper, gameID, 1, 0);
        playerList.setAdapter(playerListAdapter);

    }

    public void addPlayers(){
        player = editTextPlayer.getText().toString();
        boolean duplicates;
        players.add(players.size(), player);
        duplicates = dataHelper.checkDuplicates(players);

        if (duplicates){
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                }
            };
            players.remove(players.size() -1);

            snackbar = Snackbar.make(relativeLayout, R.string.duplicates_message, Snackbar.LENGTH_SHORT)
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

            snackbar = Snackbar.make(relativeLayout, R.string.must_have_name, Snackbar.LENGTH_SHORT)
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
                dbHelper.open();
                dbHelper.deleteGame(gameID);
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

    public void createScoreArray(){
        score.clear();

        while (score.size() < players.size()){
            score.add("0");
        }

        dbHelper.open();
        dbHelper.updateGame(score, null, ScoreDBAdapter.KEY_SCORE, gameID);
        dbHelper.close();

    }

    public void deletePresetsDialog(){
        final View dialogView;
        ArrayList titleArrayList  =new ArrayList();

        for (int i = 1; i <= presetDBAdapter.open().numRows(); i++){
            presetDBAdapter.open();
            titleArrayList.add(dataHelper.getPresetStringByID(i, PresetDBAdapter.KEY_TITLE, presetDBAdapter));
            presetDBAdapter.close();
        }

        final RecyclerViewArrayAdapter arrayAdapter = new RecyclerViewArrayAdapter(titleArrayList, this);
        LayoutInflater inflter = LayoutInflater.from(this);
        final AlertDialog alertDialog;
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogView = inflter.inflate(R.layout.recyclerview_fragment, null);
        final RecyclerView recyclerView = (RecyclerView) dialogView.findViewById(R.id.recyclerViewFragment);
        dialogBuilder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for (int j = 0; j < arrayAdapter.getItemsToDeleteList().size(); j++){
                    presetDBAdapter.open();
                    Log.e(TAG, arrayAdapter.getItemsToDeleteList().get(j) + " , "+j);
                    presetDBAdapter.deletePreset((Integer) arrayAdapter.getItemsToDeleteList().get(j));
                    presetDBAdapter.close();
                    displaySpinner(false);

                }
            }

        });
        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


        dialogBuilder.setView(dialogView);

        alertDialog = dialogBuilder.create();
        mLayoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.setAdapter(arrayAdapter);


        alertDialog.show();
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
                createNewGame(players, true);

                break;

            }

            case R.id.checkBoxNoTimeLimit: {
                if (checkBoxNoTimeLimit.isChecked()){
                    spinnerTimeLimit.setEnabled(true);
                    displaySpinner(false);
                    spinnerTimeLimit.setVisibility(View.VISIBLE);
                    dbHelper.open();
                    dbHelper.updateGame(null, timeLimitArrayNum.get(0).toString(),ScoreDBAdapter.KEY_TIMER, gameID);

                }else{
                    spinnerTimeLimit.setEnabled(false);
                    spinnerTimeLimit.setVisibility(View.INVISIBLE);
                    timeLimit = null;
                    dbHelper.open();
                    timeLimit=null;
                    dbHelper.updateGame(null, timeLimit, ScoreDBAdapter.KEY_TIMER, gameID);
                    dbHelper.close();
                }

                break;
            }

            case R.id.buttonCreatePreset:{
                createNewGame(players, false);

                break;
            }


        }
    }

    public void createPresetDialog() {
        final View dialogView;

        LayoutInflater inflter = LayoutInflater.from(this);
        final AlertDialog alertDialog;
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogView = inflter.inflate(R.layout.create_preset_fragment, null);
        final EditText editTextPresetTitle = (EditText) dialogView.findViewById(R.id.editTextPresetTitle);
        dialogBuilder.setPositiveButton(R.string.create, null);
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
                final String[] title = new String[1];

                if (players.size() == 2){
                    editTextPresetTitle.setHint(players.get(0) + " vs " + players.get(1));

                }else if (players.size() == 3){
                    editTextPresetTitle.setHint(players.get(0) + " vs " + players.get(1) + " vs " + players.get(2));

                }else if (players.size() > 3 && players.size() < 10){
                    String playerTitle = "";
                    for (int i = 0; i < players.size(); i++){
                        playerTitle += players.get(i);
                        if (i != players.size()-1){
                            playerTitle += ",";
                        }
                    }

                    editTextPresetTitle.setHint(playerTitle);

                }else if (players.size() > 10){
                    String playerTitle = "";
                    for (int i = 0; i < players.size(); i++){
                        playerTitle += players.get(i);
                        if (i != players.size()-1){
                            playerTitle += ",";
                        }
                    }

                    editTextPresetTitle.setHint(playerTitle);

                }else if (players.size() == 1){
                    editTextPresetTitle.setHint(String.valueOf(players.get(0)));

                }

                 title[0] = editTextPresetTitle.getHint().toString();

                editTextPresetTitle.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        title[0] = charSequence.toString();

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        createPreset(title[0]);
                        alertDialog.dismiss();
                        displaySpinner(false);
                        spinnerPreset.setVisibility(View.VISIBLE);

                    }
                });
            }
        });
        alertDialog.show();

    }

    public void createNewGame(ArrayList players, boolean startGame) {
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

                snackbar = Snackbar.make(relativeLayout, R.string.more_than_two_players, Snackbar.LENGTH_SHORT)
                        .setAction("Dismiss", onClickListener);
                snackbar.show();
            } else {
                if (dataHelper.checkDuplicates(players)) {

                    View.OnClickListener onClickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                        }
                    };

                    snackbar = Snackbar.make(relativeLayout, R.string.duplicates_message, Snackbar.LENGTH_SHORT)
                            .setAction("Dismiss", onClickListener);
                    snackbar.show();
                } else {
                    createScoreArray();

                    if (startGame) {
                        mainActivityIntent.putExtra("gameID", gameID);
                        startActivity(mainActivityIntent);
                        finish();
                    }else{
                        createPresetDialog();
                    }
                }

            }

    }

    public void createPreset(String title){
        presetDBAdapter = new PresetDBAdapter(this);
        presetDBAdapter.open();
        presetDBAdapter.createPreset(players, timeLimit, title);
        presetDBAdapter.close();

    }

    @Override
    public void updateEditText(ArrayList players, String timeLimit) {
        this.players = players;
        displayRecyclerView(players);
        checkBoxNoTimeLimit.setChecked(true);
        spinnerTimeLimit.setEnabled(true);
        spinnerTimeLimit.setVisibility(View.VISIBLE);
    }


}
