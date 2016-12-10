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


    private static final String STATE_GAMEID = "mGameID";
    public static PlayerListAdapter PLAYER_LIST_ADAPTER;
    public static RelativeLayout NEW_GAME_LAYOUT;
    private Snackbar mSnackbar;
    private PresetDBAdapter mPresetDBAdapter;
    private DataHelper mDataHelper;
    private String mTime = null;
    private String TAG = "NewGame";
    private EditText mEditTextPlayer, mEditTextGameTitle;
    private Button mButtonNewGame, mButtonAddPlayer, mButtonQuit, mButtonCreatePreset;
    private RecyclerView mPlayerList;
    private int mGameID;
    private Intent mHomeIntent;
    private RecyclerView.LayoutManager mLayoutManager;
    private GameDBAdapter mDBHelper;
    private boolean mStop = true;
    private Spinner mSpinnerTimeLimit, mSpinnerPreset;
    private String mDefaultTitle;
    private RecyclerViewArrayAdapter mRecyclerViewAdapter;
    private SharedPreferences mSharedPreferences;
    private List<OptionCardView> mCardViewList = new ArrayList<>();
    private NestedScrollView mScrollView;

    private Game mCurrentGame;

    private List<IntEditTextOption> mIntEditTextOptions = new ArrayList<>();
    private List<CheckBoxOption> mCheckBoxOptions = new ArrayList<>();
    private List<TimeLimit> mTimeLimitArray = new ArrayList<>();

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

        Themes.themeActivity(this, R.layout.activity_new_game, true);

        AdView mAdView = (AdView) findViewById(R.id.adViewHome);
        AdCreator adCreator = new AdCreator(mAdView, this);
        adCreator.createAd();

        mDBHelper = new GameDBAdapter(this);
        mDataHelper = new DataHelper();

        mTimeLimitArray = TimeLimit.getTimeLimitArray(this);

        if (mTimeLimitArray == null){
            mTimeLimitArray = new ArrayList<>();
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
            if (card.getmHeader().getId() != R.id.playersHeader) {

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

        NEW_GAME_LAYOUT = (RelativeLayout)findViewById(R.id.newGameLayout);
        mSpinnerTimeLimit = (Spinner)findViewById(R.id.spinnerTimeLimit);
        mSpinnerPreset = (Spinner)findViewById(R.id.spinnerPreset);

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

        mSpinnerTimeLimit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (i == 0){
                    mCurrentGame.setmTimeLimit(null);
                }else if (i == 1){
                    timeLimitDialog();
                }else {
                    mCurrentGame.setmTimeLimit(mTimeLimitArray.get(i - 2));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        displaySpinner(mSpinnerTimeLimit, timeLimitStringArray());
        displaySpinner(mSpinnerPreset, presetStringArray());

        mDBHelper.open().updateGame(mCurrentGame);


    }

    private void timeLimitDialog() {

        LayoutInflater mInflater = LayoutInflater.from(this);
        final AlertDialog mAlertDialog;
        final View dialogView;

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogView = mInflater.inflate(R.layout.create_time_limit, null);
        EditText editTextHour = (EditText) dialogView.findViewById(R.id.editTextHour);
        EditText editTextMinute = (EditText) dialogView.findViewById(R.id.editTextMinute);
        EditText editTextSecond = (EditText) dialogView.findViewById(R.id.editTextSeconds);
        RelativeLayout relativeLayout = (RelativeLayout) dialogView.findViewById(R.id.relativeLayout2);
        relativeLayout.setVisibility(View.VISIBLE);
        final CheckBox checkBoxExtend = (CheckBox) dialogView.findViewById(R.id.checkBoxExtend);
        checkBoxExtend.setVisibility(View.INVISIBLE);

        editTextHour.setText("0");
        editTextMinute.setText("0");
        editTextSecond.setText("0");

        dialogBuilder.setPositiveButton(R.string.create, null);

        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                mSpinnerTimeLimit.setSelection(0);
                mCurrentGame.setmTimeLimit(null);
            }
        });

        dialogBuilder.setView(dialogView);

        mAlertDialog = dialogBuilder.create();

        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        try {

                            String timeLimitString = TimeLimit.updateTimeLimit(dialogView, null);

                            if (timeLimitString != null) {

                                if (!timeLimitString.equals("00:00:00:0")) {
                                    TimeLimit timeLimit = new TimeLimit(mDataHelper.createTimeLimitCondensed(timeLimitString), timeLimitString);

                                    if (mTimeLimitArray != null) {
                                        mTimeLimitArray.add(timeLimit);
                                    } else {
                                        mTimeLimitArray = new ArrayList<>();
                                    }

                                    if (mDataHelper.checkDuplicates(timeLimitStringArray())) {

                                        mTimeLimitArray.remove(mTimeLimitArray.size() - 1);
                                        TimeLimit.saveTimeLimit(mTimeLimitArray, NewGame.this);
                                        displaySpinner(mSpinnerTimeLimit, timeLimitStringArray());
                                        mSpinnerPreset.setSelection(0);
                                        Toast.makeText(NewGame.this, "Already exists", Toast.LENGTH_SHORT).show();

                                    } else {

                                        mCurrentGame.setmTimeLimit(timeLimit);
                                        mDBHelper.open().updateGame(mCurrentGame);
                                        mAlertDialog.dismiss();
                                        TimeLimit.saveTimeLimit(mTimeLimitArray, NewGame.this);
                                        displaySpinner(mSpinnerTimeLimit, timeLimitStringArray());

                                    }

                                } else {

                                    mAlertDialog.dismiss();
                                    mSpinnerTimeLimit.setSelection(0);
                                    mCurrentGame.setmTimeLimit(null);

                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(TAG, e.toString());
                            Toast toast = Toast.makeText(NewGame.this, R.string.invalid_length, Toast.LENGTH_SHORT);
                            toast.show();
                        }

                    }

                });


            }

        });

        mAlertDialog.show();

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

    private List<String> timeLimitStringArray(){
        List<String> arrayList = new ArrayList<>();
        arrayList.add("No Time Limit");
        arrayList.add("Create...");

        if (mTimeLimitArray != null) {
            for (TimeLimit timeLimit : mTimeLimitArray) {
                arrayList.add(timeLimit.getmTitle());
            }
        }

        return arrayList;
    }

    private List<String> presetStringArray(){
        List<String> arrayList = new ArrayList<>();
        arrayList.add("No Preset");

        for (int i = 1; i <= mPresetDBAdapter.open().numRows(); i++){
            mPresetDBAdapter.open();
            arrayList.add(mDataHelper.getPreset(i, mPresetDBAdapter).getmTitle());
            mPresetDBAdapter.close();
        }

        return arrayList;
    }

    private void displaySpinner(final Spinner spinner, List<String> array){

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, array);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

    }

    public void loadGame(int position){
        mDataHelper = new DataHelper();
        mPresetDBAdapter.open();
        mCurrentGame = mDataHelper.getPreset(position, mPresetDBAdapter);
        mPresetDBAdapter.close();

        for (IntEditTextOption e : mCurrentGame.getmIntEditTextOptions()){
            e.setInt(mCurrentGame.getInt(e.getmID()));
        }

        for (CheckBoxOption c : mCurrentGame.getmCheckBoxOptions()){
            c.setChecked(mCurrentGame.isChecked(c.getmID()));
        }

        mEditTextPlayer.setText("");

        updateOptionViews();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_delete).setVisible(false);
        menu.findItem(R.id.action_settings).setVisible(false);
        menu.findItem(R.id.action_delete_presets).setVisible(true);
        menu.findItem(R.id.action_delete_timelimits).setVisible(true);
        menu.findItem(R.id.action_reset).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_delete_presets:
                if (mPresetDBAdapter.open().numRows() != 0){
                    deleteDialog(presetStringArray(), Pointers.DELETE_PRESETS);
                }else{
                    Toast.makeText(this, "No Presets Created", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.action_delete_timelimits:
                if (timeLimitStringArray().size() > 2){
                    deleteDialog(timeLimitStringArray(), Pointers.DELETE_TIMELIMITS);
                }else{
                    Toast.makeText(this, "No Time Limits Created", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.action_reset:
                reset();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void reset(){
        mCurrentGame.getmPlayerArray().clear();
        displayRecyclerView();

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

        mSpinnerPreset.setSelection(0);

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

            mSnackbar = Snackbar.make(NEW_GAME_LAYOUT, R.string.duplicates_message, Snackbar.LENGTH_SHORT)
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

            mSnackbar = Snackbar.make(NEW_GAME_LAYOUT, R.string.must_have_name, Snackbar.LENGTH_SHORT)
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

    public void deleteDialog(List<String> array, final int type){

        final View dialogView;

        mRecyclerViewAdapter = new RecyclerViewArrayAdapter(array, this, this);

        LayoutInflater inflter = LayoutInflater.from(this);
        final AlertDialog alertDialog;
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogView = inflter.inflate(R.layout.recyclerview_fragment, null);
        final RecyclerView recyclerView = (RecyclerView) dialogView.findViewById(R.id.recyclerViewFragment);

        if (type == Pointers.DELETE_PRESETS) {

            dialogBuilder.setTitle(getResources().getString(R.string.delete_presets));
            dialogBuilder.setMessage(getResources().getString(R.string.delete_presets_message));

        }else{

            dialogBuilder.setTitle(getResources().getString(R.string.delete_time_limits));
            dialogBuilder.setMessage(getResources().getString(R.string.delete_time_limits_message));

        }

        dialogBuilder.setNeutralButton(R.string.delete_all, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (type == Pointers.DELETE_PRESETS) {
                    mPresetDBAdapter.open();
                    mPresetDBAdapter.deleteAllPresets();
                    mPresetDBAdapter.close();
                    displaySpinner(mSpinnerPreset, presetStringArray());

                }else{

                    TimeLimit.deleteAllTimeLimits(NewGame.this);
                    mTimeLimitArray = TimeLimit.getTimeLimitArray(NewGame.this);
                    displaySpinner(mSpinnerTimeLimit, timeLimitStringArray());

                }
            }
        });

        dialogBuilder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                mRecyclerViewAdapter.deleteSelectedItems(type, NewGame.this);

                if (type == Pointers.DELETE_PRESETS) {
                    displaySpinner(mSpinnerPreset, presetStringArray());

                }else{

                    mTimeLimitArray = TimeLimit.getTimeLimitArray(NewGame.this);
                    displaySpinner(mSpinnerTimeLimit, timeLimitStringArray());

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

        recyclerView.setAdapter(mRecyclerViewAdapter);

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
                createNewGame(true);
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
                        mCurrentGame.setmTitle(presetName[0]);
                        createPreset();
                        alertDialog.dismiss();
                        displaySpinner(mSpinnerPreset, presetStringArray());
                        mSpinnerPreset.setVisibility(View.VISIBLE);

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

            mSnackbar = Snackbar.make(NEW_GAME_LAYOUT, R.string.more_than_two_players, Snackbar.LENGTH_SHORT)
                    .setAction("Dismiss", onClickListener);
            mSnackbar.show();
        }else{
            if (mDataHelper.checkPlayerDuplicates(mCurrentGame.getmPlayerArray())) {

                mSnackbar = Snackbar.make(NEW_GAME_LAYOUT, R.string.duplicates_message, Snackbar.LENGTH_SHORT)
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
    public void onItemClicked(int position) {
        mRecyclerViewAdapter.toggleSelection(position);
    }
}
