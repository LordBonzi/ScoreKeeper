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
import android.util.Log;
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
    private String lengthStr =null;
    private EditText editTextLength, editTextDate;
    private ArrayList arrayListPlayers, arrayListScores;
    private RecyclerView recyclerView;
    private ScoreDBAdapter dbHelper;
    private DataHelper dataHelper;
    private Intent settingsIntent, homeIntent;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private PlayerListAdapter playerListAdapter;
    private ArrayList players;
    public static CoordinatorLayout editGameLayout;
    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
    SimpleDateFormat lengthFormat = new SimpleDateFormat("mm:ss:S");//dd/MM/yyyy
    SimpleDateFormat hourlengthFormat = new SimpleDateFormat("hh:mm:ss:S");//dd/MM/yyyy
    int accentColor;

    private MenuItem menuItemDelete, menuItemGraph, menuItemEdit, menuItemDone, menuItemCancel, menuItemAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
        int accentColor = sharedPreferences.getInt("prefAccent", R.style.AppTheme);
        setTheme(accentColor);
        setContentView(R.layout.activity_edit_game);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        gameID = extras.getInt("gameID");

        dbHelper = new ScoreDBAdapter(this);
        dbHelper.open();

        dataHelper = new DataHelper();

        settingsIntent = new Intent(this, Settings.class);
        homeIntent = new Intent(this, Home.class);

        editTextDate = (EditText) findViewById(R.id.editTextDate);
        editTextLength = (EditText) findViewById(R.id.editTextLength);
        editGameLayout = (CoordinatorLayout) findViewById(R.id.edit_game_content);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewEditGame);

        arrayListPlayers = dataHelper.getArrayById(ScoreDBAdapter.KEY_PLAYERS, gameID, dbHelper);
        arrayListScores = dataHelper.getArrayById(ScoreDBAdapter.KEY_SCORE, gameID, dbHelper);

        date = dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_TIME, dbHelper);

        editTextLength.setHint(dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_CHRONOMETER, dbHelper));
        editTextDate.setHint(dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_TIME, dbHelper));
        editTextLength.setEnabled(false);
        editTextDate.setEnabled(false);

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
    protected void onResume() {
        super.onResume();
        dbHelper.open();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }

    @Override
    protected void onStop() {
        super.onStop();
        dbHelper.close();
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
            PlayerListAdapter.newPlayer(playerListAdapter);
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

        editTextLength.setText(dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_CHRONOMETER, dbHelper));
        editTextDate.setText(dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_TIME, dbHelper));
        displayRecyclerView(1);
    }

    public void displayRecyclerView(int editable) {
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        players = dataHelper.getArrayById(ScoreDBAdapter.KEY_PLAYERS, gameID, dbHelper);
        playerListAdapter = new PlayerListAdapter(players, arrayListScores, dbHelper, gameID, 2, editable);
        recyclerView.setAdapter(playerListAdapter);

    }

    public void onMenuDoneClick() {

        final String newDate = editTextDate.getText().toString();
        final String newLength = editTextLength.getText().toString();
        final boolean bDateAndTime = checkValidity(editTextDate.getText().toString(), dateTimeFormat, 19);
        final boolean bLength = checkValidity(editTextLength.getText().toString(), lengthFormat, 7)||checkValidity(editTextLength.getText().toString(), hourlengthFormat, 10);
        final boolean bCheckEmpty = false;
        final boolean bCheckDuplicates = PlayerListAdapter.checkDuplicates(PlayerListAdapter.playerArray);
        final boolean bNumPlayers = PlayerListAdapter.checkNumberPlayers(PlayerListAdapter.playerArray);

        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.edit_game_question);

        builder.setMessage(R.string.are_you_sure_edit_game);

        builder.setPositiveButton(R.string.title_activity_edit_game, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                Log.i("positivebutton", ""+PlayerListAdapter.playerArray);

                if (bCheckEmpty) {

                    invalidSnackbar("You can't have empty names!");

                }else if (!bDateAndTime) {
                    invalidSnackbar(getString(R.string.invalid_date_and_time));

                } else if (!bLength){
                    invalidSnackbar(getString(R.string.invalid_time));

                } else if (bCheckDuplicates) {

                    invalidSnackbar("You can't have duplicate players!");

                } else if (bNumPlayers) {

                    invalidSnackbar("Must have 2 or more players");

                }else if (!bCheckEmpty && bDateAndTime && bLength && !bCheckDuplicates && !bNumPlayers){
                    dbHelper.open();
                    dbHelper.updateGame(null, newDate, ScoreDBAdapter.KEY_TIME, gameID);
                    dbHelper.updateGame(null, newLength, ScoreDBAdapter.KEY_CHRONOMETER, gameID);
                    for (int i = 1; i < PlayerListAdapter.playerArray.size(); i++){
                        if (PlayerListAdapter.playerArray.get(i).equals("")){
                            PlayerListAdapter.playerArray.remove(i);
                            PlayerListAdapter.scoreArray.remove(i);
                        }

                    }

                    dbHelper.updateGame(PlayerListAdapter.playerArray,null, ScoreDBAdapter.KEY_PLAYERS, gameID);
                    dbHelper.updateGame(PlayerListAdapter.scoreArray, null, ScoreDBAdapter.KEY_SCORE, gameID);
                    editTextLength.setText(dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_CHRONOMETER, dbHelper));
                    editTextDate.setText(dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_TIME, dbHelper));
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
                    dbHelper.close();
                }

            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                displayRecyclerView(1);
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
        editTextLength.setText(dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_CHRONOMETER, dbHelper));
        editTextDate.setText(dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_TIME, dbHelper));

        editTextLength.setEnabled(false);
        editTextDate.setEnabled(false);
        menuItemDelete.setVisible(true);
        menuItemGraph.setVisible(true);
        menuItemDone.setVisible(false);
        menuItemEdit.setVisible(true);
        menuItemAdd.setVisible(false);
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



}
