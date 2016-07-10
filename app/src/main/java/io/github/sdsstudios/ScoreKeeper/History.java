package io.github.sdsstudios.ScoreKeeper;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.RelativeLayout;

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

    private MenuItem settingsMenuItem, menuItemCompleted, menuItemUnfinished;
    private Toolbar toolbar;
    private HistoryAdapter historyAdapter;
    private static ArrayList<GameModel> gameModel;
    private GameModel gModel;
    private ActionMode actionMode = null;
    private int typeChecked = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dbHelper = new ScoreDBAdapter(this).open();
        newGameIntent = new Intent(this, NewGame.class);
        aboutIntent = new Intent(this, About.class);
        settingsIntent = new Intent(this, Settings.class);
        relativeLayout = (RelativeLayout) findViewById(R.id.historyLayout);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        recyclerView = (RecyclerView)findViewById(R.id.historyRecyclerView);

        displayRecyclerView(typeChecked);

        dbHelper.close();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        menuItemUnfinished = menu.findItem(R.id.action_unfinished);
        menuItemUnfinished.setVisible(true);

        menuItemCompleted = menu.findItem(R.id.action_completed);
        menuItemCompleted.setVisible(true);

        settingsMenuItem = menu.findItem(R.id.action_settings);
        settingsMenuItem.setVisible(false);

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
            typeChecked = 1;
            displayRecyclerView(typeChecked);
            menuItemUnfinished.setChecked(true);


        }if (id == R.id.action_completed){
            typeChecked = 2;

            displayRecyclerView(typeChecked);
            menuItemCompleted.setChecked(true);


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    public void displayRecyclerView(int type){
        dbHelper.open();
        if (dbHelper.numRows() != 0) {

            RecyclerView.LayoutManager mLayoutManager;
            mLayoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(mLayoutManager);
            gameModel = GameModel.createGameModel(type, this, dbHelper);
            historyAdapter = new HistoryAdapter(gameModel, this, this);
            recyclerView.setAdapter(historyAdapter);
        }
        dbHelper.close();
    }

    @Override
    public void gamesDeleted() {

    }

    @Override
    public void multiSelectEnabled() {
        AppBarLayout.LayoutParams layoutParams = new AppBarLayout.LayoutParams(0,0);
        toolbar.setLayoutParams(layoutParams);

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
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void multiSelectDisabled() {
        AppBarLayout.LayoutParams layoutParams = new AppBarLayout.LayoutParams(AppBarLayout.LayoutParams.MATCH_PARENT, AppBarLayout.LayoutParams.WRAP_CONTENT);
        toolbar.setLayoutParams(layoutParams);
    }

    @Override
    public void onItemClicked(int position, int gameID) {

        if (actionMode != null) {
            toggleSelection(position, gameID);
            actionMode.setTitle(historyAdapter.getSelectedItemCount() + " items selected");
        }else{
            Intent intent = new Intent(this, EditGame.class);
            intent.putExtra("gameID", gameID);
            startActivity(intent);
        }

    }

    @Override
    public boolean onItemLongClicked(int position, int gameID) {
        if (actionMode == null) {
            actionMode = ((AppCompatActivity) this).startSupportActionMode(new History.ActionBarCallback());
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
            mode.getMenuInflater().inflate (R.menu.action_mode, menu);

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
                    Log.e("actionbarcallback", "menu_remove");
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


            actionMode = null;
        }
    }

}



