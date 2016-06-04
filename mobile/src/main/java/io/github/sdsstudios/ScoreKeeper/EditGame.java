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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class EditGame extends AppCompatActivity {
    String date;
    int gameID;
    private EditText editTextLength, editTextDate;
    private ArrayList arrayListPlayers, arrayListScores;
    private RecyclerView recyclerView;
    private ScoreDBAdapter dbHelper;
    private CursorHelper cursorHelper;
    private Intent settingsIntent, homeIntent, graphIntent;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private PlayerListAdapter playerListAdapter;
    private ArrayList players;
    public static CoordinatorLayout editGameLayout;
    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
    SimpleDateFormat lengthFormat = new SimpleDateFormat("mm:ss:SSS");//dd/MM/yyyy


    private MenuItem menuItemDelete, menuItemGraph, menuItemEdit, menuItemDone, menuItemCancel, menuItemAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_game);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        gameID = extras.getInt("gameID");

        dbHelper = new ScoreDBAdapter(this);
        dbHelper.open();

        cursorHelper = new CursorHelper();

        settingsIntent = new Intent(this, Settings.class);
        graphIntent = new Intent(this, Graph.class);
        homeIntent = new Intent(this, Home.class);

        editTextDate = (EditText) findViewById(R.id.editTextDate);
        editTextLength = (EditText) findViewById(R.id.editTextLength);
        editGameLayout = (CoordinatorLayout) findViewById(R.id.edit_game_content);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewEditGame);

        arrayListPlayers = cursorHelper.getArrayById(ScoreDBAdapter.KEY_PLAYERS, gameID, dbHelper);
        arrayListScores = cursorHelper.getArrayById(ScoreDBAdapter.KEY_SCORE, gameID, dbHelper);

        date = cursorHelper.getStringById(gameID, ScoreDBAdapter.KEY_TIME, dbHelper);

        editTextLength.setHint(cursorHelper.getStringById(gameID, ScoreDBAdapter.KEY_CHRONOMETER, dbHelper));
        editTextDate.setHint(cursorHelper.getStringById(gameID, ScoreDBAdapter.KEY_TIME, dbHelper));
        editTextLength.setEnabled(false);
        editTextDate.setEnabled(false);

        final String PREFS_NAME = "scorekeeper";

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        if (settings.getBoolean("my_first_time", true)) {

            saveInfo();
            settings.edit().putBoolean("my_first_time", false).commit();
        } else {
            SharedPreferences sharedPref = getSharedPreferences("scorekeeper"
                    , Context.MODE_PRIVATE);

        }

        displayRecyclerView(0);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menuItemDelete = menu.findItem(R.id.action_delete);
        menuItemDone = menu.findItem(R.id.action_done);
        menuItemEdit = menu.findItem(R.id.action_edit);
        menuItemGraph = menu.findItem(R.id.action_graph);
        menuItemCancel = menu.findItem(R.id.action_cancel);
        menuItemAdd = menu.findItem(R.id.action_add);

        menu.findItem(R.id.action_delete).setVisible(true);
        menu.findItem(R.id.action_graph).setVisible(true);
        menu.findItem(R.id.action_edit).setVisible(true);
        menu.findItem(R.id.action_settings).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete) {
            delete();
        } else if (id == R.id.action_graph) {
            Toast toast = Toast.makeText(this, R.string.graph_coming_soon, Toast.LENGTH_LONG);
            toast.show();

        } else if (id == R.id.action_edit) {
            onMenuEditClick();

        } else if (id == R.id.action_done) {
            onMenuDoneClick();

        } else if (id == R.id.action_cancel) {
            onMenuCancelClick();

        } else if (id == R.id.action_add){
            PlayerListAdapter.newPlayer(dbHelper, gameID, playerListAdapter);
        }

        return super.onOptionsItemSelected(item);
    }

    public void onMenuEditClick() {
        editTextLength.setEnabled(true);
        editTextDate.setEnabled(true);

        menuItemAdd.setVisible(true);
        menuItemDelete.setVisible(false);
        menuItemGraph.setVisible(false);
        menuItemEdit.setVisible(false);
        menuItemDone.setVisible(true);
        menuItemCancel.setVisible(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        editTextLength.setText(cursorHelper.getStringById(gameID, ScoreDBAdapter.KEY_CHRONOMETER, dbHelper));
        editTextDate.setText(cursorHelper.getStringById(gameID, ScoreDBAdapter.KEY_TIME, dbHelper));
        displayRecyclerView(1);
    }

    public void displayRecyclerView(int editable) {
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        players = cursorHelper.getArrayById(ScoreDBAdapter.KEY_PLAYERS, gameID, dbHelper);
        playerListAdapter = new PlayerListAdapter(players, arrayListScores, dbHelper, gameID, 2, editable);
        recyclerView.setAdapter(playerListAdapter);

    }

    public void onMenuDoneClick() {

        final String newDate = editTextDate.getText().toString();
        final String newLength = editTextLength.getText().toString();
        final boolean bDateAndTime = checkValidity(editTextDate.getText().toString(), dateTimeFormat, 19);
        final boolean bLength = checkValidity(editTextLength.getText().toString(), lengthFormat, 9);

        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.edit_game_question);

        builder.setMessage(R.string.are_you_sure_edit_game);

        builder.setPositiveButton(R.string.title_activity_edit_game, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (!bDateAndTime) {
                    invalidSnackbar(getString(R.string.invalid_date_and_time));

                } else if (!bLength){
                    invalidSnackbar(getString(R.string.invalid_time));

                } else if (PlayerListAdapter.checkDuplicates(PlayerListAdapter.playerArray)) {

                    invalidSnackbar("You can't have duplicate players!");

                }else if (PlayerListAdapter.checkEmpty()) {

                    invalidSnackbar("You can't have empty names!");

                }else{
                    dbHelper.updateGame(null, newDate, ScoreDBAdapter.KEY_TIME, gameID);
                    dbHelper.updateGame(null, newLength, ScoreDBAdapter.KEY_CHRONOMETER, gameID);
                    editTextLength.setText(cursorHelper.getStringById(gameID, ScoreDBAdapter.KEY_CHRONOMETER, dbHelper));
                    editTextDate.setText(cursorHelper.getStringById(gameID, ScoreDBAdapter.KEY_TIME, dbHelper));
                    displayRecyclerView(0);
                    editTextLength.setEnabled(false);
                    editTextDate.setEnabled(false);
                    menuItemAdd.setVisible(false);
                    menuItemDelete.setVisible(true);
                    menuItemGraph.setVisible(true);
                    menuItemDone.setVisible(false);
                    menuItemEdit.setVisible(true);
                    menuItemCancel.setVisible(false);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }

            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                displayRecyclerView(0);
                dialog.dismiss();
            }
        });

        dialog = builder.create();
        dialog.show();

    }


    public boolean checkValidity(String string, SimpleDateFormat simpleDateFormat, int length) {
        boolean validity = false;

        try {
            Date dateDate = simpleDateFormat.parse(string);

            if(string.length() == length) {
                validity = true;
            }

        } catch (ParseException e) {
            e.printStackTrace();

        }

        return validity;
    }

    public void invalidSnackbar(String message) {
        Snackbar snackbar;

        snackbar = Snackbar.make(editGameLayout, message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    public void onMenuCancelClick(){
        editTextLength.setText(cursorHelper.getStringById(gameID, ScoreDBAdapter.KEY_CHRONOMETER, dbHelper));
        editTextDate.setText(cursorHelper.getStringById(gameID, ScoreDBAdapter.KEY_TIME, dbHelper));

        editTextLength.setEnabled(false);
        editTextDate.setEnabled(false);
        menuItemDelete.setVisible(true);
        menuItemGraph.setVisible(true);
        menuItemDone.setVisible(false);
        menuItemEdit.setVisible(true);
        menuItemCancel.setVisible(false);

        displayRecyclerView(0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void delete(){
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.delete_game);

        builder.setMessage(R.string.delete_game_message);

        builder.setPositiveButton(R.string.delete_game, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dbHelper.deleteGame(gameID);
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

    public void saveInfo(){
        SharedPreferences sharedPref = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("gameID", gameID = 0);


        editor.apply();
    }


    }

