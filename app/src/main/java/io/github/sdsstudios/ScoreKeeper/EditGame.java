package io.github.sdsstudios.ScoreKeeper;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
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
import io.github.sdsstudios.ScoreKeeper.Options.Option;
import io.github.sdsstudios.ScoreKeeper.Options.StringEditTextOption;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static io.github.sdsstudios.ScoreKeeper.Activity.Activity.EDIT_GAME;

public class EditGame extends ScoreKeeperTabActivity {

    private SimpleDateFormat mDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat mHourlengthFormat = new SimpleDateFormat("hh:mm:ss:S");
    private MenuItem mMenuItemDelete, mMenuItemEdit, mMenuItemDone, mMenuItemCancel, mMenuItemAdd
            , mMenuItemShare, mMenuItemComplete;

    private List<MenuItem> mMenuItemList = new ArrayList<>();
    private View mEditGameContent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mEditGameContent = findViewById(R.id.edit_game_content);

        ImageButton buttonHelpDate = (ImageButton) findViewById(R.id.buttonHelpDate);
        ImageButton buttonHelpLength = (ImageButton) findViewById(R.id.buttonHelpLength);
        buttonHelpDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helpDialog(getString(R.string.date_and_time_help), getString(R.string.date_and_time_help_message));
            }
        });

        new AdCreator((AdView) findViewById(R.id.adViewHome2), this).createAd();

        buttonHelpLength.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helpDialog(getString(R.string.length_help), getString(R.string.length_help_message));
            }
        });

        loadOptions();
        setOptionChangeListeners();

    }

    private void updateCompleteMenuItem(){
        if (!game.ismCompleted()) {
            mMenuItemComplete.setTitle(R.string.complete);
        }else{
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

            menu.findItem(R.id.action_delete).setVisible(true);
            menu.findItem(R.id.action_edit).setVisible(true);
            menu.findItem(R.id.action_settings).setVisible(false);
            mMenuItemComplete.setVisible(true);
            mMenuItemAdd.setVisible(true);

            updateCompleteMenuItem();

            ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mMenuItemShare);
            mShareActionProvider.setShareIntent(shareIntent());

        }catch (Exception e){
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

        switch (id){

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
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(title);

        builder.setMessage(message);

        builder.setPositiveButton(R.string.okay, dismissDialogListener);

        dialog = builder.create();
        dialog.show();
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

        if (!checkValidity(newLength, mHourlengthFormat, 10) && newLength.length() != 0){
            game.setChecked(Option.STOPWATCH, true);
            booleanLength = true;

        }else if (newLength.length() == 0|| newLength.equals("")){
            booleanLength = false;

        }else if(checkValidity(newLength, mHourlengthFormat, 10) && newLength.length() != 0){
            booleanLength = false;
        }else{
            booleanLength = false;
        }

        final boolean bDateAndTime = checkValidity(game.getmTime(), mDateTimeFormat, 19);
        final boolean bCheckEmpty = false;
        final boolean bCheckDuplicates = dataHelper.checkPlayerDuplicates(getPlayerArray());
        final boolean bNumPlayers = game.size() >= 2;

        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.edit_game_question);

        builder.setMessage(R.string.are_you_sure_edit_game);

        builder.setPositiveButton(R.string.title_activity_edit_game, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

                deleteEmptyPlayers();

                if (bCheckEmpty) {

                    createSnackbar(relativeLayout, "You can't have empty names!");

                }else if (!bDateAndTime) {

                    createSnackbar(relativeLayout, getString(R.string.invalid_date_and_time));

                }else if (booleanLength) {

                    createSnackbar(relativeLayout, getString(R.string.invalid_length));

                } else if (bCheckDuplicates) {

                    createSnackbar(relativeLayout, "You can't have duplicate players!");

                } else if (!bNumPlayers) {

                    createSnackbar(relativeLayout, "Must have 2 or more players");

                }else{

                    saveGameToDatabase();

                    mMenuItemAdd.setVisible(false);
                    mMenuItemDelete.setVisible(true);
                    mMenuItemDone.setVisible(false);
                    mMenuItemEdit.setVisible(true);
                    mMenuItemCancel.setVisible(false);
                    mMenuItemShare.setVisible(true);
                    mMenuItemComplete.setVisible(true);

                    loadOptions();

                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }

            }
        });

        builder.setNegativeButton(R.string.cancel, dismissDialogListener);

        dialog = builder.create();
        dialog.show();

    }

    private boolean checkValidity(String string, SimpleDateFormat simpleDateFormat, int length) {
        boolean validity = false;

        try {
            Date dateDate = simpleDateFormat.parse(string);

            if(string.length() == length) {
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

        loadOptions();
        populateSetGridView();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void deleteGameDialog() {

        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.delete_game);

        builder.setMessage(R.string.delete_game_message);

        builder.setPositiveButton(R.string.delete_game, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteGame();

                if (gameDBAdapter.open().numRows() == 0) {
                    startActivity(homeIntent);
                } else {
                    startActivity(historyIntent);
                }

                gameDBAdapter.close();

            }
        });

        builder.setNegativeButton(R.string.cancel, dismissDialogListener);

        dialog = builder.create();
        dialog.show();
    }

    @Override
    public Activity getActivity() {
        return EDIT_GAME;
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
                    e.setData(charSequence.toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }
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
