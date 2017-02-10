package io.github.sdsstudios.ScoreKeeper;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.github.sdsstudios.ScoreKeeper.Activity.Activity;
import io.github.sdsstudios.ScoreKeeper.Activity.OptionActivity;
import io.github.sdsstudios.ScoreKeeper.Adapters.PresetDBAdapter;
import io.github.sdsstudios.ScoreKeeper.Adapters.RecyclerViewArrayAdapter;
import io.github.sdsstudios.ScoreKeeper.Options.CheckBoxOption;
import io.github.sdsstudios.ScoreKeeper.Options.IntEditTextOption;
import io.github.sdsstudios.ScoreKeeper.Options.Option;
import io.github.sdsstudios.ScoreKeeper.Options.StringEditTextOption;

import static io.github.sdsstudios.ScoreKeeper.Activity.Activity.NEW_GAME;

public class NewGame extends OptionActivity
        implements View.OnClickListener, RecyclerViewArrayAdapter.ClickListener {

    private PresetDBAdapter mPresetDBAdapter;
    private EditText mEditTextPlayer, mEditTextGameTitle;
    private Button mButtonNewGame, mButtonAddPlayer, mButtonQuit, mButtonCreatePreset;
    private boolean mStop = true;
    private Spinner mSpinnerTimeLimit, mSpinnerPreset;
    private String mDefaultTitle;

    private RecyclerViewArrayAdapter mRecyclerViewAdapter;

    private List<TimeLimit> mTimeLimitArray = new ArrayList<>();

    @Override
    public Activity getActivity() {
        return NEW_GAME;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        mStop = false;
        saveGameToDatabase();

        savedInstanceState.putInt(STATE_GAMEID, gameID);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTimeLimitArray = TimeLimit.getTimeLimitArray(this);

        if (mTimeLimitArray == null) {
            mTimeLimitArray = new ArrayList<>();
        }

        mSpinnerTimeLimit = (Spinner) findViewById(R.id.spinnerTimeLimit);
        mSpinnerPreset = (Spinner) findViewById(R.id.spinnerPreset);

        mButtonCreatePreset = (Button) findViewById(R.id.buttonCreatePreset);
        mButtonCreatePreset.setOnClickListener(this);
        mButtonCreatePreset.setVisibility(View.VISIBLE);

        mPresetDBAdapter = new PresetDBAdapter(this);

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
                game.setmTitle(s.toString().trim());
                saveGameToDatabase();
            }
        });

        if (savedInstanceState != null) {

            gameDBAdapter.open();
            gameID = savedInstanceState.getInt(STATE_GAMEID);

            game = dataHelper.getGame(gameID, gameDBAdapter);

            saveGameToDatabase();

            gameDBAdapter.close();

        } else {

            game = new Game(new ArrayList<Player>(), null, false, 0
                    , IntEditTextOption.loadEditTextOptions(this)
                    , CheckBoxOption.loadCheckBoxOptions(this)
                    , StringEditTextOption.loadEditTextOptions(this), null);

            gameDBAdapter.open().createGame(game);

            game.setmLength("00:00:00:0");
            game.setmTitle(mEditTextGameTitle.getText().toString().trim());

            gameID = gameDBAdapter.getNewestGame();
            game.setmID(gameID);

            saveGameToDatabase();
            gameDBAdapter.close();
        }

        mButtonNewGame = (Button) findViewById(R.id.buttonNewGame);
        mButtonNewGame.setOnClickListener(this);

        mButtonQuit = (Button) findViewById(R.id.buttonQuit);
        mButtonQuit.setOnClickListener(this);

        mButtonAddPlayer = (Button) findViewById(R.id.buttonAddPlayer);
        mButtonAddPlayer.setOnClickListener(this);

        setGameTime();

        mEditTextPlayer.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    addPlayer();
                    return true;
                }
                return false;
            }
        });

        mSpinnerTimeLimit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (i == 0) {
                    game.setmTimeLimit(null);
                } else if (i == 1) {
                    timeLimitDialog();
                } else {
                    game.setmTimeLimit(mTimeLimitArray.get(i - 2));
                    checkStopwatchCheckBox();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mSpinnerPreset.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i != 0) {
                    loadPreset(i);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        displaySpinner(mSpinnerTimeLimit, timeLimitStringArray());
        displaySpinner(mSpinnerPreset, presetStringArray());

        displayRecyclerView(false);
        setOptionChangeListeners();

        saveGameToDatabase();

    }

    private void checkStopwatchCheckBox() {
        getCheckBox(game.getCheckBoxOption(Option.STOPWATCH)).setChecked(true);
    }

    private void timeLimitDialog() {

        final View dialogView;

        dialogView = layoutInflater.inflate(R.layout.create_time_limit, null);

        EditText editTextHour = (EditText) dialogView.findViewById(R.id.editTextHour);
        EditText editTextMinute = (EditText) dialogView.findViewById(R.id.editTextMinute);
        EditText editTextSecond = (EditText) dialogView.findViewById(R.id.editTextSeconds);

        RelativeLayout relativeLayout = (RelativeLayout) dialogView.findViewById(R.id.relativeLayout2);
        relativeLayout.setVisibility(View.VISIBLE);

        editTextHour.setText("0");
        editTextMinute.setText("0");
        editTextSecond.setText("0");

        DialogInterface.OnClickListener positiveClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {

                    String timeLimitString = TimeLimit.updateTimeLimit(dialogView, null);

                    if (timeLimitString != null) {

                        if (!timeLimitString.equals("00:00:00:0")) {
                            TimeLimit timeLimit = new TimeLimit(dataHelper.createTimeLimitCondensed(timeLimitString), timeLimitString);

                            if (mTimeLimitArray != null) {
                                mTimeLimitArray.add(timeLimit);
                            } else {
                                mTimeLimitArray = new ArrayList<>();
                            }

                            if (dataHelper.checkDuplicates(timeLimitStringArray())) {

                                mTimeLimitArray.remove(mTimeLimitArray.size() - 1);
                                TimeLimit.saveTimeLimit(mTimeLimitArray, NewGame.this);
                                displaySpinner(mSpinnerTimeLimit, timeLimitStringArray());
                                mSpinnerPreset.setSelection(0);
                                Toast.makeText(NewGame.this, "Time limit already exists", Toast.LENGTH_SHORT).show();

                            } else {

                                game.setmTimeLimit(timeLimit);
                                saveGameToDatabase();
                                dialog.dismiss();
                                TimeLimit.saveTimeLimit(mTimeLimitArray, NewGame.this);
                                displaySpinner(mSpinnerTimeLimit, timeLimitStringArray());
                                mSpinnerTimeLimit.setSelection(mTimeLimitArray.size() + 1);
                                checkStopwatchCheckBox();
                            }

                        } else {

                            dialog.dismiss();
                            mSpinnerTimeLimit.setSelection(0);
                            game.setmTimeLimit(null);

                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.toString());
                    Toast toast = Toast.makeText(NewGame.this, R.string.invalid_length, Toast.LENGTH_SHORT);
                    toast.show();
                }

            }
        };

        showCustomAlertDialog(getString(R.string.create_time_limit), null, getString(R.string.create), positiveClickListener,
                getString(R.string.cancel), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        mSpinnerTimeLimit.setSelection(0);
                        game.setmTimeLimit(null);
                    }
                }, dialogView);

    }

    private List<String> timeLimitStringArray() {
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

    private List<String> presetStringArray() {
        List<String> arrayList = new ArrayList<>();
        arrayList.add("No Preset");

        for (int i = 1; i <= mPresetDBAdapter.open().numRows(); i++) {
            mPresetDBAdapter.open();
            arrayList.add(dataHelper.getPreset(i, mPresetDBAdapter).getmTitle());
            mPresetDBAdapter.close();
        }

        return arrayList;
    }

    private void displaySpinner(final Spinner spinner, List<String> array) {

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, array);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_delete_presets:
                if (mPresetDBAdapter.open().numRows() != 0) {
                    deleteDialog(presetStringArray(), Dialog.PRESETS);
                } else {
                    Toast.makeText(this, "No Presets Created", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.action_delete_timelimits:
                if (timeLimitStringArray().size() > 2) {
                    deleteDialog(timeLimitStringArray(), Dialog.TIME_LIMIT);
                } else {
                    Toast.makeText(this, "No Time Limits Created", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.action_reset:
                reset();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void reset() {
        game.setmTitle("The Game With No Name");
        mEditTextGameTitle.setText(game.getmTitle());
        getPlayerArray().clear();
        displayRecyclerView(false);

        game.setmCheckBoxOptions(CheckBoxOption.loadCheckBoxOptions(this));
        game.setmIntEditTextOption(IntEditTextOption.loadEditTextOptions(this));
        game.setmStringEditTextOptions(StringEditTextOption.loadEditTextOptions(this));

        for (IntEditTextOption e : IntEditTextOptions()) {
            getEditText(e).setText("");
            getEditText(e).setHint(e.getmHint());
        }

        for (CheckBoxOption c : CheckBoxOptions()) {
            getCheckBox(c).setChecked(c.isChecked());
        }

        mSpinnerPreset.setSelection(0);

    }

    private void addPlayer() {

        Player newPlayer = new Player(mEditTextPlayer.getText().toString().trim(), 0);

        game.addNewPlayer(newPlayer);

        if (validPlayer(newPlayer)) {
            mEditTextPlayer.setText("");
            saveGameToDatabase();
            playerListAdapter.notifyItemInserted(game.size());
        } else {
            game.removePlayer(game.size() - 1);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mStop) {
            deleteGame();
        }
    }

    @Override
    public void onBackPressed() {
        showAlertDialog(getString(R.string.quit_setup_question), getString(R.string.quit_setup_message), getString(R.string.quit_setup),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        mStop = true;
                        deleteGame();
                        startActivity(homeIntent);
                    }
                }, getString(R.string.cancel), dismissDialogListener);
    }

    private void deleteDialog(List<String> array, final Dialog type) {

        mRecyclerViewAdapter = new RecyclerViewArrayAdapter(array, this, this, type);

        final View dialogView = layoutInflater.inflate(R.layout.recyclerview_fragment, null);
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final RecyclerView recyclerView = (RecyclerView) dialogView.findViewById(R.id.recyclerView);

        if (type == Dialog.PRESETS) {

            dialogBuilder.setTitle(R.string.delete_presets_message);

        } else {
            dialogBuilder.setTitle(R.string.delete_time_limits_message);

        }

        dialogBuilder.setNeutralButton(R.string.delete_all, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (type == Dialog.PRESETS) {
                    mPresetDBAdapter.open();
                    mPresetDBAdapter.deleteAllPresets();
                    mPresetDBAdapter.close();
                    displaySpinner(mSpinnerPreset, presetStringArray());

                } else {

                    TimeLimit.deleteAllTimeLimits(NewGame.this);
                    mTimeLimitArray = TimeLimit.getTimeLimitArray(NewGame.this);
                    displaySpinner(mSpinnerTimeLimit, timeLimitStringArray());

                }
            }
        });

        dialogBuilder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                mRecyclerViewAdapter.deleteSelectedItems();

                if (type == Dialog.PRESETS) {
                    displaySpinner(mSpinnerPreset, presetStringArray());

                } else {

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

        layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(mRecyclerViewAdapter);

        dialogBuilder.setView(dialogView);
        dialogBuilder.show();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonAddPlayer: {
                addPlayer();
                break;
            }

            case R.id.buttonQuit: {
                onBackPressed();
                break;
            }

            case R.id.buttonNewGame: {
                createNewGame(true);
                break;

            }

            case R.id.buttonCreatePreset: {
                createNewGame(false);
                break;

            }

        }
    }

    private void createPresetDialog() {
        final String[] presetName = {game.getmTitle()};

        final View dialogView = layoutInflater.inflate(R.layout.create_preset_fragment, null);
        final EditText editTextPresetTitle = (EditText) dialogView.findViewById(R.id.editTextPresetTitle);

        editTextPresetTitle.setHint(presetName[0]);

        mDefaultTitle = editTextPresetTitle.getHint().toString();

        editTextPresetTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                presetName[0] = charSequence.toString();
                if (charSequence == "") {
                    presetName[0] = mDefaultTitle;
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        showCustomAlertDialog(getString(R.string.create_preset), getString(R.string.create_preset_message), getString(R.string.create),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        game.setmTitle(presetName[0]);
                        createPreset();
                        dialog.dismiss();
                        displaySpinner(mSpinnerPreset, presetStringArray());
                    }
                }, getString(R.string.default_title), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editTextPresetTitle.setText(mDefaultTitle);
                    }
                }, getString(R.string.cancel), dismissDialogListener, dialogView);

    }

    private void createNewGame(boolean startGame) {
        Intent mainActivityIntent = new Intent(this, GameActivity.class);
        mStop = false;

        if (game.size() < 2) {

            createSnackbar(relativeLayout, getString(R.string.more_than_two_players));

        } else {

            if (dataHelper.checkPlayerDuplicates(getPlayerArray())) {

                createSnackbar(relativeLayout, getString(R.string.duplicates_message));

            } else {

                if (startGame) {

                    int startingScore = game.getInt(Option.STARTING_SCORE);

                    for (Player p : getPlayerArray()) {
                        p.setmScore(startingScore);
                    }

                    saveGameToDatabase();

                    mainActivityIntent.putExtra("GAME_ID", gameID);
                    startActivity(mainActivityIntent);
                    finish();

                } else {
                    createPresetDialog();
                }
            }
        }

    }

    private void createPreset() {

        mPresetDBAdapter = new PresetDBAdapter(this);
        mPresetDBAdapter.open();
        mPresetDBAdapter.createPreset(game);
        mPresetDBAdapter.close();

    }

    private void loadPreset(int position) {

        mPresetDBAdapter.open();
        game = dataHelper.getPreset(position, mPresetDBAdapter);
        game.setmID(gameID);
        mPresetDBAdapter.close();

        mEditTextPlayer.setText("");
        mEditTextGameTitle.setText(game.getmTitle());

        displayRecyclerView(false);

        loadOptions();

        setGameTime();

        saveGameToDatabase();
    }

    @Override
    public void onItemClicked(int position) {
        mRecyclerViewAdapter.toggleSelection(position);
    }

}
