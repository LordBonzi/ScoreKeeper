package io.github.sdsstudios.ScoreKeeper;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;

import io.github.sdsstudios.ScoreKeeper.Activity.Activity;
import io.github.sdsstudios.ScoreKeeper.Activity.ScoreKeeperActivity;
import io.github.sdsstudios.ScoreKeeper.Adapters.HistoryAdapter;
import io.github.sdsstudios.ScoreKeeper.Listeners.UpdateTabsListener;

public class History extends ScoreKeeperActivity implements UpdateTabsListener, HistoryAdapter.ViewHolder.ClickListener {

    private RecyclerView mRecyclerView;
    private MenuItem menuItemCompleted;
    private MenuItem menuItemUnfinished;
    private HistoryAdapter mHistoryAdapter;
    private ActionMode mHistoryActionMode = null;
    private int mPrimaryDarkColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrimaryDarkColor = sharedPreferences.getInt("prefPrimaryDarkColor"
                , Themes.DEFAULT_PRIMARY_DARK_COLOR(this));

        Themes.themeActivity(this, R.layout.activity_recyclerview, true);

        AdView mAdView = (AdView) findViewById(R.id.adViewHome);
        AdCreator adCreator = new AdCreator(mAdView, this);
        adCreator.createAd();

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

    }

    @Override
    public Activity getActivity() {
        return Activity.HISTORY;
    }

    @Override
    public void onDialogDismissed() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        menuItemUnfinished = menu.findItem(R.id.action_unfinished);
        menuItemUnfinished.setVisible(true);
        menuItemUnfinished.setChecked(true);


        menuItemCompleted = menu.findItem(R.id.action_completed);
        menuItemCompleted.setVisible(true);
        menuItemCompleted.setChecked(true);


        MenuItem settingsMenuItem = menu.findItem(R.id.action_settings);
        settingsMenuItem.setVisible(false);

        displayRecyclerView();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameDBAdapter.open();

    }

    @Override
    protected void onPause() {
        super.onPause();
        gameDBAdapter.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(settingsIntent);
            return true;
        }
        if (id == R.id.action_about) {
            startActivity(aboutIntent);
            return true;
        }
        if (id == R.id.action_unfinished) {
            if (!menuItemCompleted.isChecked()) {
                Toast.makeText(this, "One must be checked at all times", Toast.LENGTH_SHORT).show();
            } else {
                menuItemUnfinished.setChecked(!menuItemUnfinished.isChecked());
                displayRecyclerView();
            }

        }
        if (id == R.id.action_completed) {

            if (!menuItemUnfinished.isChecked()) {
                Toast.makeText(this, "One must be checked at all times", Toast.LENGTH_SHORT).show();
            } else {
                menuItemCompleted.setChecked(!menuItemCompleted.isChecked());

                displayRecyclerView();
            }


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public synchronized void displayRecyclerView() {
        gameDBAdapter.open();

        try {
            if (gameDBAdapter.numRows() != 0) {
                int gamesToShow = HistoryAdapter.BOTH;

                if (menuItemCompleted.isChecked()) {
                    gamesToShow = HistoryAdapter.COMPLETED;
                }

                if (menuItemUnfinished.isChecked()) {
                    gamesToShow = HistoryAdapter.UNFINISHED;
                }

                if (menuItemCompleted.isChecked() && menuItemUnfinished.isChecked()) {
                    gamesToShow = HistoryAdapter.BOTH;
                }

                RecyclerView.LayoutManager mLayoutManager;
                mLayoutManager = new LinearLayoutManager(this);
                mRecyclerView.setLayoutManager(mLayoutManager);

                mHistoryAdapter = new HistoryAdapter(HistoryModel.getHistoryModelList(gameDBAdapter, this, Activity.HISTORY, gamesToShow)
                        , this, this, Activity.HISTORY);

                mRecyclerView.setAdapter(mHistoryAdapter);
            } else {
                mRecyclerView.setVisibility(View.INVISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
            Toast.makeText(this, "Error opening History", Toast.LENGTH_SHORT).show();

        }

        gameDBAdapter.close();
    }


    @Override
    public void gamesDeleted() {
        displayRecyclerView();

    }

    @Override
    public void multiSelectEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.primaryBlueDark));
        }

    }

    private void updateActionModeCount() {
        int count = mHistoryAdapter.getSelectedItemCount();

        if (count == 0) {
            mHistoryActionMode.finish();
        } else {

            try {
                mHistoryActionMode.invalidate();
                mHistoryActionMode.setTitle(count + " items selected");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void toggleSelection(int position, int gameID) {
        mHistoryAdapter.toggleSelection(position, gameID);
        updateActionModeCount();
    }

    @Override
    public void multiSelectDisabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(mPrimaryDarkColor);
        }

        displayRecyclerView();
        if (gameDBAdapter.open().numRows() == 0) {
            gameDBAdapter.close();
            Intent home = new Intent(this, Home.class);
            startActivity(home);
        }
    }

    @Override
    public void onItemClicked(int position, final int gameID) {

        if (mHistoryActionMode != null) {
            toggleSelection(position, gameID);
        } else {

            if (!dataHelper.getGame(gameID, gameDBAdapter).ismCompleted()) {

                showAlertDialog(getString(R.string.carry_on), getString(R.string.continue_game_message), getString(R.string.carry_on), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                Intent intent = new Intent(History.this, GameActivity.class);
                                intent.putExtra("GAME_ID", gameID);
                                startActivity(intent);
                            }

                        }, getString(R.string.edit)
                        , new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(History.this, EditGame.class);
                                intent.putExtra("GAME_ID", gameID);
                                startActivity(intent);
                            }
                        }, getString(R.string.cancel), dismissDialogListener);

            } else {
                Intent intent = new Intent(this, EditGame.class);
                intent.putExtra("GAME_ID", gameID);
                startActivity(intent);
            }

        }
    }

    @Override
    public boolean onItemLongClicked(int position, int gameID) {
        if (mHistoryActionMode == null) {
            mHistoryActionMode = startSupportActionMode(new ActionBarCallback());
        }

        if (mHistoryActionMode != null) {
            mHistoryActionMode.setTitle(1 + " items selected");
        }

        multiSelectEnabled();
        toggleSelection(position, gameID);

        return true;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    public class ActionBarCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.action_mode, menu);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:

                    gameDBAdapter.open();
                    mHistoryAdapter.deleteSelectedGames(gameDBAdapter);
                    gameDBAdapter.close();

                    gamesDeleted();
                    mode.finish();
                    break;

                case R.id.action_delete_all:
                    gameDBAdapter.open();
                    gameDBAdapter.deleteAllGames();
                    gameDBAdapter.close();

                    startActivity(new Intent(getBaseContext(), Home.class));

                    mode.finish();
                    break;

                case R.id.action_select_all:

                    mHistoryAdapter.clearSelection();
                    mHistoryAdapter.selectAllItems();
                    updateActionModeCount();

                    break;

            }

            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mHistoryAdapter.clearSelection();
            multiSelectDisabled();
            HistoryAdapter.ACTION_MODE_DISABLED = true;
            mHistoryAdapter.notifyDataSetChanged();
            gamesDeleted();

            mHistoryActionMode = null;
        }
    }

}



