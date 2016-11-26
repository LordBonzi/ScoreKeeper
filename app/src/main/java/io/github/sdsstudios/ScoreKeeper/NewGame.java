package io.github.sdsstudios.ScoreKeeper;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

    public static PlayerListAdapter PLAYER_LIST_ADAPTER;
    private Snackbar mSnackbar;
    private TimeLimitAdapter mTimeLimitAdapter;
    private PresetDBAdapter mPresetDBAdapter;
    private DataHelper mDataHelper;
    private String mTime = null;
    private String TAG = "NewGame";
    private EditText mEditTextPlayer, mEditTextGameTitle;
    private Button mButtonNewGame, mButtonAddPlayer, mButtonQuit, mButtonCreatePreset;
    private CheckBox mCheckBoxNoTimeLimit;
    private RecyclerView mPlayerList;
    private int mGameID;
    private Intent mHomeIntent;
    private RecyclerView.LayoutManager mLayoutManager;
    private ScoreDBAdapter mDBHelper;
    private boolean mStop = true;
    public static Spinner SPINNER_TIME_LIMIT;
    public static Spinner SPINNER_PRESET;
    private List timeLimitArray;
    private List timeLimitArrayNum;
    private String mDefaultTitle;
    public static RelativeLayout RELATIVE_LAYOUT;
    private RecyclerViewArrayAdapter mRecyclerViewAdapter;
    private SharedPreferences mSharedPreferences;
    private static final String STATE_GAMEID = "mGameID";
    private List<OptionCardView> mCardViewList = new ArrayList<>();
    private NestedScrollView mScrollView;

    private Game mCurrentGame;
    private List<IntEditTextOption> mIntEditTextOptions = new ArrayList<>();
    private List<CheckBoxOption> mCheckBoxOptions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null){
            loadActivity(savedInstanceState);
        }else{
            loadActivity(null);

        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state

        mStop = false;
        mDBHelper.open().updateGame(mCurrentGame);

        savedInstanceState.putInt(STATE_GAMEID, mGameID);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public void loadActivity(Bundle savedInstanceState){

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int accentColor = mSharedPreferences.getInt("prefAccentColor", R.style.DarkTheme);

        int primaryColor = mSharedPreferences.getInt("prefPrimaryColor", getResources().getColor(R.color.primaryIndigo));
        int primaryDarkColor = mSharedPreferences.getInt("prefPrimaryDarkColor", getResources().getColor(R.color.primaryIndigoDark));
        boolean colorNavBar = mSharedPreferences.getBoolean("prefColorNavBar", false);

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

        mDBHelper = new ScoreDBAdapter(this);
        mDataHelper = new DataHelper();

        String timeLimit = mSharedPreferences.getString("timelimitarray", null);
        final String timeLimitnum = mSharedPreferences.getString("timelimitarraynum", null);

        if (timeLimit != null && timeLimitnum !=null) {

            timeLimitArray = mDataHelper.convertToArray(timeLimit);
            timeLimitArrayNum = mDataHelper.convertToArray(timeLimitnum);
        }else{
            timeLimitArray = new ArrayList();
            timeLimitArray.add(0, "Create...");

            timeLimitArrayNum = new ArrayList();
            timeLimitArrayNum.add(0, "Create...");
            mDataHelper.saveSharedPrefs(timeLimitArray, timeLimitArrayNum, this);

        }

        mScrollView = (NestedScrollView) findViewById(R.id.scrollView);

        mCardViewList.add(new OptionCardView((RelativeLayout) findViewById(R.id.relativeLayoutOptions)
                , (RelativeLayout) findViewById(R.id.optionsHeader), 0));

        mCardViewList.add(new OptionCardView((RelativeLayout) findViewById(R.id.relativeLayoutPresets)
                , (RelativeLayout) findViewById(R.id.presetHeader), 0));

        mCardViewList.add(new OptionCardView((RelativeLayout) findViewById(R.id.relativeLayoutPlayers)
                , (RelativeLayout) findViewById(R.id.playersHeader), 0));

        mCardViewList.add(new OptionCardView((RelativeLayout) findViewById(R.id.relativeLayoutTimeLimit)
                , (RelativeLayout) findViewById(R.id.timeLimitHeader), 0));

        for (final OptionCardView card: mCardViewList){
            if (card.getmHeader().getId() != R.id.playersHeader && getResources().getConfiguration().orientation
                    != getResources().getConfiguration().ORIENTATION_LANDSCAPE) {

                card.getmHeader().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toggleCardViewHeight(card.getmHeight(), card, mScrollView.getBottom());

                    }
                });
            }

            card.getmContent().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int height = card.getmContent().getMeasuredHeight();

                    card.setmHeight(height);

                    if (card.getmHeader().getId() != R.id.playersHeader) {
                        toggleCardViewHeight(height, card, mScrollView.getScrollY());
                    }
                    // Do whatever you want with h
                    // Remove the onSharedPreferenceChangeListener so it is not called repeatedly
                    removeOnGlobalLayoutListener(card.getmContent(), this);
                }
            });
        }

        RELATIVE_LAYOUT = (RelativeLayout)findViewById(R.id.newGameLayout);
        SPINNER_TIME_LIMIT = (Spinner)findViewById(R.id.spinnerTimeLimit);
        SPINNER_PRESET = (Spinner)findViewById(R.id.spinnerPreset);

        mCheckBoxNoTimeLimit = (CheckBox) findViewById(R.id.checkBoxNoTimeLimit);
        mCheckBoxNoTimeLimit.setOnClickListener(this);
        mCheckBoxNoTimeLimit.setChecked(false);

        mButtonCreatePreset = (Button)findViewById(R.id.buttonCreatePreset);
        mButtonCreatePreset.setOnClickListener(this);
        mButtonCreatePreset.setVisibility(View.VISIBLE);

        mPresetDBAdapter = new PresetDBAdapter(this);

        mHomeIntent = new Intent(this, Home.class);
        mPlayerList = (RecyclerView)findViewById(R.id.playerList);

        mEditTextPlayer = (EditText) findViewById(R.id.editTextPlayer);

        mEditTextGameTitle = (EditText) findViewById(R.id.editTextGameTitle);
        mEditTextGameTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mCurrentGame.setmTitle(s.toString().trim());
                mDBHelper.open().updateGame(mCurrentGame);
            }
        });

        mIntEditTextOptions = IntEditTextOption.loadEditTextOptions(this);
        mCheckBoxOptions = CheckBoxOption.loadCheckBoxOptions(this);

        if (savedInstanceState != null){

            mDBHelper.open();
            mGameID = savedInstanceState.getInt(STATE_GAMEID);

            mCurrentGame = mDataHelper.getGame(mGameID, mDBHelper);

            if (timeLimit== null){
                mCheckBoxNoTimeLimit.setChecked(false);
                SPINNER_TIME_LIMIT.setVisibility(View.INVISIBLE);
            }else{
                mCheckBoxNoTimeLimit.setChecked(true);
                SPINNER_TIME_LIMIT.setVisibility(View.VISIBLE);
                SPINNER_TIME_LIMIT.setSelection(timeLimitArray.indexOf(timeLimit));
            }

            mDBHelper.updateGame(mCurrentGame);
            displayRecyclerView();
            mDBHelper.close();

        }else{

            mCurrentGame = new Game(new ArrayList<Player>(), null, false, 0
                    , IntEditTextOption.loadEditTextOptions(this), CheckBoxOption.loadCheckBoxOptions(this), StringEditTextOption.loadEditTextOptions(), null);

            mDBHelper.open().createGame(mCurrentGame);

            mCurrentGame.setmLength("00:00:00:0");
            mCurrentGame.setmTime(mTime);
            mCurrentGame.setmTitle(mEditTextGameTitle.getText().toString().trim());

            mGameID = mDBHelper.getNewestGame();
            mCurrentGame.setmID(mGameID);

            mDBHelper.close();
            SPINNER_TIME_LIMIT.setVisibility(View.INVISIBLE);
            displayRecyclerView();

        }

        mButtonNewGame = (Button)findViewById(R.id.buttonNewGame);
        mButtonNewGame.setOnClickListener(this);

        mButtonQuit = (Button)findViewById(R.id.buttonQuit);
        mButtonQuit.setOnClickListener(this);

        mButtonAddPlayer = (Button) findViewById(R.id.buttonAddPlayer);
        mButtonAddPlayer.setOnClickListener(this);

        for (final CheckBoxOption c : mCheckBoxOptions){
            getCheckBox(c).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    c.setChecked(!c.isChecked());

                    mCurrentGame.setmCheckBoxOption(c);

                    mDBHelper.open().updateGame(mCurrentGame);

                }
            });

            getCheckBox(c).setChecked(false);
        }

        for (final IntEditTextOption e: mIntEditTextOptions){
            getEditText(e).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    try
                    {
                        if (charSequence.toString().equals("")){
                            if (e.getmID() == IntEditTextOption.SCORE_INTERVAL) {
                                e.setInt(1);
                            }else{
                                e.setInt(0);
                            }
                        }else {
                            e.setInt(Integer.parseInt(charSequence.toString()));
                        }
                    }
                    catch (NumberFormatException error)
                    {
                        error.printStackTrace();
                        e.setInt(0);

                    }

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    mCurrentGame.setmIntEditTextOption(e);

                    mDBHelper.open().updateGame(mCurrentGame);

                }
            });
        }

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        mTime = sdfDate.format(now);
        mCurrentGame.setmTime(mTime);

        mEditTextPlayer.setOnEditorActionListener(new TextView.OnEditorActionListener() {

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

        mDBHelper.open().updateGame(mCurrentGame);


    }

    private CheckBox getCheckBox(CheckBoxOption checkBoxOption){
        try{

            return ((CheckBox) findViewById(checkBoxOption.getmCheckBoxID()));

        }catch (ClassCastException e){

            switch (checkBoxOption.getmID()){
                case CheckBoxOption.REVERSE_SCORING:
                    checkBoxOption.setmCheckBoxID(R.id.checkBoxReverseScoring);
                    return ((CheckBox) findViewById(R.id.checkBoxReverseScoring));

                case CheckBoxOption.STOPWATCH:
                    checkBoxOption.setmCheckBoxID(R.id.checkBoxStopwatch);
                    return ((CheckBox) findViewById(R.id.checkBoxStopwatch));

                default:
                    return null;

            }

        }
    }

    private EditText getEditText(EditTextOption editTextOption){
        try{

            return ((EditText) findViewById(editTextOption.getmEditTextID()));

        }catch (ClassCastException e){

            switch (editTextOption.getmID()){
                case EditTextOption.NUMBER_SETS:
                    editTextOption.setmEditTextID(R.id.editTextNumSets);
                    return ((EditText) findViewById(R.id.editTextNumSets));

                case EditTextOption.SCORE_DIFF_TO_WIN:
                    editTextOption.setmEditTextID(R.id.editTextDiffToWin);
                    return ((EditText) findViewById(R.id.editTextDiffToWin));

                case EditTextOption.WINNING_SCORE:
                    editTextOption.setmEditTextID(R.id.editTextMaxScore);
                    return ((EditText) findViewById(R.id.editTextMaxScore));

                case EditTextOption.STARTING_SCORE:
                    editTextOption.setmEditTextID(R.id.editTextStartingScore);
                    return ((EditText) findViewById(R.id.editTextStartingScore));

                case EditTextOption.SCORE_INTERVAL:
                    editTextOption.setmEditTextID(R.id.editTextScoreInterval);
                    return ((EditText) findViewById(R.id.editTextScoreInterval));

                default:
                    return null;

            }

        }

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

            mTimeLimitAdapter = new TimeLimitAdapter(this, timeLimitArray, timeLimitArrayNum, mDBHelper, mGameID);
            SPINNER_TIME_LIMIT.setAdapter(mTimeLimitAdapter);

        }else {

            List<String> titleArrayList = new ArrayList();
            titleArrayList.add("No Preset");

            for (int i = 1; i <= mPresetDBAdapter.open().numRows(); i++){
                mPresetDBAdapter.open();
                titleArrayList.add(mDataHelper.getPreset(i, mPresetDBAdapter).getmTitle());
                mPresetDBAdapter.close();
            }

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, titleArrayList);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            SPINNER_PRESET.setAdapter(dataAdapter);

            SPINNER_PRESET.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    if (SPINNER_PRESET.getSelectedItemPosition() != 0){
                        loadGame(SPINNER_PRESET.getSelectedItemPosition());
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }

    }

    public void loadGame(int position){
        mDataHelper = new DataHelper();
        mPresetDBAdapter.open();
        List<Player> presetPlayers;
        String presetTimeLimit;
        Game presetGame = mDataHelper.getPreset(position, mPresetDBAdapter);
        presetPlayers = presetGame.getmPlayerArray();
        presetTimeLimit = presetGame.getmTimeLimit();

        for (IntEditTextOption e : mCurrentGame.getmIntEditTextOptions()){
            e.setInt(presetGame.getInt(e.getmID()));
        }

        for (CheckBoxOption c : mCurrentGame.getmCheckBoxOptions()){
            c.setChecked(presetGame.isChecked(c.getmID()));
        }

        mPresetDBAdapter.close();
        mEditTextPlayer.setText("");

        updateOptionViews();

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
            if (mPresetDBAdapter.open().numRows() != 0){
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
        mCurrentGame.getmPlayerArray().clear();
        displayRecyclerView();
        mCheckBoxNoTimeLimit.setChecked(false);
        disableTimeLimitSpinner();

        mCheckBoxOptions = CheckBoxOption.loadCheckBoxOptions(this);
        mIntEditTextOptions = IntEditTextOption.loadEditTextOptions(this);

        for (IntEditTextOption e: mIntEditTextOptions){
            getEditText(e).setText("");
            getEditText(e).setHint(e.getmHint());
        }

        for (CheckBoxOption c: mCheckBoxOptions){
            getCheckBox(c).setChecked(c.isChecked());
        }

        mCurrentGame.setmCheckBoxOptions(mCheckBoxOptions);
        mCurrentGame.setmIntEditTextOption(mIntEditTextOptions);

        SPINNER_PRESET.setSelection(0);

    }

    public void displayRecyclerView(){
        mPlayerList.setVisibility(View.VISIBLE);
        mLayoutManager = new LinearLayoutManager(this);
        mPlayerList.setLayoutManager(mLayoutManager);
        PLAYER_LIST_ADAPTER = new PlayerListAdapter(mCurrentGame, mDBHelper, Pointers.NEW_GAME, false);
        mPlayerList.setAdapter(PLAYER_LIST_ADAPTER);

    }

    public void addPlayers() {
        String playerName = mEditTextPlayer.getText().toString().trim();

        boolean areDuplicatePlayers;

        mCurrentGame.addPlayer(new Player(playerName, 0));
        areDuplicatePlayers = mDataHelper.checkPlayerDuplicates(mCurrentGame.getmPlayerArray());

        if (areDuplicatePlayers) {
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSnackbar.dismiss();
                }
            };

            mCurrentGame.removePlayer(mCurrentGame.size() - 1);

            mSnackbar = Snackbar.make(RELATIVE_LAYOUT, R.string.duplicates_message, Snackbar.LENGTH_SHORT)
                    .setAction("Dismiss", onClickListener);
            mSnackbar.show();
        }

        if (playerName.equals("") || playerName.equals(" ")) {
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSnackbar.dismiss();
                }
            };

            mCurrentGame.removePlayer(mCurrentGame.size() - 1);

            mSnackbar = Snackbar.make(RELATIVE_LAYOUT, R.string.must_have_name, Snackbar.LENGTH_SHORT)
                    .setAction("Dismiss", onClickListener);
            mSnackbar.show();
        } else if (!areDuplicatePlayers && !playerName.equals("") && !playerName.equals(" ")) {

            mEditTextPlayer.setText("");

            mDBHelper.open().updateGame(mCurrentGame);

            // specify an adapter (see also next example)
            PLAYER_LIST_ADAPTER.notifyItemInserted(mCurrentGame.size());

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mStop){
            mDBHelper.open();
            mDBHelper.deleteGame(mGameID);
            mDBHelper.close();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDBHelper.close();

    }

    @Override
    public void onBackPressed() {
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.quit_setup_question);

        builder.setMessage(R.string.quit_setup_message);

        builder.setPositiveButton(R.string.quit_setup, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                mStop = true;
                mDBHelper.open();
                mDBHelper.deleteGame(mGameID);
                mDBHelper.close();
                startActivity(mHomeIntent);
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

    public void deletePresetsDialog(){
        final View dialogView;
        final List<String> titleArrayList  = new ArrayList<>();

        for (int i = 1; i <= mPresetDBAdapter.open().numRows(); i++){
            Game presetGame = mDataHelper.getPreset(i, mPresetDBAdapter.open());
            titleArrayList.add(presetGame.getmTitle());
        }

        mRecyclerViewAdapter = new RecyclerViewArrayAdapter((ArrayList) titleArrayList, this, this, Pointers.NEW_GAME);
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
                mPresetDBAdapter.open();
                mPresetDBAdapter.deleteAllPresets();
                mPresetDBAdapter.close();
                displaySpinner(false);
            }
        });

        dialogBuilder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mPresetDBAdapter.open();
                mRecyclerViewAdapter.deleteSelectedPresets(mPresetDBAdapter, 0);
                mPresetDBAdapter.close();
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

        recyclerView.setAdapter(mRecyclerViewAdapter);

        alertDialog.show();
    }

    public void disableTimeLimitSpinner(){
        SPINNER_TIME_LIMIT.setEnabled(false);
        SPINNER_TIME_LIMIT.setVisibility(View.INVISIBLE);
        mCurrentGame.setmTimeLimit(null);
        mDBHelper.open().updateGame(mCurrentGame);
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
                createNewGame(true);
                break;

            }

            case R.id.checkBoxNoTimeLimit: {

                if (mCheckBoxNoTimeLimit.isChecked()){
                    SPINNER_TIME_LIMIT.setEnabled(true);
                    SPINNER_TIME_LIMIT.setVisibility(View.VISIBLE);
                    mCurrentGame.setmTimeLimit(timeLimitArrayNum.get(0).toString());
                    mDBHelper.open().updateGame(mCurrentGame);

                }else{
                    disableTimeLimitSpinner();
                }

                break;
            }


            case R.id.buttonCreatePreset:{
                createNewGame(false);

                break;
            }


        }
    }

    private void toggleCardViewHeight(int height, OptionCardView cardView, int scrollTo) {
        if (cardView.getmHeader().getId() != R.id.playersHeader) {

            if (cardView.getmContent().getHeight() != height) {
                // expand

                expandView(height, cardView.getmContent(), scrollTo); //'height' is the height of screen which we have measured already.

            } else {
                cardView.setmHeight(cardView.getmContent().getMeasuredHeight());
                // collapse
                collapseView(cardView);


            }
        }
    }

    public void collapseView(final OptionCardView cardView) {

        ValueAnimator anim = ValueAnimator.ofInt(cardView.getmHeight(), 0);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = cardView.getmContent().getLayoutParams();
                layoutParams.height = val;
                cardView.getmContent().setLayoutParams(layoutParams);

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


            }
        });
        anim.start();

    }

    public void createPresetDialog() {
        final String[] presetName = {mCurrentGame.getmTitle()};

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

                editTextPresetTitle.setHint(presetName[0]);

                mDefaultTitle = editTextPresetTitle.getHint().toString();

                editTextPresetTitle.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        presetName[0] = charSequence.toString();
                        if (charSequence == ""){
                            presetName[0]= mDefaultTitle;
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
                        createPreset();
                        alertDialog.dismiss();
                        displaySpinner(false);
                        SPINNER_PRESET.setVisibility(View.VISIBLE);

                    }
                });

            }
        });
        alertDialog.show();
        Button b1 = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextPresetTitle.setText(mDefaultTitle);
            }
        });

    }

    public void createNewGame(boolean startGame) {
        Intent mainActivityIntent = new Intent(this, GameActivity.class);
        mStop = false;
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSnackbar.dismiss();
            }
        };

        //snackbar must have 2 or more players

        if (mCurrentGame.size() < 2) {

            mSnackbar = Snackbar.make(RELATIVE_LAYOUT, R.string.more_than_two_players, Snackbar.LENGTH_SHORT)
                    .setAction("Dismiss", onClickListener);
            mSnackbar.show();
        }else{
            if (mDataHelper.checkPlayerDuplicates(mCurrentGame.getmPlayerArray())) {

                mSnackbar = Snackbar.make(RELATIVE_LAYOUT, R.string.duplicates_message, Snackbar.LENGTH_SHORT)
                        .setAction("Dismiss", onClickListener);
                mSnackbar.show();

            }else {

                if (startGame) {

                    int startingScore = mCurrentGame.getInt(IntEditTextOption.STARTING_SCORE);

                    for (Player p : mCurrentGame.getmPlayerArray()){
                        p.setmScore(startingScore);
                    }

                    mDBHelper.updateGame(mCurrentGame);

                    mainActivityIntent.putExtra("GAME_ID", mGameID);
                    startActivity(mainActivityIntent);
                    finish();

                }else{
                    createPresetDialog();
                }
            }

        }

    }

    public void createPreset(){

        mPresetDBAdapter = new PresetDBAdapter(this);

        mPresetDBAdapter.open();
        mPresetDBAdapter.createPreset(mCurrentGame);
        mPresetDBAdapter.close();
    }

    public void updateOptionViews() {

        displayRecyclerView();

        if (mCurrentGame.getmTimeLimit() != null) {
            mCheckBoxNoTimeLimit.setChecked(true);
            SPINNER_TIME_LIMIT.setEnabled(true);
            SPINNER_TIME_LIMIT.setVisibility(View.VISIBLE);

        }else{
            mCheckBoxNoTimeLimit.setChecked(false);
            SPINNER_TIME_LIMIT.setVisibility(View.INVISIBLE);
            SPINNER_TIME_LIMIT.setEnabled(false);
        }

        for (IntEditTextOption e : mIntEditTextOptions){

            if (e.getInt() != 0) {
                getEditText(e).setText(String.valueOf(e.getInt()));
            }

        }

        for(CheckBoxOption c : mCheckBoxOptions){
            if (c.isChecked()){
                getCheckBox(c).setChecked(true);
            }else{
                getCheckBox(c).setChecked(false);
            }
        }

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        mTime = sdfDate.format(now);
        mCurrentGame.setmTime(mTime);
        mDBHelper.open().updateGame(mCurrentGame);
    }

    @Override
    public void onItemClicked(int position, int gameID) {
        mRecyclerViewAdapter.toggleSelection(position, gameID);
    }
}
