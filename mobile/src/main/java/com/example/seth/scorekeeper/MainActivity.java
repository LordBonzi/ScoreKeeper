package com.example.seth.scorekeeper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    String TAG = "MainActivity.class";
    Button buttonP1, buttonP2;
    TextView textViewP1, textViewP2, tv;
    int P1Score, P2Score;
    int amountItems, gameID, gameSize;
    RelativeLayout normal, big;
    ArrayList players;

    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ArrayList<String> scoreDataSet;
    private ScoreDBAdapter dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Log.e("MainActivity", "Started mainactivity");

        tv = (TextView) findViewById(R.id.textView4);
        textViewP1 = (TextView) findViewById(R.id.textViewP1);
        textViewP2 = (TextView) findViewById(R.id.textViewP2);
        buttonP1 = (Button) findViewById(R.id.buttonP1);
        buttonP2 = (Button) findViewById(R.id.buttonP2);
        dbHelper = new ScoreDBAdapter(this);
        dbHelper.open();

        gameID = Integer.valueOf(getID());

        gameSize = getDBCursorArray(ScoreDBAdapter.KEY_PLAYERS).size();

        players = new ArrayList();
        players = getDBCursorArray(ScoreDBAdapter.KEY_PLAYERS);

        Log.i(TAG, String.valueOf(getDBCursorString(ScoreDBAdapter.KEY_PLAYERS)));

        Log.i(TAG, "game size = " + getDBCursorArray(ScoreDBAdapter.KEY_PLAYERS).size());
        normal = (RelativeLayout)findViewById(R.id.layoutNormal);
        big = (RelativeLayout)findViewById(R.id.layoutBig);

        if (gameSize > 2) {
            big.setVisibility(View.VISIBLE);
        }else{
            normal.setVisibility(View.VISIBLE);

        }



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        }

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
            //the app is being launched for first time, do something
            Log.d("Comments", "First time");

            saveInfo();
            settings.edit().putBoolean("my_first_time", false).commit();
        }else {
            SharedPreferences sharedPref = getSharedPreferences("TTscorekeeper"
                    , Context.MODE_PRIVATE);

        }

        tv.setText(gameID + " , " +
                gameSize + " , " +
                players

        );

    }

    public String getID() {

        int index = dbHelper.getNewestGame(ScoreDBAdapter.KEY_ROWID).getColumnIndex(ScoreDBAdapter.KEY_ROWID);
        String value = dbHelper.getNewestGame(ScoreDBAdapter.KEY_ROWID).getString(index);
        return value;
    }


    public String convertToString(ArrayList arrayList) {

        arrayList = new ArrayList<>();
        String str = TextUtils.join(",", arrayList);
        Log.i(TAG, str);

        return str;
    }

    public ArrayList convertToArray(String string) {

        String[] strValues = string.split(",");
        ArrayList array = new ArrayList<String>(Arrays.asList(strValues));

        return array;
    }


    public ArrayList getDBCursorArray(String request) {

        int index = dbHelper.getNewestGame(request).getColumnIndex(request);
        String value = dbHelper.getNewestGame(request).getString(index);
        textViewP1.setText(value);

        return convertToArray(value);

    }

    public String getDBCursorString(String request) {

        int index = dbHelper.fetchGamesById(gameID).getColumnIndex(request);
        String value = dbHelper.fetchAllGames().getString(index);
        return value;
    }

    public void saveInfo(){
        SharedPreferences sharedPref = getSharedPreferences("TTscorekeeper", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putInt("p1score", P1Score);
        editor.putInt("p2score", P2Score);
        editor.putInt("amountitems", amountItems);

        editor.apply();
    }

    @Override
    public void onBackPressed() {
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
            Intent intent = new Intent(this, History.class);
            startActivity(intent);

        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
