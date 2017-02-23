package io.github.sdsstudios.ScoreKeeper;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.github.sdsstudios.ScoreKeeper.Activity.Activity;
import io.github.sdsstudios.ScoreKeeper.Activity.ScoreKeeperTabActivity;
import io.github.sdsstudios.ScoreKeeper.Options.CheckBoxOption;
import io.github.sdsstudios.ScoreKeeper.Options.IntEditTextOption;
import io.github.sdsstudios.ScoreKeeper.Options.Option;
import io.github.sdsstudios.ScoreKeeper.Options.StringEditTextOption;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static io.github.sdsstudios.ScoreKeeper.Activity.Activity.EDIT_GAME;

public class EditGame extends ScoreKeeperTabActivity {

    private SimpleDateFormat mDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat mHourlengthFormat = new SimpleDateFormat("hh:mm:ss:S");
    private MenuItem mMenuItemDelete, mMenuItemEdit, mMenuItemDone, mMenuItemCancel, mMenuItemAdd, mMenuItemShare, mMenuItemComplete;

    private List<MenuItem> mMenuItemList = new ArrayList<>();
    private View mEditGameContent;
    private boolean mEditableMode = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mEditGameContent = findViewById(R.id.edit_game_content);

        ImageButton buttonHelpDate = (ImageButton) findViewById(R.id.buttonHelpDate);
        ImageButton buttonHelpLength = (ImageButton) findViewById(R.id.buttonHelpLength);
        buttonHelpDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helpDialog(getString(R.string.help), getString(R.string.last_played_message));
            }
        });

        new AdCreator((AdView) findViewById(R.id.adViewHome2), this).createAd();

        buttonHelpLength.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helpDialog(getString(R.string.help), getString(R.string.length_help_message));
            }
        });

        loadOptions();
        setOptionChangeListeners();

    }

    private void updateCompleteMenuItem() {
        if (!game.ismCompleted()) {
            mMenuItemComplete.setTitle(R.string.complete);
        } else {
            mMenuItemComplete.setTitle(R.string.unfinish);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            getMenuInflater().inflate(R.menu.main, menu);
            mMenuItemList.add(mMenuItemDelete = menu.findItem(R.id.action_delete));
            mMenuItemList.add(mMenuItemDone = menu.findItem(R.id.action_done));
            mMenuItemList.add(mMenuItemEdit = menu.findItem(R.id.action_edit));
            mMenuItemList.add(mMenuItemCancel = menu.findItem(R.id.action_cancel));
            mMenuItemList.add(mMenuItemAdd = menu.findItem(R.id.action_add));
            mMenuItemList.add(mMenuItemComplete = menu.findItem(R.id.complete_game));
            mMenuItemList.add(mMenuItemShare = menu.findItem(R.id.menu_item_share).setVisible(true));
            menu.findItem(R.id.action_delete_timelimits).setVisible(true);

            menu.findItem(R.id.action_delete).setVisible(true);
            menu.findItem(R.id.action_edit).setVisible(true);
            menu.findItem(R.id.action_settings).setVisible(false);
            mMenuItemComplete.setVisible(true);
            mMenuItemAdd.setVisible(true);

            updateCompleteMenuItem();

            ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mMenuItemShare);
            mShareActionProvider.setShareIntent(shareIntent());

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private Intent shareIntent() {

        Intent intent;
        intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "");

        return intent;
    }

    private void completeGame() {

        game.setmCompleted(!game.ismCompleted());
        updateCompleteMenuItem();

        gameDBAdapter.open().updateGame(game);
        gameDBAdapter.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {

            case R.id.action_delete:
                deleteGameDialog();
                break;

            case R.id.action_edit:
                onMenuEditClick();
                break;

            case R.id.action_done:
                onMenuDoneClick();
                break;

            case R.id.action_cancel:
                onMenuCancelClick();
                break;

            case R.id.action_add:
                addPlayerDialog();
                break;

            case R.id.complete_game:
                completeGame();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void helpDialog(String title, String message) {
        showTextDialog(title, message, getString(R.string.okay));
    }

    private void onMenuEditClick() {

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        mMenuItemDelete.setVisible(false);
        mMenuItemEdit.setVisible(false);
        mMenuItemDone.setVisible(true);
        mMenuItemCancel.setVisible(true);
        mMenuItemShare.setVisible(false);
        mMenuItemComplete.setVisible(false);

        enableOptions(true);
        setOptionChangeListeners();

    }

    private void onMenuDoneClick() {

        deleteEmptyPlayers();

        final String newLength = game.getmLength();

        final boolean booleanLength;

        if (!checkValidity(newLength, mHourlengthFormat, 10) && newLength.length() != 0) {
            game.setChecked(Option.STOPWATCH, true);
            booleanLength = true;

        } else if (newLength.length() == 0 || newLength.equals("")) {
            booleanLength = false;

        } else if (checkValidity(newLength, mHourlengthFormat, 10) && newLength.length() != 0) {
            booleanLength = false;
        } else {
            booleanLength = false;
        }

        final boolean bDateAndTime = checkValidity(game.getmTime(), mDateTimeFormat, 19);
        final boolean bCheckEmpty = false;
        final boolean bCheckDuplicates = dataHelper.checkPlayerDuplicates(getPlayerArray());
        final boolean bNumPlayers = game.size() >= 2;

        DialogInterface.OnClickListener positiveClickListener = new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

                deleteEmptyPlayers();

                if (bCheckEmpty) {

                    createSnackbar(relativeLayout, "You can't have empty names!");

                } else if (!bDateAndTime) {

                    createSnackbar(relativeLayout, getString(R.string.invalid_date_and_time));

                } else if (booleanLength) {

                    createSnackbar(relativeLayout, getString(R.string.invalid_length));

                } else if (bCheckDuplicates) {

                    createSnackbar(relativeLayout, "You can't have duplicate players!");

                } else if (!bNumPlayers) {

                    createSnackbar(relativeLayout, "Must have 2 or more players");

                } else {

                    saveGameToDatabase();

                    mMenuItemDelete.setVisible(true);
                    mMenuItemDone.setVisible(false);
                    mMenuItemEdit.setVisible(true);
                    mMenuItemCancel.setVisible(false);
                    mMenuItemShare.setVisible(true);
                    mMenuItemComplete.setVisible(true);

                    enableOptions(false);
                    loadOptions();

                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }

            }
        };
        showAlertDialog(getString(R.string.edit_game_question), getString(R.string.are_you_sure_edit_game), getString(R.string.title_activity_edit_game),
                positiveClickListener, getString(R.string.cancel), dismissDialogListener);


    }

    private boolean checkValidity(String string, SimpleDateFormat simpleDateFormat, int length) {
        boolean validity = false;

        try {
            Date dateDate = simpleDateFormat.parse(string);

            if (string.length() == length) {
                validity = true;
            }

        } catch (ParseException e) {
            e.printStackTrace();

        }

        return validity;
    }

    private void onMenuCancelClick() {

        mMenuItemDelete.setVisible(true);
        mMenuItemDone.setVisible(false);
        mMenuItemEdit.setVisible(true);
        mMenuItemCancel.setVisible(false);
        mMenuItemShare.setVisible(true);
        mMenuItemComplete.setVisible(true);

        game = dataHelper.getGame(gameID, gameDBAdapter);

        enableOptions(false);
        loadOptions();
        populateSetGridView();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void deleteGameDialog() {

        showAlertDialog(getString(R.string.delete_game), getString(R.string.delete_game_message), getString(R.string.delete)
                , new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteGame();

                        if (gameDBAdapter.open().numRows() == 0) {
                            startActivity(homeIntent);
                        } else {
                            startActivity(historyIntent);
                        }

                        gameDBAdapter.close();

                    }
                }, getString(R.string.cancel), dismissDialogListener);

    }

    @Override
    public Activity getActivity() {
        return EDIT_GAME;
    }

    @Override
    public void onDialogDismissed() {
    }

    @Override
    public void chooseTab(int layout) {
        if (layout == OPTIONS_LAYOUT) {
            mEditGameContent.setVisibility(VISIBLE);
            setGridView.setVisibility(INVISIBLE);

        } else {
            mEditGameContent.setVisibility(INVISIBLE);
            setGridView.setVisibility(View.VISIBLE);

        }
    }

    @Override
    public void setOptionChangeListeners() {
        super.setOptionChangeListeners();
        for (final StringEditTextOption e : StringEditTextOptions()) {
            getEditText(e).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (!charSequence.toString().equals("")) {
                        e.setData(charSequence.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
        }
    }

    @Override
    protected boolean inEditableMode() {
        return mEditableMode;
    }

    @Override
    public void loadTimeLimit(final int position) {
        super.loadTimeLimit(position);

        final TimeLimit chosenTimeLimit = timeLimitArray.get(position);
        game.setmTimeLimit(chosenTimeLimit);

        try {
            if (timeHelper.convertToLong(game.getmLength()) > timeHelper.convertToLong(chosenTimeLimit.getmTime())) {
                DialogInterface.OnClickListener positiveClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        game.setmLength("00:00:00:0");
                        getEditText(game.getStringEditTextOption(Option.LENGTH)).setText(game.getmLength());
                    }
                };

                showAlertDialog(getString(R.string.are_you_sure), getString(R.string.time_limit_chosen_is_too_small)
                        , getString(R.string.reset_time_played), positiveClickListener, getString(R.string.ignore), dismissDialogListener)
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                chooseTimeLimitInSpinner();
                            }
                        });
            }

        } catch (ParseException e) {
            Toast.makeText(this, "error comparing timelimit to game time", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void enableOptions(boolean editable) {
        mEditableMode = editable;

        for (CheckBoxOption c : CheckBoxOptions()) {
            getCheckBox(c).setEnabled(mEditableMode);
        }

        for (IntEditTextOption e : IntEditTextOptions()) {
            getEditText(e).setEnabled(mEditableMode);
        }

        for (StringEditTextOption e : StringEditTextOptions()) {
            getEditText(e).setText(e.getString());
            getEditText(e).setEnabled(mEditableMode);
        }

        spinnerTimeLimit.setEnabled(editable);
    }


    @Override
    public void onScoreClick(int playerIndex) {
    }

    @Override
    public void onScoreLongClick(int playerIndex) {
    }

    @Override
    public void editPlayer(int playerIndex) {
    }
}
