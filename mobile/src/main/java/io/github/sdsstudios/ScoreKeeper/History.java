package io.github.sdsstudios.ScoreKeeper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class History extends AppCompatActivity {

    public static RelativeLayout relativeLayout;
    RecyclerView recyclerViewHistory;
    CursorHelper cursorHelper;
    Intent homeIntent;
    Intent settingsIntent;
    int numGames;
    private TextView textViewNoGames;
    private RecyclerView.Adapter historyAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ScoreDBAdapter dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dbHelper = new ScoreDBAdapter(this);
        dbHelper.open();

        cursorHelper = new CursorHelper();

        homeIntent = new Intent(this, Home.class);
        settingsIntent = new Intent(this, Settings.class);

        relativeLayout = (RelativeLayout)findViewById(R.id.historyLayout);

        recyclerViewHistory = (RecyclerView)findViewById(R.id.historyList);
        textViewNoGames = (TextView)findViewById(R.id.historyNoGames);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        recyclerViewHistory.setLayoutManager(mLayoutManager);

        try {
            numGames = Integer.valueOf(dbHelper.getNewestGame());
            ArrayList<GameModel> gameModel = GameModel.createGameModel(numGames, dbHelper, 1);
            historyAdapter = new HistoryAdapter(gameModel, dbHelper, this, relativeLayout, 1, numGames);
            recyclerViewHistory.setAdapter(historyAdapter);
        } catch (Exception e) {
            textViewNoGames.setText(R.string.no_games);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(homeIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
        }

        return super.onOptionsItemSelected(item);
    }

}
