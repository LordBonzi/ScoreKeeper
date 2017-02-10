package io.github.sdsstudios.ScoreKeeper.Activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.AdView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.github.sdsstudios.ScoreKeeper.AdCreator;
import io.github.sdsstudios.ScoreKeeper.Adapters.PlayerListAdapter;
import io.github.sdsstudios.ScoreKeeper.Options.CheckBoxOption;
import io.github.sdsstudios.ScoreKeeper.Options.EditTextOption;
import io.github.sdsstudios.ScoreKeeper.Options.IntEditTextOption;
import io.github.sdsstudios.ScoreKeeper.Options.Option;
import io.github.sdsstudios.ScoreKeeper.Options.StringEditTextOption;
import io.github.sdsstudios.ScoreKeeper.Player;
import io.github.sdsstudios.ScoreKeeper.R;
import io.github.sdsstudios.ScoreKeeper.Themes;

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

public abstract class OptionActivity extends ScoreKeeperActivity implements PlayerListAdapter.PlayerListAdapterListener {

    public static final String STATE_GAMEID = "gameID";

    public PlayerListAdapter playerListAdapter;
    public RelativeLayout relativeLayout;
    public int gameID;
    public RecyclerView playerRecyclerView;
    public RecyclerView.LayoutManager layoutManager;
    private NestedScrollView mScrollView;

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

            mScrollView = (NestedScrollView) findViewById(R.id.scrollView);
            playerRecyclerView = (RecyclerView) findViewById(R.id.playerRecyclerView);

            gameDBAdapter.open();

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

        }
    }

    public List<CheckBoxOption> CheckBoxOptions() {
        return game.getmCheckBoxOptions();
    }

    public List<IntEditTextOption> IntEditTextOptions() {
        return game.getmIntEditTextOptions();
    }

    public List<StringEditTextOption> StringEditTextOptions() {
        return game.getmStringEditTextOptions();
    }


    public void enableOptions(boolean enabled) {

        for (CheckBoxOption c : CheckBoxOptions()) {
            getCheckBox(c).setEnabled(enabled);
        }

        for (IntEditTextOption e : IntEditTextOptions()) {
            getEditText(e).setEnabled(enabled);
        }

        for (StringEditTextOption e : StringEditTextOptions()) {
            getEditText(e).setText(e.getString());
            getEditText(e).setEnabled(enabled);
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
                    saveGameToDatabase();

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
        try {

            return ((CheckBox) findViewById(checkBoxOption.getmCheckBoxID()));

        } catch (ClassCastException e) {

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
    }

    public EditText getEditText(EditTextOption editTextOption) {
        try {

            return ((EditText) findViewById(editTextOption.getmEditTextID()));

        } catch (ClassCastException e) {

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
                    editTextOption.setmEditTextID(R.id.editTextDate);
                    return ((EditText) findViewById(R.id.editTextDate));

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

    public void loadOptions() {

        for (IntEditTextOption e : IntEditTextOptions()) {
            EditText editText = getEditText(e);

            if (e.getmDefaultValue() != e.getInt()) {
                editText.setText(String.valueOf(e.getInt()));
            } else {
                editText.setText("");
            }

            if (CURRENT_ACTIVITY == EDIT_GAME) {
                editText.setEnabled(false);
            }
        }

        for (CheckBoxOption c : CheckBoxOptions()) {
            CheckBox checkBox = getCheckBox(c);

            checkBox.setChecked(c.isChecked());

            if (CURRENT_ACTIVITY == EDIT_GAME) {
                checkBox.setEnabled(false);
            }
        }

        if (CURRENT_ACTIVITY == NEW_GAME) {
            StringEditTextOption notesOption = game.getStringEditTextOption(Option.NOTES);
            getEditText(notesOption).setText(notesOption.getString());
        }

        if (CURRENT_ACTIVITY == EDIT_GAME) {
            for (StringEditTextOption e : StringEditTextOptions()) {
                EditText editText = getEditText(e);
                editText.setHint(e.getString());
                editText.setEnabled(false);
            }
        }

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
}
