package io.github.sdsstudios.ScoreKeeper;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.api.GoogleApiClient;

public class History extends AppCompatActivity implements UpdateTabsListener, HistoryAdapter.ViewHolder.ClickListener {

    private String TAG = "History";

    private Intent mSettingsIntent;
    private Intent mAboutIntent;
    private ScoreDBAdapter mDbHelper;
    private RecyclerView mRecyclerView;
    private DataHelper mDataHelper;
    private MenuItem menuItemCompleted;
    private MenuItem menuItemUnfinished;
    private HistoryAdapter mHistoryAdapter;
    private ActionMode mHistoryActionMode = null;
    private int mPrimaryDarkColor;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    private GoogleApiClient mGoogleAPIClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int accentColor = sharedPreferences.getInt("prefAccentColor", R.style.DarkTheme);
        int primaryColor = sharedPreferences.getInt("prefPrimaryColor", getResources().getColor(R.color.primaryIndigo));
        mPrimaryDarkColor = sharedPreferences.getInt("prefPrimaryDarkColor", getResources().getColor(R.color.primaryIndigoDark));
        boolean colorNavBar = sharedPreferences.getBoolean("prefColorNavBar", false);
        if (colorNavBar) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setNavigationBarColor(mPrimaryDarkColor);
            }
        }

        setTheme(accentColor);
        setContentView(R.layout.activity_history);
        AdView mAdView = (AdView) findViewById(R.id.adViewHome);
        AdCreator adCreator = new AdCreator(mAdView, this);
        adCreator.createAd();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(mPrimaryDarkColor);
        }
        getSupportActionBar();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(primaryColor);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDbHelper = new ScoreDBAdapter(this);
        mDataHelper = new DataHelper();
        mAboutIntent = new Intent(this, About.class);
        mSettingsIntent = new Intent(this, Settings.class);
        mRecyclerView = (RecyclerView) findViewById(R.id.historyRecyclerView);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
        mDbHelper.open();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mDbHelper.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(mSettingsIntent);
            return true;
        }
        if (id == R.id.action_about) {
            startActivity(mAboutIntent);
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
        mDbHelper.open();

        try {
            if (mDbHelper.numRows() != 0) {
                int type = HistoryAdapter.BOTH;

                if (menuItemCompleted.isChecked()) {
                    type = HistoryAdapter.COMPLETED;
                }

                if (menuItemUnfinished.isChecked()) {
                    type = HistoryAdapter.UNFINISHED;
                }

                if (menuItemCompleted.isChecked() && menuItemUnfinished.isChecked()) {
                    type = HistoryAdapter.BOTH;
                }

                RecyclerView.LayoutManager mLayoutManager;
                mLayoutManager = new LinearLayoutManager(this);
                mRecyclerView.setLayoutManager(mLayoutManager);

                HistoryAdapter historyAdapter = new HistoryAdapter(HistoryModel.getHistoryModelList(mDbHelper, this, Pointers.HISTORY)
                        , this, this, Pointers.HISTORY, type);

                mRecyclerView.setAdapter(historyAdapter);
            } else {
                mRecyclerView.setVisibility(View.INVISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
            Toast.makeText(this, "Error opening History", Toast.LENGTH_SHORT).show();

        }

        mDbHelper.close();
    }


    @Override
    public void gamesDeleted() {
        displayRecyclerView();

    }

    @Override
    public void multiSelectEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }

    }

    private void toggleSelection(int position, int gameID) {
        mHistoryAdapter.toggleSelection(position, gameID);
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

    @Override
    public void multiSelectDisabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(mPrimaryDarkColor);
        }

        displayRecyclerView();
        if (mDbHelper.open().numRows() == 0) {
            mDbHelper.close();
            Intent home = new Intent(this, Home.class);
            startActivity(home);
        }
    }

    @Override
    public void onItemClicked(int position, final int gameID) {

        if (mHistoryActionMode != null) {
            toggleSelection(position, gameID);
        } else {

            if (!mDataHelper.getGame(gameID, mDbHelper).ismCompleted()) {

                AlertDialog dialog;
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle(R.string.carry_on);

                builder.setMessage(R.string.continue_game_message);

                builder.setNeutralButton(R.string.edit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(History.this, EditGame.class);
                        intent.putExtra("GAME_ID", gameID);
                        startActivity(intent);
                    }
                });

                builder.setPositiveButton(R.string.carry_on, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Intent intent = new Intent(History.this, GameActivity.class);
                        intent.putExtra("GAME_ID", gameID);
                        startActivity(intent);
                    }


                });

                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

                dialog = builder.create();
                dialog.show();
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
                    mDbHelper.open();
                    mHistoryAdapter.deleteSelectedGames(mDbHelper);
                    mDbHelper.close();

                    gamesDeleted();
                    mode.finish();
                    break;

                case R.id.action_delete_all:
                    mDbHelper.open();
                    mDbHelper.deleteAllGames();
                    mDbHelper.close();

                    startActivity(new Intent(getBaseContext(), Home.class));

                    mode.finish();
                    break;

                case R.id.action_select_all:

                    for (int i = 0; i < mHistoryAdapter.getItemCount(); i++) {
                        if (mHistoryAdapter.isSelected(i)) {
                            mHistoryAdapter.toggleSelection(i, i);
                        }
                    }

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



