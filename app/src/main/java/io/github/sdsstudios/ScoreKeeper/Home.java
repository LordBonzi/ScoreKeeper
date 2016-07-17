package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;

public class Home extends AppCompatActivity implements HistoryAdapter.ViewHolder.ClickListener{

    private Intent newGameIntent;
    private Intent settingsIntent;
    private Intent aboutIntent;
    private Intent historyIntent;
    private Button buttonMore, buttonLastGame;
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
    private RelativeLayout relativeLayoutRecent;
    private String TAG = "Home";
    private int lastPlayedGame;
    private int accentColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
        accentColor = sharedPreferences.getInt("prefAccent", R.style.AppTheme);
        int primaryColor = sharedPreferences.getInt("prefPrimaryColor", getResources().getColor(R.color.primaryIndigo));
        int primaryDarkColor = sharedPreferences.getInt("prefPrimaryDarkColor", getResources().getColor(R.color.primaryIndigoDark));
        boolean colorNavBar = sharedPreferences.getBoolean("prefColorNavBar", false);
        if (colorNavBar){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setNavigationBarColor(primaryDarkColor);
            }
        }
        setTheme(accentColor);
        setContentView(R.layout.activity_home);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(primaryDarkColor);
        }
        getSupportActionBar();
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(primaryColor);
        setSupportActionBar(toolbar);
        dbHelper = new ScoreDBAdapter(this).open();
        numGamesToShow = Integer.valueOf(sharedPreferences.getString("numgamestoshow", "3"));
        lastPlayedGame = sharedPreferences.getInt("lastplayedgame", dbHelper.getNewestGame());
        newGameIntent = new Intent(this, NewGame.class);
        aboutIntent = new Intent(this, About.class);
        settingsIntent = new Intent(this, Settings.class);
        historyIntent = new Intent(this, History.class);
        relativeLayoutRecent = (RelativeLayout)findViewById(R.id.layoutRecentGames);
        buttonMore = (Button)findViewById(R.id.buttonMore);
        buttonMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(historyIntent);
            }
        });

        buttonLastGame = (Button)findViewById(R.id.buttonContinueLastGame);

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

        buttonLastGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, MainActivity.class);
                intent.putExtra("gameID", lastPlayedGame);
                startActivity(intent);
            }
        });

        if (dbHelper.numRows() == 0 ){
            relativeLayoutRecent.setVisibility(View.INVISIBLE);
            buttonMore.setVisibility(View.INVISIBLE);
            buttonLastGame.setVisibility(View.INVISIBLE);
        }

        displayRecyclerView();

    }

    public void displayRecyclerView(){
        dbHelper.open();

        try {
            if (dbHelper.numRows() != 0) {

                RecyclerView.LayoutManager mLayoutManager;
                mLayoutManager = new LinearLayoutManager(this);
                recyclerView.setLayoutManager(mLayoutManager);
                gameModel = GameModel.createGameModel(numGamesToShow, 1, this, dbHelper);
                if (gameModel.isEmpty()){
                    relativeLayoutRecent.setVisibility(View.INVISIBLE);
                    recyclerView.setVisibility(View.INVISIBLE);
                }else{
                    historyAdapter = new HistoryAdapter(gameModel, this, this, true);
                    recyclerView.setAdapter(historyAdapter);
                }

            } else {
                recyclerView.setVisibility(View.INVISIBLE);
            }
            dbHelper.close();
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, e.toString());

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        settingsMenuItem = menu.findItem(R.id.action_settings);
        settingsMenuItem.setVisible(true);

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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public void onItemClicked(int position, int gameID) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("gameID", gameID);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClicked(int position, int gameID) {
        return false;
    }
}



