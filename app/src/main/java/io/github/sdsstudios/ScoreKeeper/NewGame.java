package io.github.sdsstudios.ScoreKeeper;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import io.github.sdsstudios.ScoreKeeper.Activity.Activity;
import io.github.sdsstudios.ScoreKeeper.Activity.OptionActivity;
import io.github.sdsstudios.ScoreKeeper.Adapters.PresetDBAdapter;
import io.github.sdsstudios.ScoreKeeper.Options.CheckBoxOption;
import io.github.sdsstudios.ScoreKeeper.Options.IntEditTextOption;
import io.github.sdsstudios.ScoreKeeper.Options.Option;
import io.github.sdsstudios.ScoreKeeper.Options.StringEditTextOption;

import static io.github.sdsstudios.ScoreKeeper.Activity.Activity.NEW_GAME;

public class NewGame extends OptionActivity
        implements View.OnClickListener {

    private EditText mEditTextPlayer, mEditTextGameTitle;
    private Button mButtonNewGame, mButtonAddPlayer, mButtonQuit, mButtonCreatePreset;
    private boolean mStop = true;
    private String mDefaultTitle;

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

        spinnerPreset = (Spinner) findViewById(R.id.spinnerPreset);

        mButtonCreatePreset = (Button) findViewById(R.id.buttonCreatePreset);
        mButtonCreatePreset.setOnClickListener(this);
        mButtonCreatePreset.setVisibility(View.VISIBLE);

        presetDBAdapter = new PresetDBAdapter(this);

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

        spinnerPreset.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        displaySpinner(spinnerPreset, presetStringArray());

        displayRecyclerView(false);
        setOptionChangeListeners();

        saveGameToDatabase();

    }

    @Override
    public void loadTimeLimit(int arrayIndex) {
        game.setmTimeLimit(timeLimitArray.get(arrayIndex));
        saveGameToDatabase();
        super.loadTimeLimit(arrayIndex);
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

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_delete_presets:
                if (presetDBAdapter.open().numRows() != 0) {
                    deleteDialog(presetStringArray(), Dialog.PRESETS);
                } else {
                    Toast.makeText(this, "No Presets Created", Toast.LENGTH_SHORT).show();
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
        game.noTimeLimit();

        for (IntEditTextOption e : IntEditTextOptions()) {
            getEditText(e).setText("");
            getEditText(e).setHint(e.getmHint());
        }

        for (CheckBoxOption c : CheckBoxOptions()) {
            getCheckBox(c).setChecked(c.isChecked());
        }

        spinnerPreset.setSelection(0);
        spinnerTimeLimit.setSelection(0);

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
    protected boolean inEditableMode() {
        return true;
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
                        displaySpinner(spinnerPreset, presetStringArray());
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

        presetDBAdapter = new PresetDBAdapter(this);
        presetDBAdapter.open();
        presetDBAdapter.createPreset(game);
        presetDBAdapter.close();

    }

    private void loadPreset(int position) {

        presetDBAdapter.open();
        game = dataHelper.getPreset(position, presetDBAdapter);
        game.setmID(gameID);
        presetDBAdapter.close();

        mEditTextPlayer.setText("");
        mEditTextGameTitle.setText(game.getmTitle());

        displayRecyclerView(false);

        loadOptions();
        setOptionChangeListeners();

        setGameTime();

        saveGameToDatabase();
    }

}
