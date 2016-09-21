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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
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
    private String defaultTitle;
    public static RelativeLayout relativeLayout;
    private RecyclerViewArrayAdapter arrayAdapter;
    private SharedPreferences sharedPreferences;
    private boolean classicTheme;
    static final String STATE_GAMEID = "gameID";
    private RelativeLayout optionsHeader, timeLimitHeader;
    private NestedScrollView scrollView;
    private int optionsHeight, timeLimitHeight;
    private RelativeLayout optionsContent, timeLimitContent;

    private List<EditTextOption> mEditTextOptions = new ArrayList<>();
    private List<CheckBoxOption> mCheckBoxOptions = new ArrayList<>();

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
        classicTheme = sharedPreferences.getBoolean("prefClassicTheme", false);
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
        final String timeLimitnum = sharedPreferences.getString("timelimitarraynum", null);

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

        optionsContent = (RelativeLayout)findViewById(R.id.relativeLayoutOptions);
        timeLimitContent = (RelativeLayout)findViewById(R.id.relativeLayoutTimeLimit);

        optionsContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                optionsHeight = optionsContent.getMeasuredHeight();
                toggleCardViewHeight(optionsHeight, optionsContent, 0);
                // Do whatever you want with h
                // Remove the listener so it is not called repeatedly
                removeOnGlobalLayoutListener(optionsContent, this);
            }
        });

        timeLimitContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                timeLimitHeight = timeLimitContent.getMeasuredHeight();
                toggleCardViewHeight(timeLimitHeight, timeLimitContent, 0);
                // Do whatever you want with h
                // Remove the listener so it is not called repeatedly
                removeOnGlobalLayoutListener(timeLimitContent, this);
            }
        });


        relativeLayout = (RelativeLayout)findViewById(R.id.newGameLayout);
        spinnerTimeLimit = (Spinner)findViewById(R.id.spinnerTimeLimit);
        spinnerPreset = (Spinner)findViewById(R.id.spinnerPreset);

        checkBoxNoTimeLimit = (CheckBox) findViewById(R.id.checkBoxNoTimeLimit);
        checkBoxNoTimeLimit.setOnClickListener(this);
        checkBoxNoTimeLimit.setChecked(false);

        buttonCreatePreset = (Button)findViewById(R.id.buttonCreatePreset);
        buttonCreatePreset.setOnClickListener(this);
        buttonCreatePreset.setVisibility(View.VISIBLE);

        presetDBAdapter = new PresetDBAdapter(this);

        homeIntent = new Intent(this, Home.class);
        playerList = (RecyclerView)findViewById(R.id.playerList);

        mEditTextOptions.add(new EditTextOption((EditText) findViewById(R.id.editTextNumSets), 0, ScoreDBAdapter.KEY_NUM_SETS, getString(R.string.num_sets)));
        mEditTextOptions.add(new EditTextOption((EditText) findViewById(R.id.editTextDiffToWin), 0, ScoreDBAdapter.KEY_DIFF_TO_WIN, getString(R.string.diff_to_win)));
        mEditTextOptions.add(new EditTextOption((EditText) findViewById(R.id.editTextMaxScore), 0, ScoreDBAdapter.KEY_MAX_SCORE, getString(R.string.max_score)));
        mEditTextOptions.add(new EditTextOption((EditText) findViewById(R.id.editTextScoreInterval), 0, ScoreDBAdapter.KEY_SCORE_INTERVAL, getString(R.string.score_interval)));

        mCheckBoxOptions.add(new CheckBoxOption((CheckBox) findViewById(R.id.checkBoxReverseScoring)
                , 0, ScoreDBAdapter.KEY_REVERSE_SCORING, getString(R.string.reverse_scoring)));

        mCheckBoxOptions.add(new CheckBoxOption((CheckBox) findViewById(R.id.checkBoxStopwatch)
                , 0, ScoreDBAdapter.KEY_STOPWATCH, getString(R.string.stopwatch)));



        if (savedInstanceState != null){
            dbHelper.open();
            gameID = savedInstanceState.getInt(STATE_GAMEID);
            players = dataHelper.getArrayById(ScoreDBAdapter.KEY_PLAYERS, gameID, dbHelper);
            score = dataHelper.getArrayById(ScoreDBAdapter.KEY_SCORE, gameID, dbHelper);
            timeLimit = dataHelper.getStringById( gameID,ScoreDBAdapter.KEY_TIMER, dbHelper);

            for (EditTextOption editTextOption : mEditTextOptions){
                editTextOption.setmData(dataHelper.getIntByID(gameID,editTextOption.getmDatabaseColumn(), dbHelper));
            }



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
            dbHelper.createGame(players, time, score, 0, timeLimit, mEditTextOptions, mCheckBoxOptions);
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

        for (final CheckBoxOption c : mCheckBoxOptions){
            c.getmCheckBox().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (c.getmCheckBox().isChecked()){
                        c.setmData(1);
                    }else{
                        c.setmData(0);
                    }
                    dbHelper.open();
                    dbHelper.updateGame(null, null,c.getmData(),c.getmDatabaseColumn(), gameID);
                    dbHelper.close();

                }
            });
            c.getmCheckBox().setChecked(false);
        }

        for (final EditTextOption e: mEditTextOptions){
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

                    dbHelper.open();
                    dbHelper.updateGame(null, null, e.getmData(), e.getmDatabaseColumn(), gameID);
                    dbHelper.close();
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }

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

        for (EditTextOption e : mEditTextOptions){
            e.setmData(dataHelper.getPresetIntByID(position, e.getmDatabaseColumn(), presetDBAdapter));
        }

        for (CheckBoxOption c : mCheckBoxOptions){
            c.setmData(dataHelper.getPresetIntByID(position, c.getmDatabaseColumn(), presetDBAdapter));
        }

        presetDBAdapter.close();

        editTextPlayer.setText("");

        updateEditText(presetPlayers, presetTimeLimit);

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

        for (CheckBoxOption c : mCheckBoxOptions){
            c.getmCheckBox().setChecked(false);
        }

        for (EditTextOption editTextOption: mEditTextOptions){
            editTextOption.getmEditText().setText("");
            editTextOption.getmEditText().setHint(editTextOption.getmHint());
        }

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
        player = editTextPlayer.getText().toString().trim();
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

            players.remove(players.size() - 1);

            snackbar = Snackbar.make(relativeLayout, R.string.duplicates_message, Snackbar.LENGTH_SHORT)
                    .setAction("Dismiss", onClickListener);
            snackbar.show();
        }

        if (player.equals("") || player.equals(" ") || player == null){
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
        }else if (!duplicates && !player.equals("") && !player.equals(" ") && player != null){

            editTextPlayer.setText("");

            dbHelper.open();
            dbHelper.updateGame(players, null,0, ScoreDBAdapter.KEY_PLAYERS,gameID);
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
        score = new ArrayList<>();

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

        arrayAdapter = new RecyclerViewArrayAdapter(titleArrayList, this, this, 1);
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
                arrayAdapter.deleteSelectedPresets(presetDBAdapter, 0);
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


            case R.id.buttonCreatePreset:{
                createNewGame(players, false);

                break;
            }

            case R.id.optionsHeader:{
                toggleCardViewHeight(optionsHeight, optionsContent, scrollView.getBottom());
                break;
            }
            case R.id.timeLimitHeader:{
                toggleCardViewHeight(timeLimitHeight, timeLimitContent, scrollView.getBottom());
                break;
            }

        }
    }

    private void toggleCardViewHeight(int height, RelativeLayout layout, int scrollTo) {

        if (layout.getHeight() != height) {
            // expand

            expandView(height, layout, scrollTo); //'height' is the height of screen which we have measured already.

        } else {
            // collapse
            collapseView(layout);

        }
    }

    public void collapseView(final RelativeLayout layout) {

        ValueAnimator anim = ValueAnimator.ofInt(timeLimitHeight, 0);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = layout.getLayoutParams();
                layoutParams.height = val;
                layout.setLayoutParams(layoutParams);

            }
        });
        anim.start();
    }
    public void expandView(int height, final RelativeLayout layout, final int scrollTo) {

        ValueAnimator anim = ValueAnimator.ofInt(layout.getMeasuredHeightAndState(),
                height);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = layout.getLayoutParams();
                layoutParams.height = val;
                layout.setLayoutParams(layoutParams);
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
            }else{
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
                    , title, mEditTextOptions, mCheckBoxOptions);

            presetDBAdapter.close();
        }else{
            presetDBAdapter.createPreset(players, timeLimit, title, mEditTextOptions, mCheckBoxOptions);
        }
        presetDBAdapter.close();
    }

    public void updateEditText(ArrayList players, String timeLimit) {

        this.players = players;
        this.timeLimit = timeLimit;

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

        for (EditTextOption e : mEditTextOptions){
            if (e.getmData() != 0){
                e.getmEditText().setText(String.valueOf(e.getmData()));
            }
        }

        for (CheckBoxOption c : mCheckBoxOptions){
            if (c.getmData() == 1){
                c.getmCheckBox().setChecked(true);
            }else{
                c.getmCheckBox().setChecked(false);
            }
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

        for (EditTextOption e : mEditTextOptions){
            dbHelper.open();
            dbHelper.updateGame(null, null,e.getmData(), e.getmDatabaseColumn(), gameID);
        }

        for (CheckBoxOption c : mCheckBoxOptions){
            dbHelper.open();
            dbHelper.updateGame(null, null,c.getmData(), c.getmDatabaseColumn(), gameID);
        }
    }

    @Override
    public void onItemClicked(int position, int gameID) {
        arrayAdapter.toggleSelection(position, gameID);
    }
}
