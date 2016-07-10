package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;

public class Home extends AppCompatActivity{

    private Intent newGameIntent;
    private Intent settingsIntent;
    private Intent aboutIntent;
    private Intent historyIntent;
    private ScoreDBAdapter dbHelper;
    private FirebaseAnalytics mFirebaseAnalytics;
    private TextView textViewNumGames;
    private RecyclerView recyclerView;
    private MenuItem settingsMenuItem, historyMenuItem;
    private Toolbar toolbar;
    private HistoryAdapter historyAdapter;
    private static ArrayList<GameModel> gameModel;
    private SharedPreferences sharedPreferences;
    private int numGamesToShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHelper = new ScoreDBAdapter(this).open();
        newGameIntent = new Intent(this, NewGame.class);
        aboutIntent = new Intent(this, About.class);
        settingsIntent = new Intent(this, Settings.class);
        historyIntent = new Intent(this, History.class);

        recyclerView = (RecyclerView)findViewById(R.id.homeRecyclerView);

        textViewNumGames = (TextView)findViewById(R.id.textViewNumGamesPlayed);
        textViewNumGames.setText(String.valueOf(dbHelper.numRows()));

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabNewGame);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(newGameIntent);
            }
        });

        sharedPreferences = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
        numGamesToShow = Integer.valueOf(sharedPreferences.getString("numgamestoshow", "3"));

        displayRecyclerView();

    }

    public void displayRecyclerView(){
        dbHelper.open();

        if (dbHelper.numRows() != 0) {

            RecyclerView.LayoutManager mLayoutManager;
            mLayoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(mLayoutManager);
            gameModel = GameModel.createGameModel(numGamesToShow, 1, this, dbHelper);
            historyAdapter = new HistoryAdapter(gameModel, this, null);
            recyclerView.setAdapter(historyAdapter);
        }else{
            recyclerView.setVisibility(View.INVISIBLE);
        }
        dbHelper.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        settingsMenuItem = menu.findItem(R.id.action_settings);
        historyMenuItem = menu.findItem(R.id.action_history);
        settingsMenuItem.setVisible(true);
        historyMenuItem.setVisible(true);

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
        }if (id == R.id.action_history) {
            startActivity(historyIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

}



