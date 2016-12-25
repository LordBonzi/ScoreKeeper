package io.github.sdsstudios.ScoreKeeper;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class NewGame extends OptionActivity
        implements View.OnClickListener, RecyclerViewArrayAdapter.ViewHolder.ClickListener {

    private PresetDBAdapter mPresetDBAdapter;
    private EditText mEditTextGameTitle;
    private Button mButtonNewGame, mButtonQuit;
    private boolean mStop = true;
    private String mDefaultTitle;

    private List<TimeLimit> mTimeLimitArray = new ArrayList<>();

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state

        mStop = false;
        updateGame();

        savedInstanceState.putInt(STATE_GAMEID, mGameID);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    Activity getActivity() {
        return Activity.NEW_GAME;
    }

    @Override
    public void loadActivity(Bundle savedInstanceState) {

        mTimeLimitArray = TimeLimit.getTimeLimitArray(this);

        if (mTimeLimitArray == null) {
            mTimeLimitArray = new ArrayList<>();
        }

        mPresetDBAdapter = new PresetDBAdapter(this);

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
                updateGame();
            }
        });

        if (savedInstanceState != null) {

            mDbHelper.open();
            mGameID = savedInstanceState.getInt(STATE_GAMEID);

            mGame = mDataHelper.getGame(mGameID, mDbHelper);

        } else {

            mGame = new Game(new ArrayList<Player>(), null, false, 0
                    , IntEditTextOption.loadEditTextOptions(this), CheckBoxOption.loadCheckBoxOptions(NewGame.this), StringEditTextOption.loadEditTextOptions(this), null);

            mDbHelper.open().createGame(mGame);

            mGame.setmLength("00:00:00:0");
            mGame.setmTitle(mEditTextGameTitle.getText().toString().trim());

            mGameID = mDbHelper.getNewestGame();
            mGame.setmID(mGameID);

        }

        mButtonNewGame = (Button) findViewById(R.id.buttonNewGame);
        mButtonNewGame.setOnClickListener(this);

        mButtonQuit = (Button) findViewById(R.id.buttonQuit);
        mButtonQuit.setOnClickListener(this);

        setGameTime();

        updateGame();

        setOptionChangeListeners();

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

            case R.id.action_reset:
                reset();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void reset() {
        mGame.setmTitle("The Game With No Name");
        mEditTextGameTitle.setText(mGame.getmTitle());
        mGame.getmPlayerArray().clear();

        mGame.setmCheckBoxOptions(CheckBoxOption.loadCheckBoxOptions(NewGame.this));
        mGame.setmIntEditTextOption(IntEditTextOption.loadEditTextOptions(this));
        mGame.setmStringEditTextOptions(StringEditTextOption.loadEditTextOptions(this));

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mStop) {
            mDbHelper.open();
            mDbHelper.deleteGame(mGameID);
            mDbHelper.close();
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
                mDbHelper.open();
                mDbHelper.deleteGame(mGameID);
                mDbHelper.close();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

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

        if (mGame.size() < 2) {

            mSnackBar = Snackbar.make(mRelativeLayout, R.string.more_than_two_players, Snackbar.LENGTH_SHORT)
                    .setAction("Dismiss", onClickListener);
            mSnackBar.show();

        } else {

            if (mDataHelper.checkPlayerDuplicates(mGame.getmPlayerArray())) {

                mSnackBar = Snackbar.make(mRelativeLayout, R.string.duplicates_message, Snackbar.LENGTH_SHORT)
                        .setAction("Dismiss", onClickListener);
                mSnackBar.show();

            } else {

                if (startGame) {

                    int startingScore = mGame.getInt(Option.OptionID.STARTING_SCORE);

                    for (Player p : mGame.getmPlayerArray()) {
                        p.setmScore(startingScore);
                    }

                    updateGame();

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

        mEditTextGameTitle.setText(mGame.getmTitle());

        setGameTime();

        updateGame();
    }

    @Override
    public void onItemClicked(int position) {
    }

    enum Delete {
        PRESETS, TIME_LIMIT
    }
}
