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
    private Integer gameID;
    private Intent homeIntent;
    private RecyclerView.LayoutManager mLayoutManager;
    private ScoreDBAdapter dbHelper;
    private boolean stop = true;
    public static Spinner spinnerTimeLimit;
    public static Spinner spinnerPreset;
    private List timeLimitArray;
    private List timeLimitArrayNum;
    private String defaultTitle;
    public static RelativeLayout relativeLayout;
    private RecyclerViewArrayAdapter arrayAdapter;
    private SharedPreferences sharedPreferences;
    private boolean classicTheme;
    static final String STATE_GAMEID = "gameID";
    private List<OptionCardView> mCardViewList = new ArrayList<>();
    private NestedScrollView scrollView;

    private Game mCurrentGame;
    private List<EditTextOption> mEditTextOptions = new ArrayList<>();
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

        stop = false;
        dbHelper.open().updateGame(mCurrentGame);

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

        mCardViewList.add(new OptionCardView((RelativeLayout) findViewById(R.id.relativeLayoutOptions)
                , (RelativeLayout) findViewById(R.id.optionsHeader), 0));

        mCardViewList.add(new OptionCardView((RelativeLayout) findViewById(R.id.relativeLayoutPresets)
                , (RelativeLayout) findViewById(R.id.presetHeader), 0));

        mCardViewList.add(new OptionCardView((RelativeLayout) findViewById(R.id.relativeLayoutPlayers)
                , (RelativeLayout) findViewById(R.id.playersHeader), 0));

        mCardViewList.add(new OptionCardView((RelativeLayout) findViewById(R.id.relativeLayoutTimeLimit)
                , (RelativeLayout) findViewById(R.id.timeLimitHeader), 0));

        for (final OptionCardView card: mCardViewList){
            if (card.getmHeader().getId() == R.id.playersHeader && getResources().getConfiguration().orientation == getResources().getConfiguration().ORIENTATION_LANDSCAPE) {
            }else{

                card.getmHeader().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toggleCardViewHeight(card.getmHeight(), card, scrollView.getBottom());

                    }
                });
            }

            card.getmContent().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int height = card.getmContent().getMeasuredHeight();

                    card.setmHeight(height);

                    if (card.getmHeader().getId() != R.id.playersHeader) {
                        toggleCardViewHeight(height, card, scrollView.getScrollY());
                    }
                    // Do whatever you want with h
                    // Remove the listener so it is not called repeatedly
                    removeOnGlobalLayoutListener(card.getmContent(), this);
                }
            });
        }

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

        List<EditTextOption> mEditTextOptions = EditTextOption.loadEditTextOptions(this);
        List<CheckBoxOption> mCheckBoxOptions = CheckBoxOption.loadCheckBoxOptions(this);

        if (savedInstanceState != null){
            dbHelper.open();
            gameID = savedInstanceState.getInt(STATE_GAMEID);

            mCurrentGame = dataHelper.getGame(gameID, dbHelper);

            if (timeLimit== null){
                checkBoxNoTimeLimit.setChecked(false);
                spinnerTimeLimit.setVisibility(View.INVISIBLE);
            }else{
                checkBoxNoTimeLimit.setChecked(true);
                spinnerTimeLimit.setVisibility(View.VISIBLE);
                spinnerTimeLimit.setSelection(timeLimitArray.indexOf(timeLimit));
            }

            dbHelper.updateGame(mCurrentGame);
            displayRecyclerView();
            dbHelper.close();

        }else{

            mCurrentGame = new Game(new ArrayList<Player>(), null, "The Game with no name", "00:00:00:0", time, false,0, Game.createOptionArray(mEditTextOptions, mCheckBoxOptions));


            dbHelper.open().createGame(mCurrentGame);
            gameID = dbHelper.getNewestGame();
            mCurrentGame.setmID(gameID);
            dbHelper.close();
            spinnerTimeLimit.setVisibility(View.INVISIBLE);
            displayRecyclerView();

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
                        c.setmChecked(true);
                    }else{
                        c.setmChecked(false);
                    }
                    mCurrentGame.setOptionData(c.getmID(), c.ismChecked());
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
                    mCurrentGame.setOptionData(e.getmID(), e.getmData());

                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }


        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        time = sdfDate.format(now);
        mCurrentGame.setmTimeLimit(time);

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

        dbHelper.open().updateGame(mCurrentGame);


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

            List<String> titleArrayList = new ArrayList();
            titleArrayList.add("No Preset");

            for (int i = 0; i < presetDBAdapter.open().numRows(); i++){
                presetDBAdapter.open();
                titleArrayList.add(dataHelper.getPreset(i, presetDBAdapter).getmTitle());
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
        List<Player> presetPlayers;
        String presetTimeLimit;
        Game presetGame = dataHelper.getPreset(position, presetDBAdapter);
        presetPlayers = presetGame.getmPlayerArray();
        presetTimeLimit = presetGame.getmTimeLimit();

        for (EditTextOption e : mEditTextOptions){
            e.setmData(presetGame.getData(e.getmID()));
        }

        for (CheckBoxOption c : mCheckBoxOptions){
            c.setmChecked(presetGame.isChecked(c.getmID()));
        }

        presetDBAdapter.close();
        editTextPlayer.setText("");

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
        mCurrentGame.getmPlayerArray().clear();
        displayRecyclerView();
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

    public void displayRecyclerView(){
        playerList.setVisibility(View.VISIBLE);
        mLayoutManager = new LinearLayoutManager(this);
        playerList.setLayoutManager(mLayoutManager);
        playerListAdapter = new PlayerListAdapter(mCurrentGame, dbHelper, PlayerListAdapter.NEW_GAME, false);
        playerList.setAdapter(playerListAdapter);

    }

    public void addPlayers() {
        String playerName = editTextPlayer.getText().toString().trim();
        boolean duplicates;
        mCurrentGame.addPlayer(new Player(playerName, 0, new ArrayList<Integer>()));
        duplicates = dataHelper.checkPlayerDuplicates(mCurrentGame.getmPlayerArray());

        if (duplicates) {
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                }
            };

            mCurrentGame.removePlayer(mCurrentGame.size() - 1);

            snackbar = Snackbar.make(relativeLayout, R.string.duplicates_message, Snackbar.LENGTH_SHORT)
                    .setAction("Dismiss", onClickListener);
            snackbar.show();
        }

        if (playerName.equals("") || playerName.equals(" ")) {
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                }
            };

            mCurrentGame.removePlayer(mCurrentGame.size() - 1);

            snackbar = Snackbar.make(relativeLayout, R.string.must_have_name, Snackbar.LENGTH_SHORT)
                    .setAction("Dismiss", onClickListener);
            snackbar.show();
        } else if (!duplicates && !playerName.equals("") && !playerName.equals(" ")) {

            editTextPlayer.setText("");

            dbHelper.open().updateGame(mCurrentGame);

            // specify an adapter (see also next example)
            playerListAdapter.notifyItemInserted(mCurrentGame.size());

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

    public void deletePresetsDialog(){
        final View dialogView;
        final List<String> titleArrayList  = new ArrayList<>();

        for (int i = 1; i <= presetDBAdapter.open().numRows(); i++){
            Game presetGame = dataHelper.getPreset(i, presetDBAdapter.open());
            titleArrayList.add(presetGame.getmTitle());
        }

        arrayAdapter = new RecyclerViewArrayAdapter((ArrayList) titleArrayList, this, this, 1);
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
        mCurrentGame.setmTimeLimit(null);
        dbHelper.open().updateGame(mCurrentGame);
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
                if (checkBoxNoTimeLimit.isChecked()){
                    spinnerTimeLimit.setEnabled(true);
                    spinnerTimeLimit.setVisibility(View.VISIBLE);
                    mCurrentGame.setmTimeLimit(timeLimitArrayNum.get(0).toString());
                    dbHelper.open().updateGame(mCurrentGame);

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
                List<Player> mPlayerArray = mCurrentGame.getmPlayerArray();

                if (mPlayerArray.size() == 2){
                    editTextPresetTitle.setHint(mPlayerArray.get(0) + " vs " + mPlayerArray.get(1));

                }else if (mPlayerArray.size() == 3){
                    editTextPresetTitle.setHint(mPlayerArray.get(0) + " vs " + mPlayerArray.get(1) + " vs " + mPlayerArray.get(2));

                }else if (mPlayerArray.size() > 3 && mPlayerArray.size() < 10){
                    String playerTitle = "";
                    for (int i = 0; i < mPlayerArray.size(); i++){
                        playerTitle += mPlayerArray.get(i);
                        if (i != mPlayerArray.size()-1){
                            playerTitle += ",";
                        }
                    }

                    editTextPresetTitle.setHint(playerTitle);

                }else if (mPlayerArray.size() > 10){
                    String playerTitle = "";
                    for (int i = 0; i < mPlayerArray.size(); i++){
                        playerTitle += mPlayerArray.get(i);
                        if (i != mPlayerArray.size()-1){
                            playerTitle += ",";
                        }
                    }

                    editTextPresetTitle.setHint(playerTitle);

                }else if (mPlayerArray.size() == 1){
                    editTextPresetTitle.setHint(String.valueOf(mPlayerArray.get(0)));

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

    public void createNewGame(boolean startGame) {
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        stop = false;
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        };

        //snackbar must have 2 or more players

        if (mCurrentGame.size() < 2) {

            snackbar = Snackbar.make(relativeLayout, R.string.more_than_two_players, Snackbar.LENGTH_SHORT)
                    .setAction("Dismiss", onClickListener);
            snackbar.show();
        }else{
            if (dataHelper.checkPlayerDuplicates(mCurrentGame.getmPlayerArray())) {

                snackbar = Snackbar.make(relativeLayout, R.string.duplicates_message, Snackbar.LENGTH_SHORT)
                        .setAction("Dismiss", onClickListener);
                snackbar.show();

            }else {

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
        mCurrentGame.setmTitle(title);
        presetDBAdapter = new PresetDBAdapter(this);
        presetDBAdapter.open();
        if (spinnerTimeLimit.getSelectedItemPosition()+1 != timeLimitArrayNum.size()) {
            presetDBAdapter.open();

            presetDBAdapter.createPreset(mCurrentGame);

            presetDBAdapter.close();
        }else{
            presetDBAdapter.createPreset(mCurrentGame);
        }
        presetDBAdapter.close();
    }

    public void updateOptionViews() {

        displayRecyclerView();

        if (mCurrentGame.getmTimeLimit() != null) {
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
            if (c.ismChecked()){
                c.getmCheckBox().setChecked(true);
            }else{
                c.getmCheckBox().setChecked(false);
            }
        }

        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
        Date now = new Date();
        time = sdfDate.format(now);
        mCurrentGame.setmTime(time);
        dbHelper.open().updateGame(mCurrentGame);
    }

    @Override
    public void onItemClicked(int position, int gameID) {
        arrayAdapter.toggleSelection(position, gameID);
    }
}
