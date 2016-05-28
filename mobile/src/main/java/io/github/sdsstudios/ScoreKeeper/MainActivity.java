package io.github.sdsstudios.ScoreKeeper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    public static int gameID;
    public static Button buttonP1;
    public static Button buttonP2;
    TextView textViewP1;
    TextView textViewP2;
    String TAG = "MainActivity.class";
    int gameSize;
    RelativeLayout normal, big;
    ArrayList playersArray;
    CursorHelper cursorHelper;
    SmallLayout smallLayout;
    BigLayout bigLayout;

    Intent historyIntent;
    Intent settingsIntent;
    Intent aboutIntent;
    Intent homeIntent;

    private ScoreDBAdapter dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.e("MainActivity", "Started mainactivity");

        historyIntent = new Intent(this, History.class);
        settingsIntent = new Intent(this, Settings.class);
        aboutIntent = new Intent(this, Settings.class);
        homeIntent = new Intent(this, Home.class);

        buttonP1 = (Button) findViewById(R.id.buttonP1);
        buttonP1.setOnClickListener(this);

        buttonP2 = (Button) findViewById(R.id.buttonP2);
        buttonP2.setOnClickListener(this);

        textViewP1 = (TextView)findViewById(R.id.textViewP1);
        textViewP2 = (TextView)findViewById(R.id.textViewP2);

        normal = (RelativeLayout)findViewById(R.id.layoutNormal);
        big = (RelativeLayout)findViewById(R.id.layoutBig);

        cursorHelper = new CursorHelper();

        dbHelper = new ScoreDBAdapter(this);
        dbHelper.open();

        smallLayout = new SmallLayout();
        bigLayout = new BigLayout();

        gameID = Integer.valueOf(dbHelper.getNewestGame());

        playersArray = new ArrayList();
        playersArray = cursorHelper.getArrayById(ScoreDBAdapter.KEY_PLAYERS, gameID, dbHelper);

        gameSize = playersArray.size();

        smallLayout.onCreate(buttonP1,  buttonP2, dbHelper, gameID);

        if (gameSize > 2) {
            big.setVisibility(View.VISIBLE);
        }else{
            normal.setVisibility(View.VISIBLE);
        }

        textViewP1.setText(String.valueOf(playersArray.get(0)));
        textViewP2.setText(String.valueOf(playersArray.get(1)));

        //navigation drawer stuff
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Shared Preferences stuff
        final String PREFS_NAME = "scorekeeper";

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        if (settings.getBoolean("my_first_time", true)) {

            saveInfo();
            settings.edit().putBoolean("my_first_time", false).commit();
        }else {
            SharedPreferences sharedPref = getSharedPreferences("scorekeeper"
                    , Context.MODE_PRIVATE);

        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonP1:
                smallLayout.onClick(buttonP1, dbHelper, gameID);
                break;

            case R.id.buttonP2:
                smallLayout.onClick(buttonP2, dbHelper, gameID);

                break;
        }
    }

    public void saveInfo(){
        SharedPreferences sharedPref = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();


        editor.apply();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

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

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_history) {
            startActivity(historyIntent);

        } else if (id == R.id.nav_settings) {
            startActivity(settingsIntent);


        } else if (id == R.id.nav_about) {
            startActivity(aboutIntent);

        } else if (id == R.id.nav_home){
            startActivity(homeIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}



class BigLayout extends  Activity{

    public void onCreate(){
    }

    public void game(Button button){



    }
}

class SmallLayout extends Activity{
    public static Integer P1Score =0 , P2Score =0;
    ArrayList scoresArray;


    public void onCreate(Button b1, Button b2, ScoreDBAdapter dbHelper, int id){
        scoresArray = new ArrayList();
        P1Score = 0;
        P2Score = 0;
        scoresArray.add(0, String.valueOf(P1Score));
        scoresArray.add(1, String.valueOf(P2Score));
        updateScores(dbHelper, id);

        b1.setText(String.valueOf(P1Score));
        b2.setText(String.valueOf(P2Score));
    }

    public void game(Button button, Integer score, ScoreDBAdapter dbHelper, int id){
        score += 1;
        button.setText(String.valueOf(score));
        updateScores(dbHelper, id);
    }

    public void onClick(Button button, ScoreDBAdapter dbHelper, int id){

        if (button == MainActivity.buttonP1){
            P1Score += 1;
            button.setText(String.valueOf(P1Score));
        }else {
            P2Score += 1;
            button.setText(String.valueOf(P2Score));
        }

        updateScores(dbHelper, id);
    }

    public void updateScores(ScoreDBAdapter dbHelper, int id){
        scoresArray.set(0, String.valueOf(P1Score));
        scoresArray.set(1, String.valueOf(P2Score));

        dbHelper.updateGame(scoresArray, null, ScoreDBAdapter.KEY_SCORE, id);
    }

}
