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
import java.util.List;
import java.util.Set;

public class EditGame extends AppCompatActivity {
    String date;
    int gameID;
    private String lengthStr =null;
    private EditText editTextLength, editTextDate;
    private ArrayList<String> playerArray, scoreArray;
    private RecyclerView recyclerView;
    private ScoreDBAdapter dbHelper;
    private DataHelper dataHelper;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private PlayerListAdapter playerListAdapter;
    public static RelativeLayout editGameLayout;
    SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat hourlengthFormat = new SimpleDateFormat("hh:mm:ss:S");
    boolean bLength = false;
    private MenuItem menuItemDelete, menuItemEdit, menuItemDone, menuItemCancel, menuItemAdd
            , menuItemShare, menuItemComplete;
    private ShareActionProvider mShareActionProvider;
    private Intent mShareIntent;
    private ImageButton buttonHelpLength, buttonHelpDate;
    private boolean completed = false;

    private List<EditTextOption> mEditTextOptions = new ArrayList<>();
    private List<CheckBoxOption> mCheckBoxOptions = new ArrayList<>();
    private List<MenuItem> mMenuItemList = new ArrayList<>();

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

        date = dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_TIME, dbHelper);

        mEditTextOptions.add(new EditTextOption((EditText) findViewById(R.id.editTextNumSets), 0, ScoreDBAdapter.KEY_NUM_SETS, getString(R.string.num_sets)));
        mEditTextOptions.add(new EditTextOption((EditText) findViewById(R.id.editTextDiffToWin), 0, ScoreDBAdapter.KEY_DIFF_TO_WIN, getString(R.string.diff_to_win)));
        mEditTextOptions.add(new EditTextOption((EditText) findViewById(R.id.editTextMaxScore), 0, ScoreDBAdapter.KEY_MAX_SCORE, getString(R.string.max_score)));
        mEditTextOptions.add(new EditTextOption((EditText) findViewById(R.id.editTextScoreInterval), 0, ScoreDBAdapter.KEY_SCORE_INTERVAL, getString(R.string.score_interval)));

        mCheckBoxOptions.add(new CheckBoxOption((CheckBox) findViewById(R.id.checkBoxReverseScoring)
                , 0, ScoreDBAdapter.KEY_REVERSE_SCORING, getString(R.string.reverse_scoring)));

        mCheckBoxOptions.add(new CheckBoxOption((CheckBox) findViewById(R.id.checkBoxStopwatch)
                , 0, ScoreDBAdapter.KEY_STOPWATCH, getString(R.string.time_limit)));

        for (EditTextOption e : mEditTextOptions){
            e.setmData(dataHelper.getIntByID( gameID, e.getmDatabaseColumn(), dbHelper));

            if (e.getmData() != 0){
                e.getmEditText().setText(String.valueOf(e.getmData()));
            }

            e.getmEditText().setEnabled(false);
        }

        for (final CheckBoxOption c : mCheckBoxOptions){
            c.setmData(dataHelper.getIntByID( gameID, c.getmDatabaseColumn(), dbHelper));

            c.getmCheckBox().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (c.getmCheckBox().isChecked()){
                        c.setmData(1);
                    }else {
                        c.setmData(0);

                    }
                }
            });

            if (c.getmData() == 1){
                c.getmCheckBox().setChecked(true);
            }else{
                c.getmCheckBox().setChecked(false);
            }

            c.getmCheckBox().setEnabled(false);

        }

        editTextLength.setHint(dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_CHRONOMETER, dbHelper));
        editTextDate.setHint(dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_TIME, dbHelper));
        editTextLength.setEnabled(false);
        editTextDate.setEnabled(false);

        displayRecyclerView(0);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        try {
            getMenuInflater().inflate(R.menu.main, menu);
            mMenuItemList.add(menuItemDelete = menu.findItem(R.id.action_delete));
            mMenuItemList.add(menuItemDone = menu.findItem(R.id.action_done));
            mMenuItemList.add(menuItemEdit = menu.findItem(R.id.action_edit));
            mMenuItemList.add(menuItemCancel = menu.findItem(R.id.action_cancel));
            mMenuItemList.add(menuItemAdd = menu.findItem(R.id.action_add));
            mMenuItemList.add(menuItemComplete = menu.findItem(R.id.complete_game));
            mMenuItemList.add(menuItemShare = menu.findItem(R.id.menu_item_share).setVisible(true));

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

        for (CheckBoxOption c: mCheckBoxOptions){
            c.getmCheckBox().setEnabled(true);
        }

        for (final EditTextOption e: mEditTextOptions){
            e.getmEditText().setEnabled(true);

            e.getmEditText().addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    try
                    {
                        e.setmData(Integer.parseInt(charSequence.toString()));
                    }
                    catch (NumberFormatException error)
                    {
                        error.printStackTrace();
                        e.setmData(0);

                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        editTextLength.setText(dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_CHRONOMETER, dbHelper));
        editTextDate.setText(dataHelper.getStringById(gameID, ScoreDBAdapter.KEY_TIME, dbHelper));
        displayRecyclerView(1);

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

                    dbHelper.open();
                    dbHelper.updateGame(playerArray,null,0, ScoreDBAdapter.KEY_PLAYERS, gameID);

                    dbHelper.open();
                    dbHelper.updateGame(scoreArray, null,0, ScoreDBAdapter.KEY_SCORE, gameID);

                    dbHelper.open();
                    dbHelper.updateGame(null, newLength,0, ScoreDBAdapter.KEY_CHRONOMETER, gameID);

                    for (CheckBoxOption c : mCheckBoxOptions){
                        dbHelper.open();
                        dbHelper.updateGame(null, null, c.getmData(), c.getmDatabaseColumn(), gameID);

                        c.getmCheckBox().setEnabled(false);
                    }
                    for (EditTextOption e : mEditTextOptions){
                        dbHelper.open();
                        dbHelper.updateGame(null, null, e.getmData(), e.getmDatabaseColumn(), gameID);

                        e.getmEditText().setEnabled(false);
                    }

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
                    editTextDate.setEnabled(false);
                    editTextLength.setEnabled(false);

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
        editTextDate.setEnabled(false);
        editTextLength.setEnabled(false);

        for (CheckBoxOption c : mCheckBoxOptions){
            c.getmCheckBox().setEnabled(false);
            c.setmData(dataHelper.getIntByID( gameID,c.getmDatabaseColumn(), dbHelper));

            if (c.getmData() == 1){
                c.getmCheckBox().setChecked(true);
            }else{
                c.getmCheckBox().setChecked(false);
            }
        }

        for (EditTextOption e : mEditTextOptions){
            e.getmEditText().setEnabled(false);
            e.setmData(dataHelper.getIntByID( gameID,e.getmDatabaseColumn(), dbHelper));

            e.getmEditText().setText("");
            if (e.getmData() != 0) {
                e.getmEditText().setText(String.valueOf(e.getmData()));
            }
        }
        scoreArray = dataHelper.getArrayById(ScoreDBAdapter.KEY_SCORE,gameID, dbHelper);

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
                startActivity(new Intent(EditGame.this, History.class));
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
