package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;

public class History extends AppCompatActivity implements UpdateTabsListener, HistoryAdapter.ViewHolder.ClickListener{

    private Intent newGameIntent;
    private Intent settingsIntent;
    private Intent aboutIntent;
    private RecyclerView.Adapter adapter;
    private ScoreDBAdapter dbHelper;
    private RelativeLayout relativeLayout;
    private FirebaseAnalytics mFirebaseAnalytics;
    private RecyclerView recyclerView;
    private DataHelper dataHelper;
    private MenuItem settingsMenuItem, menuItemCompleted, menuItemUnfinished;
    private HistoryAdapter historyAdapter;
    private static ArrayList<GameModel> gameModel;
    private ActionMode actionMode = null;
    private int primaryDarkColor;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
        int accentColor = sharedPreferences.getInt("prefAccent", R.style.AppTheme);
        int primaryColor = sharedPreferences.getInt("prefPrimaryColor", getResources().getColor(R.color.primaryIndigo));
        primaryDarkColor = sharedPreferences.getInt("prefPrimaryDarkColor", getResources().getColor(R.color.primaryIndigoDark));
        boolean colorNavBar = sharedPreferences.getBoolean("prefColorNavBar", false);
        if (colorNavBar){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setNavigationBarColor(primaryDarkColor);
            }
        }
        setTheme(accentColor);
        setContentView(R.layout.activity_history);
        AdView mAdView = (AdView) findViewById(R.id.adViewHome);
        AdCreator adCreator = new AdCreator(mAdView, this);
        adCreator.createAd();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(primaryDarkColor);
        }
        getSupportActionBar();
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(primaryColor);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dbHelper = new ScoreDBAdapter(this).open();
        dataHelper = new DataHelper();
        newGameIntent = new Intent(this, NewGame.class);
        aboutIntent = new Intent(this, About.class);
        settingsIntent = new Intent(this, Settings.class);
        relativeLayout = (RelativeLayout) findViewById(R.id.historyLayout);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        recyclerView = (RecyclerView)findViewById(R.id.historyRecyclerView);


        dbHelper.close();

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


        settingsMenuItem = menu.findItem(R.id.action_settings);
        settingsMenuItem.setVisible(false);

        displayRecyclerView();


        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        dbHelper.open();

    }

    @Override
    protected void onPause() {
        super.onPause();
        dbHelper.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(settingsIntent);
            return true;
        }if (id == R.id.action_about) {
            startActivity(aboutIntent);
            return true;
        }if (id == R.id.action_unfinished){
            if (!menuItemCompleted.isChecked()){
                Toast.makeText(this, "One must be checked at all times", Toast.LENGTH_SHORT).show();
            }else {
                menuItemUnfinished.setChecked(!menuItemUnfinished.isChecked());
                displayRecyclerView();
            }

        }if (id == R.id.action_completed){

            if (!menuItemUnfinished.isChecked()){
                Toast.makeText(this, "One must be checked at all times", Toast.LENGTH_SHORT).show();
            }else {
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

    public void displayRecyclerView(){
        dbHelper.open();
        try {
            if (dbHelper.numRows() != 0) {
                int type = 3;

                if (menuItemCompleted.isChecked()) {
                    type = 2;
                }
                if (menuItemUnfinished.isChecked()) {
                    type = 1;
                }

                if (menuItemCompleted.isChecked() && menuItemUnfinished.isChecked()) {
                    type = 3;
                }

                RecyclerView.LayoutManager mLayoutManager;
                mLayoutManager = new LinearLayoutManager(this);
                recyclerView.setLayoutManager(mLayoutManager);
                GameModel gModel = new GameModel();
                gameModel = gModel.createGameModel(dbHelper.numRows(), type, this, dbHelper);
                historyAdapter = new HistoryAdapter(gameModel, this, this, false);
                recyclerView.setAdapter(historyAdapter);
            } else {
                recyclerView.setVisibility(View.INVISIBLE);
            }
        }catch (Exception ignore){

        }
        dbHelper.close();
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
        historyAdapter.toggleSelection(position, gameID);
        int count = historyAdapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {

            try {
                actionMode.invalidate();
                actionMode.setTitle(count + " items selected");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void multiSelectDisabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(primaryDarkColor);
        }

        displayRecyclerView();
        if (dbHelper.open().numRows() == 0){
            dbHelper.close();
            Intent home = new Intent(this, Home.class);
            startActivity(home);
        }
    }

    @Override
    public void onItemClicked(int position, final int gameID) {

        if (actionMode != null) {
            toggleSelection(position, gameID);
        }else{

            if (dataHelper.getCompletedById(gameID, dbHelper) == 0) {

                AlertDialog dialog;
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle(R.string.carry_on);

                builder.setMessage(R.string.continue_game_message);

                builder.setNeutralButton(R.string.edit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(History.this, EditGame.class);
                        intent.putExtra("gameID", gameID);
                        startActivity(intent);
                    }
                });

                builder.setPositiveButton(R.string.carry_on, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Intent intent = new Intent(History.this, MainActivity.class);
                        intent.putExtra("gameID", gameID);
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
            }else{
                Intent intent = new Intent(this, EditGame.class);
                intent.putExtra("gameID", gameID);
                startActivity(intent);
            }

        }
    }

    @Override
    public boolean onItemLongClicked(int position, int gameID) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(new History.ActionBarCallback());
        }

        if (actionMode != null) {
            actionMode.setTitle(1 + " items selected");
        }

        multiSelectEnabled();
        toggleSelection(position, gameID);

        return true;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */

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
                    dbHelper.open();
                    historyAdapter.deleteSelectedGames(dbHelper);
                    dbHelper.close();

                    gamesDeleted();
                    mode.finish();

                    return true;

                default:

                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            historyAdapter.clearSelection();
            multiSelectDisabled();
            HistoryAdapter.actionModeDisabled = true;
            historyAdapter.notifyDataSetChanged();
            gamesDeleted();

            actionMode = null;
        }
    }

}



