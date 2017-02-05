package io.github.sdsstudios.ScoreKeeper;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
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
        implements View.OnClickListener, RecyclerViewArrayAdapter.ViewHolder.ClickListener {

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
        // Save the user's current game state

        mStop = false;
        updateGameInDatabase();

        savedInstanceState.putInt(STATE_GAMEID, mGameID);

        // Always call the superclass so it can save the view hierarchy state
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
                mGame.setmTitle(s.toString().trim());
                updateGameInDatabase();
            }
        });

        if (savedInstanceState != null) {

            mDbHelper.open();
            mGameID = savedInstanceState.getInt(STATE_GAMEID);

            mGame = mDataHelper.getGame(mGameID, mDbHelper);

            updateGameInDatabase();

            mDbHelper.close();

        } else {

            mGame = new Game(new ArrayList<Player>(), null, false, 0
                    , IntEditTextOption.loadEditTextOptions(this)
                    , CheckBoxOption.loadCheckBoxOptions(this)
                    , StringEditTextOption.loadEditTextOptions(this), null);

            mDbHelper.open().createGame(mGame);

            mGame.setmLength("00:00:00:0");
            mGame.setmTitle(mEditTextGameTitle.getText().toString().trim());

            mGameID = mDbHelper.getNewestGame();
            mGame.setmID(mGameID);

            updateGameInDatabase();
            mDbHelper.close();
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
                    addPlayers();
                    return true;
                }
                return false;
            }
        });

        mSpinnerTimeLimit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if (i == 0) {
                    mGame.setmTimeLimit(null);
                } else if (i == 1) {
                    timeLimitDialog();
                } else {
                    mGame.setmTimeLimit(mTimeLimitArray.get(i - 2));
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

        updateGameInDatabase();

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

        editTextHour.setText("0");
        editTextMinute.setText("0");
        editTextSecond.setText("0");

        dialogBuilder.setTitle(getString(R.string.create_time_limit));

        dialogBuilder.setPositiveButton(R.string.create, null);

        dialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                mSpinnerTimeLimit.setSelection(0);
                mGame.setmTimeLimit(null);
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

                                        mGame.setmTimeLimit(timeLimit);
                                        updateGameInDatabase();
                                        mAlertDialog.dismiss();
                                        TimeLimit.saveTimeLimit(mTimeLimitArray, NewGame.this);
                                        displaySpinner(mSpinnerTimeLimit, timeLimitStringArray());

                                    }

                                } else {

                                    mAlertDialog.dismiss();
                                    mSpinnerTimeLimit.setSelection(0);
                                    mGame.setmTimeLimit(null);

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
            arrayList.add(mDataHelper.getPreset(i, mPresetDBAdapter).getmTitle());
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

    public void reset() {
        mGame.setmTitle("The Game With No Name");
        mEditTextGameTitle.setText(mGame.getmTitle());
        getPlayerArray().clear();
        displayRecyclerView(false);

        mGame.setmCheckBoxOptions(CheckBoxOption.loadCheckBoxOptions(this));
        mGame.setmIntEditTextOption(IntEditTextOption.loadEditTextOptions(this));
        mGame.setmStringEditTextOptions(StringEditTextOption.loadEditTextOptions(this));

        for (IntEditTextOption e : IntEditTextOptions()) {
            getEditText(e).setText("");
            getEditText(e).setHint(e.getmHint());
        }

        for (CheckBoxOption c : CheckBoxOptions()) {
            getCheckBox(c).setChecked(c.isChecked());
        }

        mSpinnerPreset.setSelection(0);

    }

    public void addPlayers() {
        String playerName = mEditTextPlayer.getText().toString().trim();

        boolean areDuplicatePlayers;

        mGame.addNewPlayer(new Player(playerName, 0));
        areDuplicatePlayers = mDataHelper.checkPlayerDuplicates(getPlayerArray());

        if (areDuplicatePlayers) {
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSnackBar.dismiss();
                }
            };

            mGame.removePlayer(mGame.size() - 1);

            mSnackBar = Snackbar.make(mRelativeLayout, R.string.duplicates_message, Snackbar.LENGTH_SHORT)
                    .setAction("Dismiss", onClickListener);
            mSnackBar.show();
        }

        if (playerName.equals("") || playerName.equals(" ")) {
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSnackBar.dismiss();
                }
            };

            mGame.removePlayer(mGame.size() - 1);

            mSnackBar = Snackbar.make(mRelativeLayout, R.string.must_have_name, Snackbar.LENGTH_SHORT)
                    .setAction("Dismiss", onClickListener);
            mSnackBar.show();

        } else if (!areDuplicatePlayers && !playerName.equals("") && !playerName.equals(" ")) {

            mEditTextPlayer.setText("");

            updateGameInDatabase();

            mPlayerListAdapter.notifyItemInserted(mGame.size());

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
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.quit_setup_question);

        builder.setMessage(R.string.quit_setup_message);

        builder.setPositiveButton(R.string.quit_setup, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                mStop = true;
                deleteGame();
                startActivity(mHomeIntent);
            }
        });

        builder.setNegativeButton(R.string.cancel, mDismissDialogListener);

        dialog = builder.create();
        dialog.show();
    }

    public void deleteDialog(List<String> array, final Dialog type) {

        final View dialogView;

        mRecyclerViewAdapter = new RecyclerViewArrayAdapter(array, this, this);

        LayoutInflater inflter = LayoutInflater.from(this);
        final AlertDialog alertDialog;
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogView = inflter.inflate(R.layout.recyclerview_fragment, null);
        final RecyclerView recyclerView = (RecyclerView) dialogView.findViewById(R.id.recyclerView);

        if (type == Dialog.PRESETS) {

            dialogBuilder.setTitle(getResources().getString(R.string.delete_presets));
            dialogBuilder.setMessage(getResources().getString(R.string.delete_presets_message));

        } else {

            dialogBuilder.setTitle(getResources().getString(R.string.delete_time_limits));
            dialogBuilder.setMessage(getResources().getString(R.string.delete_time_limits_message));

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

                mRecyclerViewAdapter.deleteSelectedItems(type, NewGame.this);

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

    public void createPresetDialog() {
        final String[] presetName = {mGame.getmTitle()};

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

        dialogBuilder.setNegativeButton(R.string.cancel, mDismissDialogListener);

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
                        if (charSequence == "") {
                            presetName[0] = mDefaultTitle;
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
                        mGame.setmTitle(presetName[0]);
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
                mSnackBar.dismiss();
            }
        };

        //snackbar must have 2 or more players

        if (mGame.size() < 2) {

            mSnackBar = Snackbar.make(mRelativeLayout, R.string.more_than_two_players, Snackbar.LENGTH_SHORT)
                    .setAction("Dismiss", onClickListener);
            mSnackBar.show();

        } else {

            if (mDataHelper.checkPlayerDuplicates(getPlayerArray())) {

                mSnackBar = Snackbar.make(mRelativeLayout, R.string.duplicates_message, Snackbar.LENGTH_SHORT)
                        .setAction("Dismiss", onClickListener);
                mSnackBar.show();

            } else {

                if (startGame) {

                    int startingScore = mGame.getInt(Option.STARTING_SCORE);

                    for (Player p : getPlayerArray()) {
                        p.setmScore(startingScore);
                    }

                    updateGameInDatabase();

                    mainActivityIntent.putExtra("GAME_ID", mGameID);
                    startActivity(mainActivityIntent);
                    finish();

                } else {
                    createPresetDialog();
                }
            }
        }

    }

    public void createPreset() {

        mPresetDBAdapter = new PresetDBAdapter(this);
        mPresetDBAdapter.open();
        mPresetDBAdapter.createPreset(mGame);
        mPresetDBAdapter.close();

    }

    public void loadPreset(int position) {

        mPresetDBAdapter.open();
        mGame = mDataHelper.getPreset(position, mPresetDBAdapter);
        mGame.setmID(mGameID);
        mPresetDBAdapter.close();

        mEditTextPlayer.setText("");
        mEditTextGameTitle.setText(mGame.getmTitle());

        displayRecyclerView(false);

        loadOptions();
        setGameTime();

        updateGameInDatabase();
    }

    @Override
    public void onItemClicked(int position) {
        mRecyclerViewAdapter.toggleSelection(position);
    }

}
