package io.github.sdsstudios.ScoreKeeper;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.github.sdsstudios.ScoreKeeper.Options.Option;
import io.github.sdsstudios.ScoreKeeper.Options.StringEditTextOption;

import static io.github.sdsstudios.ScoreKeeper.Activity.EDIT_GAME;

public class EditGame extends OptionActivity {

    private SimpleDateFormat mDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat mHourlengthFormat = new SimpleDateFormat("hh:mm:ss:S");
    private MenuItem mMenuItemDelete, mMenuItemEdit, mMenuItemDone, mMenuItemCancel, mMenuItemAdd
            , mMenuItemShare, mMenuItemComplete;

    private List<MenuItem> mMenuItemList = new ArrayList<>();

    @Override
    void loadActivity(Bundle savedInstanceState) {

        ImageButton buttonHelpDate = (ImageButton) findViewById(R.id.buttonHelpDate);
        ImageButton buttonHelpLength = (ImageButton) findViewById(R.id.buttonHelpLength);
        buttonHelpDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helpDialog(getString(R.string.date_and_time_help), getString(R.string.date_and_time_help_message));
            }
        });

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
        if (!mGame.ismCompleted()){
            mMenuItemComplete.setTitle(R.string.complete);
        }else{
            mMenuItemComplete.setTitle(R.string.unfinish);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

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

            updateCompleteMenuItem();

            ShareActionProvider mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mMenuItemShare);
            mShareActionProvider.setShareIntent(shareIntent());

        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    public Intent shareIntent(){

        Intent intent;
        intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "");

        return intent;
    }

    public void completeGame(){

        mGame.setmCompleted(!mGame.ismCompleted());
        updateCompleteMenuItem();

        mDbHelper.open().updateGame(mGame);
        mDbHelper.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case R.id.action_delete:
                delete();
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
                mGame.addNewPlayer(new Player("", mGame.getInt(Option.OptionID.STARTING_SCORE)));
                mPlayerListAdapter.notifyItemInserted(mGame.size());
                break;

            case R.id.complete_game:
                completeGame();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    public void helpDialog(String title, String message){
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(title);

        builder.setMessage(message);

        builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        dialog = builder.create();
        dialog.show();
    }

    public void onMenuEditClick() {

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        mMenuItemAdd.setVisible(true);
        mMenuItemDelete.setVisible(false);
        mMenuItemEdit.setVisible(false);
        mMenuItemDone.setVisible(true);
        mMenuItemCancel.setVisible(true);
        mMenuItemShare.setVisible(false);
        mMenuItemComplete.setVisible(false);

        enableOptions(true);
        setOptionChangeListeners();

    }

    public void onMenuDoneClick() {

        deleteEmptyPlayers();

        for (final StringEditTextOption e : StringEditTextOptions()) {
            e.setData(getEditText(e).getText().toString());
        }

        final String newLength = mGame.getmLength();

        final boolean booleanLength;

        if (!checkValidity(newLength, mHourlengthFormat, 10) && newLength.length() != 0){
            mGame.setChecked(Option.OptionID.STOPWATCH, true);
            booleanLength = true;

        }else if (newLength.length() == 0|| newLength.equals("")){
            booleanLength = false;

        }else if(checkValidity(newLength, mHourlengthFormat, 10) && newLength.length() != 0){
            booleanLength = false;
        }else{
            booleanLength = false;
        }

        final boolean bDateAndTime = checkValidity(mGame.getmTime(), mDateTimeFormat, 19);
        final boolean bCheckEmpty = false;
        final boolean bCheckDuplicates = mDataHelper.checkPlayerDuplicates(mGame.getmPlayerArray());
        final boolean bNumPlayers = mGame.size() >= 2;

        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.edit_game_question);

        builder.setMessage(R.string.are_you_sure_edit_game);

        builder.setPositiveButton(R.string.title_activity_edit_game, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {

                deleteEmptyPlayers();

                if (bCheckEmpty) {

                    invalidSnackbar("You can't have empty names!");

                }else if (!bDateAndTime) {

                    invalidSnackbar(getString(R.string.invalid_date_and_time));

                }else if (booleanLength) {

                    invalidSnackbar(getString(R.string.invalid_length));

                } else if (bCheckDuplicates) {

                    invalidSnackbar("You can't have duplicate players!");

                } else if (!bNumPlayers) {

                    invalidSnackbar("Must have 2 or more players");

                }else{

                    updateGame();

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

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        dialog = builder.create();
        dialog.show();

    }

    public boolean checkValidity(String string, SimpleDateFormat simpleDateFormat, int length) {
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

    public void onMenuCancelClick(){

        mMenuItemDelete.setVisible(true);
        mMenuItemDone.setVisible(false);
        mMenuItemEdit.setVisible(true);
        mMenuItemAdd.setVisible(false);
        mMenuItemCancel.setVisible(false);
        mMenuItemShare.setVisible(true);
        mMenuItemComplete.setVisible(true);

        mGame = mDataHelper.getGame(mGameID, mDbHelper);

        loadOptions();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    public void delete(){
        AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.delete_game);

        builder.setMessage(R.string.delete_game_message);

        builder.setPositiveButton(R.string.delete_game, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteGame();
                startActivity(new Intent(EditGame.this, History.class));
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
    Activity getActivity() {
        return EDIT_GAME;
    }

}
