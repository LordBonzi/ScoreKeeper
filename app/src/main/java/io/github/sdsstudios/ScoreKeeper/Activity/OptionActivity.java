package io.github.sdsstudios.ScoreKeeper.Activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.github.sdsstudios.ScoreKeeper.AdCreator;
import io.github.sdsstudios.ScoreKeeper.Adapters.PlayerListAdapter;
import io.github.sdsstudios.ScoreKeeper.Adapters.PresetDBAdapter;
import io.github.sdsstudios.ScoreKeeper.Adapters.RecyclerViewArrayAdapter;
import io.github.sdsstudios.ScoreKeeper.Dialog;
import io.github.sdsstudios.ScoreKeeper.Options.CheckBoxOption;
import io.github.sdsstudios.ScoreKeeper.Options.EditTextOption;
import io.github.sdsstudios.ScoreKeeper.Options.IntEditTextOption;
import io.github.sdsstudios.ScoreKeeper.Options.Option;
import io.github.sdsstudios.ScoreKeeper.Options.StringEditTextOption;
import io.github.sdsstudios.ScoreKeeper.Player;
import io.github.sdsstudios.ScoreKeeper.R;
import io.github.sdsstudios.ScoreKeeper.Themes;
import io.github.sdsstudios.ScoreKeeper.TimeLimit;

import static io.github.sdsstudios.ScoreKeeper.Activity.Activity.EDIT_GAME;
import static io.github.sdsstudios.ScoreKeeper.Activity.Activity.GAME_ACTIVITY;
import static io.github.sdsstudios.ScoreKeeper.Activity.Activity.NEW_GAME;
import static io.github.sdsstudios.ScoreKeeper.Options.Option.DATE;
import static io.github.sdsstudios.ScoreKeeper.Options.Option.DICE_MAX;
import static io.github.sdsstudios.ScoreKeeper.Options.Option.DICE_MIN;
import static io.github.sdsstudios.ScoreKeeper.Options.Option.LENGTH;
import static io.github.sdsstudios.ScoreKeeper.Options.Option.NOTES;
import static io.github.sdsstudios.ScoreKeeper.Options.Option.NUMBER_SETS;
import static io.github.sdsstudios.ScoreKeeper.Options.Option.REVERSE_SCORING;
import static io.github.sdsstudios.ScoreKeeper.Options.Option.SCORE_DIFF_TO_WIN;
import static io.github.sdsstudios.ScoreKeeper.Options.Option.SCORE_INTERVAL;
import static io.github.sdsstudios.ScoreKeeper.Options.Option.STARTING_SCORE;
import static io.github.sdsstudios.ScoreKeeper.Options.Option.STOPWATCH;
import static io.github.sdsstudios.ScoreKeeper.Options.Option.TITLE;
import static io.github.sdsstudios.ScoreKeeper.Options.Option.WINNING_SCORE;

/**
 * Created by seth on 11/12/16.
 */

public abstract class OptionActivity extends ScoreKeeperActivity implements PlayerListAdapter.PlayerListAdapterListener, RecyclerViewArrayAdapter.ClickListener {

    public static final String STATE_GAMEID = "gameID";

    public PresetDBAdapter presetDBAdapter;
    public PlayerListAdapter playerListAdapter;
    public RelativeLayout relativeLayout;
    public int gameID;
    public RecyclerView playerRecyclerView;
    public RecyclerView.LayoutManager layoutManager;
    public Spinner spinnerPreset, spinnerTimeLimit;
    public List<TimeLimit> timeLimitArray = new ArrayList<>();
    public AdapterView.OnItemSelectedListener onTimeLimitSelectedListener;

    private RecyclerViewArrayAdapter mRecyclerViewAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CURRENT_ACTIVITY = getActivity();

        if (CURRENT_ACTIVITY != GAME_ACTIVITY) {

            Themes.themeActivity(this, CURRENT_ACTIVITY == EDIT_GAME ? R.layout.activity_edit_game : R.layout.activity_new_game
                    , true);

            AdView mAdView = (AdView) findViewById(R.id.adViewHome);
            AdCreator adCreator = new AdCreator(mAdView, this);
            adCreator.createAd();

            timeLimitArray = TimeLimit.getTimeLimitArray(this);

            if (timeLimitArray == null) {
                timeLimitArray = new ArrayList<>();
            }

            playerRecyclerView = (RecyclerView) findViewById(R.id.playerRecyclerView);
            spinnerTimeLimit = (Spinner) findViewById(R.id.spinnerTimeLimit);

            gameDBAdapter.open();

            onTimeLimitSelectedListener = new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                    if (i == 0) {
                        game.noTimeLimit();
                    } else if (i == 1) {
                        timeLimitDialog();
                    } else {
                        if (CURRENT_ACTIVITY == EDIT_GAME) {

                            if (!game.getmTimeLimit().getmTime().equals(timeLimitArray.get(i - 2).getmTime())) {
                                loadTimeLimit(i - 2);
                            }

                        } else {
                            loadTimeLimit(i - 2);
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            };

            spinnerTimeLimit.setOnItemSelectedListener(onTimeLimitSelectedListener);

            if (CURRENT_ACTIVITY == EDIT_GAME) {

                AdView mAdView2 = (AdView) findViewById(R.id.adViewHome2);
                AdCreator adCreator2 = new AdCreator(mAdView2, this);
                adCreator2.createAd();

                relativeLayout = (RelativeLayout) findViewById(R.id.layoutEditGame);

                Bundle extras = getIntent().getExtras();

                gameID = extras.getInt("GAME_ID");

                game = dataHelper.getGame(gameID, gameDBAdapter);

            } else {

                relativeLayout = (RelativeLayout) findViewById(R.id.newGameLayout);
            }

            displaySpinner(spinnerTimeLimit, timeLimitStringArray());

        }
    }

    protected void deleteDialog(List<String> array, final Dialog type) {

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
                    presetDBAdapter.open();
                    presetDBAdapter.deleteAllPresets();
                    presetDBAdapter.close();
                    displaySpinner(spinnerPreset, presetStringArray());

                } else {

                    TimeLimit.deleteAllTimeLimits(OptionActivity.this);
                    timeLimitArray = TimeLimit.getTimeLimitArray(OptionActivity.this);
                    displaySpinner(spinnerTimeLimit, timeLimitStringArray());

                }
            }
        });

        dialogBuilder.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                mRecyclerViewAdapter.deleteSelectedItems();

                if (type == Dialog.PRESETS) {
                    displaySpinner(spinnerPreset, presetStringArray());

                } else {

                    timeLimitArray = TimeLimit.getTimeLimitArray(OptionActivity.this);
                    displaySpinner(spinnerTimeLimit, timeLimitStringArray());

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

    public List<String> presetStringArray() {
        List<String> arrayList = new ArrayList<>();
        arrayList.add("No Preset");

        for (int i = 1; i <= presetDBAdapter.open().numRows(); i++) {
            presetDBAdapter.open();
            arrayList.add(dataHelper.getPreset(i, presetDBAdapter).getmTitle());
            presetDBAdapter.close();
        }

        return arrayList;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_delete_timelimits:
                if (timeLimitStringArray().size() > 2) {
                    deleteDialog(timeLimitStringArray(), Dialog.TIME_LIMIT);
                } else {
                    Toast.makeText(this, "No Time Limits Created", Toast.LENGTH_SHORT).show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);

    }

    private void checkStopwatchCheckBox() {
        game.setChecked(STOPWATCH, true);
        loadCheckBox(game.getCheckBoxOption(Option.STOPWATCH));
    }

    public void loadTimeLimit(int arrayIndex) {
        checkStopwatchCheckBox();
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

                            if (timeLimitArray != null) {
                                timeLimitArray.add(timeLimit);
                            } else {
                                timeLimitArray = new ArrayList<>();
                            }

                            if (dataHelper.checkDuplicates(timeLimitStringArray())) {

                                timeLimitArray.remove(timeLimitArray.size() - 1);
                                TimeLimit.saveTimeLimitArray(timeLimitArray, OptionActivity.this);
                                displaySpinner(spinnerTimeLimit, timeLimitStringArray());
                                spinnerPreset.setSelection(0);
                                Toast.makeText(OptionActivity.this, "Time limit already exists", Toast.LENGTH_SHORT).show();

                            } else {
                                saveGameToDatabase();
                                dialog.dismiss();
                                TimeLimit.saveTimeLimitArray(timeLimitArray, OptionActivity.this);
                                displaySpinner(spinnerTimeLimit, timeLimitStringArray());
                                spinnerTimeLimit.setSelection(timeLimitArray.size() + 1);
                            }

                        } else {

                            dialog.dismiss();
                            spinnerTimeLimit.setSelection(0);
                            game.noTimeLimit();

                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.toString());
                    Toast toast = Toast.makeText(OptionActivity.this, R.string.invalid_length, Toast.LENGTH_SHORT);
                    toast.show();
                }

            }
        };

        showCustomAlertDialog(getString(R.string.create_time_limit), null, getString(R.string.create), positiveClickListener,
                getString(R.string.cancel), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        spinnerTimeLimit.setSelection(0);
                        game.noTimeLimit();
                    }
                }, dialogView);

    }

    private List<String> timeLimitStringArray() {
        List<String> arrayList = new ArrayList<>();
        arrayList.add("No Time Limit");
        arrayList.add("Create...");

        if (timeLimitArray != null) {
            for (TimeLimit timeLimit : timeLimitArray) {
                arrayList.add(timeLimit.getmTitle());
            }
        }

        return arrayList;
    }

    public void displaySpinner(final Spinner spinner, List<String> array) {

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, array);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

    }

    public List<CheckBoxOption> CheckBoxOptions() {
        return game.getmCheckBoxOptions(this);
    }

    public List<IntEditTextOption> IntEditTextOptions() {
        return game.getmIntEditTextOptions(this);
    }

    public List<StringEditTextOption> StringEditTextOptions() {
        return game.getmStringEditTextOptions(this);
    }

    public void chooseTimeLimitInSpinner() {
        boolean contains = false;
        TimeLimit timeLimit = game.getmTimeLimit();

        if (timeLimit != null) {
            for (int i = 0; i < timeLimitArray.size(); i++) {
                if (timeLimitArray.get(i).getmTitle().equals(timeLimit.getmTitle())) {
                    spinnerTimeLimit.setSelection(i + 2);
                    contains = true;
                    break;
                }
            }

            if (!contains && !timeLimit.getmTitle().equals("")) {
                try {
                    timeLimitArray.add(timeLimit);
                    TimeLimit.saveTimeLimitArray(timeLimitArray, this);
                    displaySpinner(spinnerTimeLimit, timeLimitStringArray());
                    spinnerTimeLimit.setSelection(timeLimitArray.size() + 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

    }

    public void setOptionChangeListeners() {

        for (final CheckBoxOption c : CheckBoxOptions()) {
            getCheckBox(c).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    c.setData(b);
                    saveGameToDatabase();
                }
            });
        }

        for (final IntEditTextOption e : IntEditTextOptions()) {

            getEditText(e).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    try {

                        /** if statement necessary to avoid numberformatexception if edittext empty **/
                        if (charSequence != "") {
                            e.setData(Integer.valueOf(charSequence.toString()));
                        } else {
                            e.setData(e.getmDefaultValue());
                        }

                    } catch (NumberFormatException error) {
                        e.setData(e.getmDefaultValue());

                    }

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (CURRENT_ACTIVITY != EDIT_GAME) {
                        saveGameToDatabase();
                    }

                }
            });
        }

        if (CURRENT_ACTIVITY == NEW_GAME) {
            final StringEditTextOption notesOption = game.getStringEditTextOption(Option.NOTES);

            getEditText(notesOption).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    notesOption.setData(s.toString());

                }

                @Override
                public void afterTextChanged(Editable s) {
                    saveGameToDatabase();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameDBAdapter.open();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameDBAdapter.close();
    }

    @Override
    protected void onStop() {
        super.onStop();
        gameDBAdapter.close();
    }

    public CheckBox getCheckBox(CheckBoxOption checkBoxOption) {
        switch (checkBoxOption.getmID()) {
            case REVERSE_SCORING:
                checkBoxOption.setmCheckBoxID(R.id.checkBoxReverseScoring);
                return ((CheckBox) findViewById(R.id.checkBoxReverseScoring));

            case STOPWATCH:
                checkBoxOption.setmCheckBoxID(R.id.checkBoxStopwatch);
                return ((CheckBox) findViewById(R.id.checkBoxStopwatch));

            default:
                return null;

        }
    }

    public EditText getEditText(EditTextOption editTextOption) {
        switch (editTextOption.getmID()) {
            case NUMBER_SETS:
                editTextOption.setmEditTextID(R.id.editTextNumSets);
                return ((EditText) findViewById(R.id.editTextNumSets));

            case SCORE_DIFF_TO_WIN:
                editTextOption.setmEditTextID(R.id.editTextDiffToWin);
                return ((EditText) findViewById(R.id.editTextDiffToWin));

            case WINNING_SCORE:
                editTextOption.setmEditTextID(R.id.editTextMaxScore);
                return ((EditText) findViewById(R.id.editTextMaxScore));

            case STARTING_SCORE:
                editTextOption.setmEditTextID(R.id.editTextStartingScore);
                return ((EditText) findViewById(R.id.editTextStartingScore));

            case SCORE_INTERVAL:
                editTextOption.setmEditTextID(R.id.editTextScoreInterval);
                return ((EditText) findViewById(R.id.editTextScoreInterval));

            case LENGTH:
                editTextOption.setmEditTextID(R.id.editTextLength);
                return ((EditText) findViewById(R.id.editTextLength));

            case TITLE:
                editTextOption.setmEditTextID(R.id.editTextTitle);
                return ((EditText) findViewById(R.id.editTextTitle));

            case DATE:
                editTextOption.setmEditTextID(R.id.editTextLastPlayed);
                return ((EditText) findViewById(R.id.editTextLastPlayed));

            case DICE_MAX:
                editTextOption.setmEditTextID(R.id.editTextDiceMax);
                return ((EditText) findViewById(R.id.editTextDiceMax));

            case DICE_MIN:
                editTextOption.setmEditTextID(R.id.editTextDiceMin);
                return ((EditText) findViewById(R.id.editTextDiceMin));

            case NOTES:
                editTextOption.setmEditTextID(R.id.editText);
                return ((EditText) findViewById(R.id.editText));

            default:
                return null;

        }
    }

    public void displayRecyclerView(boolean editable) {
        playerRecyclerView.setVisibility(View.VISIBLE);
        layoutManager = new LinearLayoutManager(this);
        playerRecyclerView.setLayoutManager(layoutManager);
        playerListAdapter = new PlayerListAdapter(editable, relativeLayout, this);
        playerRecyclerView.setAdapter(playerListAdapter);
    }

    public void setGameTime() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        game.setmTime(sdfDate.format(now));
    }

    public void deleteEmptyPlayers() {
        List<Player> playerArray = game.getmPlayerArray();

        for (int i = 0; i < playerArray.size(); i++) {
            if (playerArray.get(i).getmName().equals("")
                    || playerArray.get(i).getmName() == null
                    || playerArray.get(i).getmName().equals(" ")) {
                playerArray.remove(i);
            }
        }

        game.setmPlayerArray(playerArray);

    }

    protected abstract boolean inEditableMode();

    protected void loadEditText(EditTextOption editTextOption) {
        EditText editText = getEditText(editTextOption);

        if (editTextOption instanceof IntEditTextOption) {
            if (((IntEditTextOption) editTextOption).getmDefaultValue() != editTextOption.getInt()) {
                editText.setText(String.valueOf(editTextOption.getInt()));
            } else {
                editText.setText("");
            }
        } else {
            editText.setHint(editTextOption.getString());
            editText.setEnabled(inEditableMode());
        }

        if (CURRENT_ACTIVITY == EDIT_GAME) {
            editText.setEnabled(inEditableMode());
        }
    }

    protected void loadCheckBox(CheckBoxOption checkBoxOption) {
        CheckBox checkBox = getCheckBox(checkBoxOption);

        checkBox.setChecked(checkBoxOption.isChecked());

        if (CURRENT_ACTIVITY == EDIT_GAME) {
            checkBox.setEnabled(inEditableMode());
        }
    }

    protected void loadOptions() {
        for (IntEditTextOption e : IntEditTextOptions()) {
            loadEditText(e);
        }

        for (CheckBoxOption c : CheckBoxOptions()) {
            loadCheckBox(c);
        }

        if (CURRENT_ACTIVITY == NEW_GAME) {
            StringEditTextOption notesOption = game.getStringEditTextOption(Option.NOTES);
            getEditText(notesOption).setText(notesOption.getString());
        }

        if (CURRENT_ACTIVITY == EDIT_GAME) {
            for (StringEditTextOption e : StringEditTextOptions()) {
                loadEditText(e);
            }

            spinnerTimeLimit.setEnabled(inEditableMode());
        }

        chooseTimeLimitInSpinner();

    }

    /**
     * OnPlayerChangeListener
     **/

    @Override
    public void onPlayerChange(Player player, int position) {
        game.setPlayer(player, position);
        saveGameToDatabase();

    }

    public void deleteGame() {
        gameDBAdapter.open().deleteGame(gameID);
        gameDBAdapter.close();
    }

    public boolean validPlayer(Player player) {
        boolean areDuplicatePlayers = dataHelper.checkPlayerDuplicates(game.getmPlayerArray());
        String playerName = player.getmName();

        if (areDuplicatePlayers) {
            createSnackbar(relativeLayout, getString(R.string.duplicates_message));
            return false;

        }

        if (playerName.equals("") || playerName.equals(" ")) {
            createSnackbar(relativeLayout, getString(R.string.must_have_name));
            return false;

        } else return !areDuplicatePlayers && !playerName.equals("") && !playerName.equals(" ");
    }

    @Override
    public void addPlayerToGame(Player player, int position) {

        game.addPlayerAtPosition(player, position);

        if (validPlayer(player)) {
            saveGameToDatabase();
        } else {
            game.removePlayer(position);
        }

    }

    @Override
    public List<Player> getPlayerArray() {
        return game.getmPlayerArray();
    }

    @Override
    public void onPlayerRemove(int position) {
        game.removePlayer(position);
    }


    @Override
    public void onItemClicked(int position) {
        mRecyclerViewAdapter.toggleSelection(position);
    }

}
