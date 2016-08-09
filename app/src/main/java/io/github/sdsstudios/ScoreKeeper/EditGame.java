package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class EditGame extends AppCompatActivity
        implements View.OnClickListener{
    String date;
    int gameID;
    private String lengthStr =null;
    private EditText editTextLength, editTextDate;
    private ArrayList<String> playerArray, scoreArray;
    private RecyclerView recyclerView;
    private ScoreDBAdapter dbHelper;
    private DataHelper dataHelper;
    private Intent settingsIntent, homeIntent;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private PlayerListAdapter playerListAdapter;
    public static RelativeLayout editGameLayout;
    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
    SimpleDateFormat hourlengthFormat = new SimpleDateFormat("hh:mm:ss:S");//dd/MM/yyyy
    private EditText  editTextMaxScore, editTextScoreInterval, editTextDiffToWin;
    private CheckBox  checkBoxReverseScrolling, checkBoxStopwatch;
    int accentColor;
    boolean bLength = false;
    private MenuItem menuItemDelete, menuItemEdit, menuItemDone, menuItemCancel, menuItemAdd
            , menuItemShare, menuItemComplete;
    private ShareActionProvider mShareActionProvider;
    private Intent mShareIntent;
    private ImageButton buttonHelpLength, buttonHelpDate;
    private int reverseScrolling = 0;
    private int maxScore = 0;
    private int scoreInterval = 1;
    private int diffToWin = 0;
    private int stopwatch = 0;
    private boolean completed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
        int accentColor = sharedPreferences.getInt("prefAccent", R.style.AppTheme);
        int primaryColor = sharedPreferences.getInt("prefPrimaryColor", getResources().getColor(R.color.primaryIndigo));
        int primaryDarkColor = sharedPreferences.getInt("prefPrimaryDarkColor", getResources().getColor(R.color.primaryIndigoDark));
        boolean colorNavBar = sharedPreferences.getBoolean("prefColorNavBar", false);

        if (colorNavBar){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setNavigationBarColor(primaryDarkColor);
            }
        }

        setTheme(accentColor);
        setContentView(R.layout.activity_edit_game);
        AdView mAdView = (AdView) findViewById(R.id.adViewHome);
        AdCreator adCreator = new AdCreator(mAdView, this);
        adCreator.createAd();

        AdView mAdView2 = (AdView) findViewById(R.id.adViewHome2);
        AdCreator adCreator2 = new AdCreator(mAdView2, this);
        adCreator2.createAd();
        getSupportActionBar();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(primaryDarkColor);
        }
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(primaryColor);
        setSupportActionBar(toolbar);
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
        editGameLayout = (RelativeLayout) findViewById(R.id.edit_game_content);

        buttonHelpDate = (ImageButton) findViewById(R.id.buttonHelpDate);
        buttonHelpLength = (ImageButton) findViewById(R.id.buttonHelpLength);
        buttonHelpDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helpDialog(getString(R.string.date_and_time_help), getString(R.string.date_and_time_help_message));
            }
        });
        buttonHelpLength.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helpDialog(getString(R.string.length_help), getString(R.string.length_help_message));
            }
        });


        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewEditGame);

        playerArray = dataHelper.getArrayById(ScoreDBAdapter.KEY_PLAYERS, gameID, dbHelper);
        scoreArray = dataHelper.getArrayById(ScoreDBAdapter.KEY_SCORE, gameID, dbHelper);
        maxScore = dataHelper.getIntByID( gameID,ScoreDBAdapter.KEY_MAX_SCORE, dbHelper);
        reverseScrolling = dataHelper.getIntByID( gameID,ScoreDBAdapter.KEY_REVERSE_SCORING, dbHelper);
        diffToWin = dataHelper.getIntByID( gameID,ScoreDBAdapter.KEY_DIFF_TO_WIN, dbHelper);
        stopwatch = dataHelper.getIntByID( gameID,ScoreDBAdapter.KEY_STOPWATCH, dbHelper);
        scoreInterval = dataHelper.getIntByID( gameID,ScoreDBAdapter.KEY_SCORE_INTERVAL, dbHelper);

        date = dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_TIME, dbHelper);

        editTextLength.setHint(dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_CHRONOMETER, dbHelper));
        editTextDate.setHint(dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_TIME, dbHelper));
        editTextLength.setEnabled(false);
        editTextDate.setEnabled(false);

        checkBoxReverseScrolling = (CheckBox) findViewById(R.id.checkBoxReverseScoring);
        checkBoxReverseScrolling.setOnClickListener(this);

        checkBoxStopwatch = (CheckBox) findViewById(R.id.checkBoxStopwatch);
        checkBoxStopwatch.setOnClickListener(this);

        editTextDiffToWin = (EditText) findViewById(R.id.editTextDiffToWin);
        editTextMaxScore = (EditText) findViewById(R.id.editTextMaxScore);
        editTextScoreInterval = (EditText) findViewById(R.id.editTextScoreInterval);

        if (maxScore != 0) {
            editTextMaxScore.setText(String.valueOf(maxScore));
        }
        if (scoreInterval !=0 && scoreInterval !=1) {
            editTextScoreInterval.setText(String.valueOf(scoreInterval));
        }

        if (diffToWin !=0) {
            editTextDiffToWin.setText(String.valueOf(diffToWin));
        }

        if (reverseScrolling == 1){
            checkBoxReverseScrolling.setChecked(true);
        }else{
            checkBoxReverseScrolling.setChecked(false);
        }

        if (stopwatch == 1){
            checkBoxStopwatch.setChecked(true);
        }else{
            checkBoxStopwatch.setChecked(false);
        }

        displayRecyclerView(0);
        checkBoxReverseScrolling.setEnabled(false);
        checkBoxStopwatch.setEnabled(false);
        editTextDate.setEnabled(false);
        editTextLength.setEnabled(false);
        editTextDiffToWin.setEnabled(false);
        editTextMaxScore.setEnabled(false);
        editTextScoreInterval.setEnabled(false);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.checkBoxReverseScoring: {
                if (checkBoxReverseScrolling.isChecked()){
                    reverseScrolling =1;
                }else{
                    reverseScrolling = 0;
                }

                break;
            }
            case R.id.checkBoxStopwatch: {
                if (checkBoxStopwatch.isChecked()){
                    stopwatch =1;
                }else{
                    stopwatch = 0;
                }


                break;
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        try {
            getMenuInflater().inflate(R.menu.main, menu);
            menuItemDelete = menu.findItem(R.id.action_delete);
            menuItemDone = menu.findItem(R.id.action_done);
            menuItemEdit = menu.findItem(R.id.action_edit);
            menuItemCancel = menu.findItem(R.id.action_cancel);
            menuItemAdd = menu.findItem(R.id.action_add);
            menuItemComplete = menu.findItem(R.id.complete_game);
            menuItemShare = menu.findItem(R.id.menu_item_share).setVisible(true);

            menu.findItem(R.id.action_delete).setVisible(true);
            menu.findItem(R.id.action_edit).setVisible(true);
            menu.findItem(R.id.action_settings).setVisible(false);
            menuItemComplete.setVisible(true);

            if (dataHelper.getCompletedById(gameID, dbHelper.open()) == 0){
                menuItemComplete.setTitle(R.string.complete);
                completed = true;
            }else{
                menuItemComplete.setTitle(R.string.unfinish);
                completed = false;
            }


            createShareIntent();
            // Fetch and store ShareActionProvider
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItemShare);
            mShareActionProvider.setShareIntent(mShareIntent);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
        return true;
    }


    public void createShareIntent(){
        mShareIntent = new Intent();
        mShareIntent.setAction(Intent.ACTION_SEND);
        mShareIntent.setType("text/plain");
        mShareIntent.putExtra(Intent.EXTRA_TEXT, "");
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

    public void completeGame(){
        if (completed) {
            dbHelper.updateGame(null, "1", 0, ScoreDBAdapter.KEY_COMPLETED, gameID);
        }else{
            dbHelper.updateGame(null, "0", 0, ScoreDBAdapter.KEY_COMPLETED, gameID);
        }

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

        } else if (id == R.id.action_edit) {
            onMenuEditClick();

        } else if (id == R.id.action_done) {
            onMenuDoneClick();

        } else if (id == R.id.action_cancel) {
            onMenuCancelClick();

        } else if (id == R.id.action_add){
            playerListAdapter.addPlayer();

        }else if (id == R.id.complete_game){
            completeGame();
        }

        return super.onOptionsItemSelected(item);
    }

    public void helpDialog(String title, String message){
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);


        builder.setTitle(title);

        builder.setMessage(message);

        builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        dialog = builder.create();
        dialog.show();
    }

    public void onMenuEditClick() {
        editTextLength.setEnabled(true);
        editTextDate.setEnabled(true);

        menuItemAdd.setVisible(true);
        menuItemDelete.setVisible(false);
        menuItemEdit.setVisible(false);
        menuItemDone.setVisible(true);
        menuItemCancel.setVisible(true);
        menuItemShare.setVisible(false);
        menuItemComplete.setVisible(false);

        checkBoxReverseScrolling.setEnabled(true);
        checkBoxStopwatch.setEnabled(true);
        editTextDate.setEnabled(true);
        editTextLength.setEnabled(true);
        editTextDiffToWin.setEnabled(true);
        editTextMaxScore.setEnabled(true);
        editTextScoreInterval.setEnabled(true);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        editTextLength.setText(dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_CHRONOMETER, dbHelper));
        editTextDate.setText(dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_TIME, dbHelper));
        displayRecyclerView(1);

        editTextDiffToWin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try
                {
                    diffToWin= Integer.parseInt(charSequence.toString());
                }
                catch (NumberFormatException e)
                {
                    e.printStackTrace();
                    diffToWin = 0;

                }


            }

            @Override
            public void afterTextChanged(Editable editable) {
                try
                {
                    diffToWin= Integer.parseInt(editable.toString());
                }
                catch (NumberFormatException e)
                {
                    e.printStackTrace();
                    diffToWin = 0;
                }

            }
        });

        editTextScoreInterval = (EditText) findViewById(R.id.editTextScoreInterval);
        editTextScoreInterval.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try
                {
                    scoreInterval= Integer.parseInt(charSequence.toString());
                }
                catch (NumberFormatException e) {
                    scoreInterval = 0;
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {
                try
                {
                    scoreInterval= Integer.parseInt(editable.toString());
                }
                catch (NumberFormatException e)
                {

                    scoreInterval = 0;
                }

            }
        });

        editTextMaxScore.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try
                {
                    maxScore= Integer.parseInt(charSequence.toString());
                }
                catch (NumberFormatException e)
                {
                    // handle the exception
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try
                {
                    maxScore= Integer.parseInt(editable.toString());
                }
                catch (NumberFormatException e)
                {
                    // handle the exception
                }
            }
        });
    }

    public void displayRecyclerView(int editable) {
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        playerArray = dataHelper.getArrayById(ScoreDBAdapter.KEY_PLAYERS, gameID, dbHelper);
        playerListAdapter = new PlayerListAdapter(playerArray, scoreArray, dbHelper, gameID, 2, editable);
        recyclerView.setAdapter(playerListAdapter);
    }

    public void onMenuDoneClick() {
        playerListAdapter.deleteEmptyPlayers(playerArray);
        playerArray = playerListAdapter.getPlayerArray();
        scoreArray = playerListAdapter.getScoreArray();

        final String newDate = editTextDate.getText().toString();
        final String newLength = editTextLength.getText().toString();

        if (!checkValidity(newLength, hourlengthFormat, 10) && newLength.length() != 0){
            dbHelper.open().updateGame(null, null, 1, ScoreDBAdapter.KEY_STOPWATCH, gameID);
            dbHelper.close();
            bLength = true;
            invalidSnackbar(getString(R.string.invalid_time));

        }else if (newLength.length() == 0|| newLength.equals("")){
            bLength = false;
            dbHelper.open().updateGame(null, null, 0, ScoreDBAdapter.KEY_STOPWATCH, gameID);
            dbHelper.close();

        }else if(checkValidity(newLength, hourlengthFormat, 10) && newLength.length() != 0){
            bLength = false;
            dbHelper.open().updateGame(null, null, 1, ScoreDBAdapter.KEY_STOPWATCH, gameID);
            dbHelper.close();
        }
        final boolean bDateAndTime = checkValidity(editTextDate.getText().toString(), dateTimeFormat, 19);
        final boolean bCheckEmpty = false;
        final boolean bCheckDuplicates = checkDuplicates(playerArray);
        final boolean bNumPlayers = PlayerListAdapter.checkNumberPlayers(playerArray);

        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.edit_game_question);

        builder.setMessage(R.string.are_you_sure_edit_game);

        builder.setPositiveButton(R.string.title_activity_edit_game, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                playerListAdapter.deleteEmptyPlayers(playerArray);
                playerArray = playerListAdapter.getPlayerArray();
                scoreArray = playerListAdapter.getScoreArray();

                if (bCheckEmpty) {

                    invalidSnackbar("You can't have empty names!");

                }else if (!bDateAndTime) {
                    invalidSnackbar(getString(R.string.invalid_date_and_time));

                } else if (bCheckDuplicates) {

                    invalidSnackbar("You can't have duplicate players!");

                } else if (bNumPlayers) {

                    invalidSnackbar("Must have 2 or more players");

                }else if (!bCheckEmpty && bDateAndTime && !bLength && !bCheckDuplicates && !bNumPlayers){
                    dbHelper.open();
                    dbHelper.updateGame(null, newDate,0, ScoreDBAdapter.KEY_TIME, gameID);

                    dbHelper.updateGame(playerArray,null,0, ScoreDBAdapter.KEY_PLAYERS, gameID);
                    dbHelper.updateGame(scoreArray, null,0, ScoreDBAdapter.KEY_SCORE, gameID);
                    dbHelper.updateGame(null, newLength,0, ScoreDBAdapter.KEY_CHRONOMETER, gameID);
                    dbHelper.open();
                    dbHelper.updateGame(null, null, maxScore, ScoreDBAdapter.KEY_MAX_SCORE, gameID);
                    dbHelper.open();
                    dbHelper.updateGame(null, null, scoreInterval, ScoreDBAdapter.KEY_SCORE_INTERVAL, gameID);
                    dbHelper.open();
                    dbHelper.updateGame(null, null, diffToWin, ScoreDBAdapter.KEY_DIFF_TO_WIN, gameID);
                    dbHelper.open();
                    dbHelper.updateGame(null, null,reverseScrolling,ScoreDBAdapter.KEY_REVERSE_SCORING, gameID);
                    dbHelper.open();
                    dbHelper.updateGame(null, null,stopwatch,ScoreDBAdapter.KEY_STOPWATCH, gameID);
                    editTextLength.setText(dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_CHRONOMETER, dbHelper));
                    editTextDate.setText(dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_TIME, dbHelper));

                    displayRecyclerView(0);
                    editTextLength.setEnabled(false);
                    editTextDate.setEnabled(false);
                    menuItemAdd.setVisible(false);
                    menuItemDelete.setVisible(true);
                    menuItemDone.setVisible(false);
                    menuItemEdit.setVisible(true);
                    menuItemCancel.setVisible(false);
                    menuItemShare.setVisible(true);
                    menuItemComplete.setVisible(true);
                    checkBoxReverseScrolling.setEnabled(false);
                    checkBoxStopwatch.setEnabled(false);
                    editTextDate.setEnabled(false);
                    editTextLength.setEnabled(false);
                    editTextDiffToWin.setEnabled(false);
                    editTextMaxScore.setEnabled(false);
                    editTextScoreInterval.setEnabled(false);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    dbHelper.close();
                }

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
        menuItemDone.setVisible(false);
        menuItemEdit.setVisible(true);
        menuItemAdd.setVisible(false);
        menuItemCancel.setVisible(false);
        menuItemShare.setVisible(true);
        menuItemComplete.setVisible(true);
        checkBoxReverseScrolling.setEnabled(false);
        checkBoxStopwatch.setEnabled(false);
        editTextDate.setEnabled(false);
        editTextLength.setEnabled(false);

        maxScore = dataHelper.getIntByID( gameID,ScoreDBAdapter.KEY_MAX_SCORE, dbHelper);
        reverseScrolling = dataHelper.getIntByID( gameID,ScoreDBAdapter.KEY_REVERSE_SCORING, dbHelper);
        diffToWin = dataHelper.getIntByID( gameID,ScoreDBAdapter.KEY_DIFF_TO_WIN, dbHelper);
        stopwatch = dataHelper.getIntByID( gameID,ScoreDBAdapter.KEY_STOPWATCH, dbHelper);
        scoreInterval = dataHelper.getIntByID( gameID,ScoreDBAdapter.KEY_SCORE_INTERVAL, dbHelper);
        scoreArray = dataHelper.getArrayById(ScoreDBAdapter.KEY_SCORE,gameID, dbHelper);

        editTextMaxScore.setText("");
        editTextScoreInterval.setText("");
        editTextDiffToWin.setText("");

        if (maxScore != 0) {
            editTextMaxScore.setText(String.valueOf(maxScore));
        }
        if (scoreInterval !=0) {
            editTextScoreInterval.setText(String.valueOf(scoreInterval));
        }

        if (diffToWin !=0) {
            editTextDiffToWin.setText(String.valueOf(diffToWin));
        }

        if (reverseScrolling == 1){
            checkBoxReverseScrolling.setChecked(true);
        }else{
            checkBoxReverseScrolling.setChecked(false);
        }

        if (stopwatch == 1){
            checkBoxStopwatch.setChecked(true);
        }else{
            checkBoxStopwatch.setChecked(false);
        }
        editTextDiffToWin.setEnabled(false);
        editTextMaxScore.setEnabled(false);
        editTextScoreInterval.setEnabled(false);
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

    public boolean checkDuplicates(ArrayList arrayList){

        boolean duplicate = false;

        Set<Integer> set = new HashSet<Integer>(arrayList);

        if(set.size() < arrayList.size()){
            duplicate = true;
        }

        return duplicate;
    }

    public interface PlayerListListener{
        void addPlayer();
        ArrayList getPlayerArray();
        ArrayList getScoreArray();
        void deleteEmptyPlayers(ArrayList playerArray);
    }
}
