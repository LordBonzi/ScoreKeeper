package io.github.sdsstudios.ScoreKeeper;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NewGame extends AppCompatActivity
        implements View.OnClickListener, RecyclerViewArrayAdapter.ViewHolder.ClickListener{

    public static PlayerListAdapter playerListAdapter;
    private Snackbar snackbar;
    private TimeLimitAdapter timeLimitAdapter;
    private PresetDBAdapter presetDBAdapter;
    private DataHelper dataHelper;
    private String time = null;
    private String TAG = "NewGame";
    private EditText editTextPlayer, editTextMaxScore, editTextScoreInterval, editTextDiffToWin;
    private Button buttonNewGame, buttonAddPlayer, buttonQuit, buttonCreatePreset;
    private CheckBox checkBoxNoTimeLimit, checkBoxReverseScrolling;
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
    private String defaultTitle;
    public static RelativeLayout relativeLayout;
    private RecyclerViewArrayAdapter arrayAdapter;
    private SharedPreferences sharedPreferences;
    static final String STATE_GAMEID = "gameID";
    private int reverseScrolling = 0;
    private int maxScore = 0;
    private int scoreInterval = 1;
    private int diffToWin = 0;

    private RelativeLayout optionsHeader, timeLimitHeader;
    private NestedScrollView scrollView;
    private CardView cardViewOptions, cardViewTimeLimit;
    private int cardViewOptionsHeight, cardViewTimeLimitHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null){
            loadActivity(savedInstanceState);
        }else{
            loadActivity(savedInstanceState);

        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        stop = false;
        dbHelper.open();
        dbHelper.updateGame(players, null, 0, ScoreDBAdapter.KEY_PLAYERS, gameID);
        createScoreArray();
        dbHelper.close();
        savedInstanceState.putInt(STATE_GAMEID, gameID);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public void loadActivity(Bundle savedInstanceState){
        sharedPreferences = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
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
        setContentView(R.layout.activity_new_game);
        AdView mAdView = (AdView) findViewById(R.id.adViewHome);
        AdCreator adCreator = new AdCreator(mAdView, this);
        adCreator.createAd();
        getSupportActionBar();
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(primaryColor);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(primaryDarkColor);
        }
        dbHelper = new ScoreDBAdapter(this);
        dataHelper = new DataHelper();
        String timeLimit = sharedPreferences.getString("timelimitarray", null);
        String timeLimitnum = sharedPreferences.getString("timelimitarraynum", null);

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

        scrollView = (NestedScrollView) findViewById(R.id.scrollView);

        optionsHeader = (RelativeLayout) findViewById(R.id.optionsHeader);
        optionsHeader.setOnClickListener(this);

        timeLimitHeader = (RelativeLayout) findViewById(R.id.timeLimitHeader);
        timeLimitHeader.setOnClickListener(this);

        cardViewOptions= (CardView)findViewById(R.id.cardViewOptions);
        cardViewOptions.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                cardViewOptionsHeight = cardViewOptions.getMeasuredHeight();
                // Do whatever you want with h
                // Remove the listener so it is not called repeatedly
                removeOnGlobalLayoutListener(cardViewOptions, this);
            }
        });

        cardViewTimeLimit= (CardView)findViewById(R.id.cardViewTimeLimit);
        cardViewTimeLimit.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                cardViewTimeLimitHeight = cardViewTimeLimit.getMeasuredHeight();
                // Do whatever you want with h
                // Remove the listener so it is not called repeatedly
                removeOnGlobalLayoutListener(cardViewTimeLimit, this);
            }
        });

        relativeLayout = (RelativeLayout)findViewById(R.id.newGameLayout);
        spinnerTimeLimit = (Spinner)findViewById(R.id.spinnerTimeLimit);
        spinnerPreset = (Spinner)findViewById(R.id.spinnerPreset);

        checkBoxNoTimeLimit = (CheckBox) findViewById(R.id.checkBoxNoTimeLimit);
        checkBoxNoTimeLimit.setOnClickListener(this);
        checkBoxNoTimeLimit.setChecked(false);

        checkBoxReverseScrolling = (CheckBox) findViewById(R.id.checkBoxReverseScoring);
        checkBoxReverseScrolling.setOnClickListener(this);
        checkBoxReverseScrolling.setChecked(false);

        buttonCreatePreset = (Button)findViewById(R.id.buttonCreatePreset);
        buttonCreatePreset.setOnClickListener(this);
        buttonCreatePreset.setVisibility(View.VISIBLE);

        presetDBAdapter = new PresetDBAdapter(this);

        homeIntent = new Intent(this, Home.class);
        playerList = (RecyclerView)findViewById(R.id.playerList);

        if (savedInstanceState != null){
            dbHelper.open();
            gameID = savedInstanceState.getInt(STATE_GAMEID);
            players = dataHelper.getArrayById(ScoreDBAdapter.KEY_PLAYERS, gameID, dbHelper);
            score = dataHelper.getArrayById(ScoreDBAdapter.KEY_SCORE, gameID, dbHelper);
            timeLimit = dataHelper.getStringById( gameID,ScoreDBAdapter.KEY_TIMER, dbHelper);
            maxScore = dataHelper.getIntByID( gameID,ScoreDBAdapter.KEY_MAX_SCORE, dbHelper);
            reverseScrolling = dataHelper.getIntByID( gameID,ScoreDBAdapter.KEY_REVERSE_SCORING, dbHelper);
            dbHelper.open();
            dbHelper.updateGame(null, timeLimit, 0, ScoreDBAdapter.KEY_TIMER, gameID);
            dbHelper.close();
            if (timeLimit== null){
                checkBoxNoTimeLimit.setChecked(false);
                spinnerTimeLimit.setVisibility(View.INVISIBLE);
            }else{
                checkBoxNoTimeLimit.setChecked(true);
                spinnerTimeLimit.setVisibility(View.VISIBLE);
                spinnerTimeLimit.setSelection(timeLimitArray.indexOf(timeLimit));

            }

            dbHelper.updateGame(players, null, 0, ScoreDBAdapter.KEY_PLAYERS, gameID);
            displayRecyclerView(players);
            dbHelper.close();
        }else{
            dbHelper.open();
            dbHelper.createGame(players, time, score, 0, timeLimit, maxScore, reverseScrolling, scoreInterval, diffToWin);
            gameID = dbHelper.getNewestGame();
            dbHelper.close();
            spinnerTimeLimit.setVisibility(View.INVISIBLE);
            displayRecyclerView(players);
            timeLimit = null;
            dbHelper.open();
            dbHelper.updateGame(null, timeLimit,0, ScoreDBAdapter.KEY_TIMER, gameID);
            dbHelper.close();
        }


        buttonNewGame = (Button)findViewById(R.id.buttonNewGame);
        buttonNewGame.setOnClickListener(this);

        buttonQuit = (Button)findViewById(R.id.buttonQuit);
        buttonQuit.setOnClickListener(this);

        buttonAddPlayer = (Button) findViewById(R.id.buttonAddPlayer);
        buttonAddPlayer.setOnClickListener(this);

        editTextPlayer = (EditText) findViewById(R.id.editTextPlayer);
        editTextDiffToWin = (EditText) findViewById(R.id.editTextDiffToWin);
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
                    Log.e(TAG, e.toString());
                    diffToWin = 0;

                }

                dbHelper.open();
                dbHelper.updateGame(null, null, diffToWin, ScoreDBAdapter.KEY_DIFF_TO_WIN, gameID);
                dbHelper.close();
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
                    Log.e(TAG, e.toString());
                    diffToWin = 0;
                }
                dbHelper.open();
                dbHelper.updateGame(null, null, diffToWin, ScoreDBAdapter.KEY_DIFF_TO_WIN, gameID);
                dbHelper.close();
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
                catch (NumberFormatException e)
                {

                    scoreInterval = 0;

                }

                dbHelper.open();
                dbHelper.updateGame(null, null, scoreInterval, ScoreDBAdapter.KEY_SCORE_INTERVAL, gameID);
                dbHelper.close();
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
                dbHelper.open();
                dbHelper.updateGame(null, null, scoreInterval, ScoreDBAdapter.KEY_SCORE_INTERVAL, gameID);
                dbHelper.close();
            }
        });

        editTextMaxScore = (EditText) findViewById(R.id.editTextMaxScore);
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
                dbHelper.open();
                dbHelper.updateGame(null, null, maxScore, ScoreDBAdapter.KEY_MAX_SCORE, gameID);
                dbHelper.close();
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
                dbHelper.open();
                dbHelper.updateGame(null, null, maxScore, ScoreDBAdapter.KEY_MAX_SCORE, gameID);
                dbHelper.close();
            }
        });

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        time = sdfDate.format(now);
        dbHelper.open();
        dbHelper.updateGame(null, time,0, ScoreDBAdapter.KEY_TIME, gameID);
        dbHelper.close();

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


        displaySpinner(true);
        displaySpinner(false);
    }

    public static void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener victim) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            removeLayoutListenerJB(v, victim);
        } else removeLayoutListener(v, victim);
    }

    @SuppressWarnings("deprecation")
    private static void removeLayoutListenerJB(View v, ViewTreeObserver.OnGlobalLayoutListener victim) {
        v.getViewTreeObserver().removeGlobalOnLayoutListener(victim);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static void removeLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener victim) {
        v.getViewTreeObserver().removeOnGlobalLayoutListener(victim);
    }

    public void preDrawCardView(final CardView cardView, final int height){
        cardView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

            @Override
            public boolean onPreDraw() {
                ViewGroup.LayoutParams layoutParams = null;

                cardView.getViewTreeObserver().removeOnPreDrawListener(this);

                layoutParams = cardView.getLayoutParams();
                layoutParams.height = height * 2;

                cardView.setLayoutParams(layoutParams);

                return true;
            }
        });
    }

    public void displaySpinner(boolean timelimitspinner){

        if (timelimitspinner) {
            timeLimitAdapter = new TimeLimitAdapter(this, timeLimitArray, timeLimitArrayNum, dbHelper, gameID);
            spinnerTimeLimit.setAdapter(timeLimitAdapter);
        }else {
            ArrayList titleArrayList = new ArrayList();
            titleArrayList.add("No Preset");

            for (int i = 1; i <= presetDBAdapter.open().numRows(); i++){
                presetDBAdapter.open();
                titleArrayList.add(dataHelper.getPresetStringByID(i, PresetDBAdapter.KEY_TITLE, presetDBAdapter));
                presetDBAdapter.close();
            }

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, titleArrayList);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            spinnerPreset.setAdapter(dataAdapter);

            spinnerPreset.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (spinnerPreset.getSelectedItemPosition() != 0){
                        loadGame(spinnerPreset.getSelectedItemPosition());

                    }else{
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }

    }

    public void loadGame(int position){
        dataHelper = new DataHelper();
        presetDBAdapter.open();
        ArrayList presetPlayers;
        String presetTimeLimit;
        presetPlayers = dataHelper.getPresetPlayerArrayByID(position, presetDBAdapter);
        presetTimeLimit = dataHelper.getPresetStringByID(position, PresetDBAdapter.KEY_TIME_LIMIT, presetDBAdapter);
        int maxscore = dataHelper.getPresetIntByID(position, PresetDBAdapter.KEY_MAX_SCORE, presetDBAdapter);
        int reversescrolling = dataHelper.getPresetIntByID(position, PresetDBAdapter.KEY_REVERSE_SCORING, presetDBAdapter);
        int scoreinterval = dataHelper.getPresetIntByID(position, PresetDBAdapter.KEY_SCORE_INTERVAL, presetDBAdapter);
        int diffToWin = dataHelper.getPresetIntByID(position, PresetDBAdapter.KEY_DIFF_TO_WIN, presetDBAdapter);
        presetDBAdapter.close();

        updateEditText(presetPlayers, presetTimeLimit, maxscore, reversescrolling, scoreinterval, diffToWin);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_delete).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_edit_presets).setVisible(true);
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
        }if (id == R.id.action_edit_presets){
            if (presetDBAdapter.open().numRows() != 0){
                deletePresetsDialog();
            }else{
                Toast.makeText(this, "No Presets Created", Toast.LENGTH_SHORT).show();
            }

        }if (id == R.id.action_reset){
            reset();
        }

        return super.onOptionsItemSelected(item);
    }

    public void reset(){
        players.clear();
        score.clear();
        displayRecyclerView(players);
        checkBoxNoTimeLimit.setChecked(false);
        disableTimeLimitSpinner();
        checkBoxReverseScrolling.setChecked(false);
        editTextMaxScore.setText("");
        editTextMaxScore.setHint(getString(R.string.max_score));
        editTextScoreInterval.setText("");
        editTextScoreInterval.setHint(getString(R.string.score_interval));
        editTextDiffToWin.setText("");
        editTextDiffToWin.setHint(getString(R.string.diff_to_win));
        spinnerPreset.setSelection(0);

    }

    public void displayRecyclerView(ArrayList players){
        playerList.setVisibility(View.VISIBLE);
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
            dbHelper.updateGame(players, null,0, ScoreDBAdapter.KEY_PLAYERS,gameID);
            dbHelper.updateGame(null, "00:00:00:0",0, ScoreDBAdapter.KEY_CHRONOMETER,gameID);
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
        dbHelper.updateGame(score, null,0, ScoreDBAdapter.KEY_SCORE, gameID);
        dbHelper.close();

    }

    public void deletePresetsDialog(){
        final View dialogView;
        final ArrayList titleArrayList  =new ArrayList();

        for (int i = 1; i <= presetDBAdapter.open().numRows(); i++){
            presetDBAdapter.open();
            titleArrayList.add(dataHelper.getPresetStringByID(i, PresetDBAdapter.KEY_TITLE, presetDBAdapter));
            presetDBAdapter.close();
        }

        arrayAdapter = new RecyclerViewArrayAdapter(titleArrayList, this, this);
        LayoutInflater inflter = LayoutInflater.from(this);
        final AlertDialog alertDialog;
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogView = inflter.inflate(R.layout.recyclerview_fragment, null);
        final RecyclerView recyclerView = (RecyclerView) dialogView.findViewById(R.id.recyclerViewFragment);

        dialogBuilder.setTitle(getResources().getString(R.string.delete_presets));
        dialogBuilder.setMessage(getResources().getString(R.string.delete_presets_message));
        dialogBuilder.setNeutralButton(R.string.delete_all, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                presetDBAdapter.open();
                presetDBAdapter.deleteAllPresets();
                presetDBAdapter.close();
                displaySpinner(false);
            }
        });

        dialogBuilder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                presetDBAdapter.open();
                arrayAdapter.deleteSelectedPresets(presetDBAdapter);
                presetDBAdapter.close();
                displaySpinner(false);

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

    public void disableTimeLimitSpinner(){
        spinnerTimeLimit.setEnabled(false);
        spinnerTimeLimit.setVisibility(View.INVISIBLE);
        timeLimit = null;
        dbHelper.open();
        timeLimit=null;
        dbHelper.updateGame(null, timeLimit,0, ScoreDBAdapter.KEY_TIMER, gameID);
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
                createNewGame(players, true);

                break;

            }

            case R.id.checkBoxNoTimeLimit: {
                if (checkBoxNoTimeLimit.isChecked()){
                    spinnerTimeLimit.setEnabled(true);
                    spinnerTimeLimit.setVisibility(View.VISIBLE);
                    dbHelper.open();
                    dbHelper.updateGame(null, timeLimitArrayNum.get(0).toString(),0,ScoreDBAdapter.KEY_TIMER, gameID);
                    dbHelper.close();

                }else{
                    disableTimeLimitSpinner();
                }

                break;
            }

            case R.id.checkBoxReverseScoring: {
                if (checkBoxReverseScrolling.isChecked()){
                    reverseScrolling =1;
                }else{
                    reverseScrolling = 0;
                }
                dbHelper.open();
                dbHelper.updateGame(null, null,reverseScrolling,ScoreDBAdapter.KEY_REVERSE_SCORING, gameID);
                dbHelper.close();

                break;
            }

            case R.id.buttonCreatePreset:{
                createNewGame(players, false);

                break;
            }

            case R.id.optionsHeader:{
                toggleCardViewnHeight(cardViewOptionsHeight, cardViewOptions, scrollView.getBottom());
                break;
            }

            case R.id.timeLimitHeader:{
                toggleCardViewnHeight(cardViewTimeLimitHeight, cardViewTimeLimit, 0);
                break;
            }

        }
    }

    private void toggleCardViewnHeight(int height, CardView cardView, int scrollTo) {

        if (cardView.getHeight() != height) {
            // expand

            expandView(height, cardView, scrollTo); //'height' is the height of screen which we have measured already.

        } else {
            // collapse
            collapseView(cardView);

        }
    }

    public void collapseView(final CardView cardView) {

        ValueAnimator anim = ValueAnimator.ofInt(cardView.getMeasuredHeightAndState(), 96);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = cardView.getLayoutParams();
                layoutParams.height = val;
                cardView.setLayoutParams(layoutParams);

            }
        });
        anim.start();
    }
    public void expandView(int height, final CardView cardView, final int scrollTo) {

        ValueAnimator anim = ValueAnimator.ofInt(cardView.getMeasuredHeightAndState(),
                height);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = cardView.getLayoutParams();
                layoutParams.height = val;
                cardView.setLayoutParams(layoutParams);
                if (scrollTo != 0) {
                    scrollView.scrollTo(0, scrollTo);
                }

            }
        });
        anim.start();

    }

    public void createPresetDialog() {
        final View dialogView;

        LayoutInflater inflter = LayoutInflater.from(this);
        final AlertDialog alertDialog;
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogView = inflter.inflate(R.layout.create_preset_fragment, null);
        final EditText editTextPresetTitle = (EditText) dialogView.findViewById(R.id.editTextPresetTitle);
        dialogBuilder.setPositiveButton(R.string.create, null);

        dialogBuilder.setTitle(getResources().getString(R.string.create_preset));
        dialogBuilder.setMessage(getResources().getString(R.string.create_preset_message));
        dialogBuilder.setNeutralButton(R.string.default_title, null);


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

                defaultTitle = editTextPresetTitle.getHint().toString();
                 title[0] = editTextPresetTitle.getHint().toString();

                editTextPresetTitle.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        title[0] = charSequence.toString();
                        if (charSequence == ""){
                            title[0]=defaultTitle;
                        }

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
        Button b1 = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextPresetTitle.setText(defaultTitle);
            }
        });


    }

    public void createNewGame(ArrayList players, boolean startGame) {
            mainActivityIntent = new Intent(this, MainActivity.class);
            stop = false;
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                }
            };

            //snackbar must have 2 or more players

            if (players.size() < 2) {

                snackbar = Snackbar.make(relativeLayout, R.string.more_than_two_players, Snackbar.LENGTH_SHORT)
                        .setAction("Dismiss", onClickListener);
                snackbar.show();
            } else {
                if (dataHelper.checkDuplicates(players)) {


                    snackbar = Snackbar.make(relativeLayout, R.string.duplicates_message, Snackbar.LENGTH_SHORT)
                            .setAction("Dismiss", onClickListener);
                    snackbar.show();

                }else {
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
        if (spinnerTimeLimit.getSelectedItemPosition()+1 != timeLimitArrayNum.size()) {
            presetDBAdapter.open();
            presetDBAdapter.createPreset(players, timeLimitArrayNum.get(spinnerTimeLimit.getSelectedItemPosition()).toString()
                    , title, maxScore, reverseScrolling, scoreInterval, diffToWin);
            presetDBAdapter.close();
        }else{
            presetDBAdapter.createPreset(players, timeLimit, title, maxScore, reverseScrolling, scoreInterval, diffToWin);
        }
        presetDBAdapter.close();
    }

    public void updateEditText(ArrayList players, String timeLimit, int maxScore, int reverseScrolling, int scoreInterval, int difftowin) {
        this.players = players;
        this.timeLimit = timeLimit;
        this.maxScore = maxScore;
        this.reverseScrolling = reverseScrolling;
        this.scoreInterval = scoreInterval;
        this.diffToWin = difftowin;
        displayRecyclerView(players);
        if (timeLimit!=null) {
            checkBoxNoTimeLimit.setChecked(true);
            spinnerTimeLimit.setEnabled(true);
            spinnerTimeLimit.setVisibility(View.VISIBLE);

        }else{
            checkBoxNoTimeLimit.setChecked(false);
            spinnerTimeLimit.setVisibility(View.INVISIBLE);
            spinnerTimeLimit.setEnabled(false);
        }

        if (maxScore != 0) {
            editTextMaxScore.setText(String.valueOf(maxScore));
        }
        if (scoreInterval !=0) {
            editTextScoreInterval.setText(String.valueOf(scoreInterval));
        }

        if (difftowin !=0) {
            editTextDiffToWin.setText(String.valueOf(difftowin));
        }

        if (reverseScrolling == 1){
            checkBoxReverseScrolling.setChecked(true);
        }else{
            checkBoxReverseScrolling.setChecked(false);
        }

        createScoreArray();
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        time = sdfDate.format(now);
        dbHelper.open();
        dbHelper.updateGame(null, time,0, ScoreDBAdapter.KEY_TIME, gameID);
        dbHelper.open();
        dbHelper.updateGame(players, null,0, ScoreDBAdapter.KEY_PLAYERS, gameID);
        dbHelper.open();
        dbHelper.updateGame(null, timeLimit,0, ScoreDBAdapter.KEY_TIMER, gameID);
        dbHelper.open();
        dbHelper.updateGame(null, timeLimit,maxScore, ScoreDBAdapter.KEY_MAX_SCORE, gameID);
        dbHelper.open();
        dbHelper.updateGame(null, timeLimit,reverseScrolling, ScoreDBAdapter.KEY_REVERSE_SCORING, gameID);
        dbHelper.close();
    }

    @Override
    public void onItemClicked(int position, int gameID) {
        arrayAdapter.toggleSelection(position, gameID);
    }
}
