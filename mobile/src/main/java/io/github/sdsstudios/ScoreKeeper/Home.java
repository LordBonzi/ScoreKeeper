package io.github.sdsstudios.ScoreKeeper;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Intent newGameIntent;
    private Intent historyIntent;
    private Intent settingsIntent;
    private Intent aboutIntent;
    private Intent editGameIntent;
    private RecyclerView recyclerViewRecent;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView.Adapter adapter;
    private ScoreDBAdapter dbHelper;
    private RelativeLayout relativeLayout;
    private CursorHelper cursorHelper;
    private TextView textViewNoGames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHelper = new ScoreDBAdapter(this).open();
        cursorHelper = new CursorHelper();

        newGameIntent = new Intent(this, NewGame.class);
        historyIntent = new Intent(this, History.class);
        aboutIntent = new Intent(this, About.class);
        settingsIntent = new Intent(this, Settings.class);
        editGameIntent = new Intent(this, EditGame.class);
        relativeLayout = (RelativeLayout) findViewById(R.id.historyLayout);
        recyclerViewRecent = (RecyclerView) findViewById(R.id.recyclerViewRecent);
        textViewNoGames = (TextView)findViewById(R.id.textViewHomeNoGamesHome);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(newGameIntent);
            }
        });


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mLayoutManager = new LinearLayoutManager(this);
        recyclerViewRecent.setLayoutManager(mLayoutManager);

        try {
            ArrayList<GameModel> gameModel = GameModel.createGameModel(recentNumGames(), dbHelper);
            adapter = new HistoryAdapter(gameModel, dbHelper, this, relativeLayout, 2);
            recyclerViewRecent.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
            textViewNoGames.setText(getResources().getString(R.string.no_games));
            Log.e("Home", String.valueOf(e));
        }

    }

    public Integer recentNumGames(){
        int numGames = 0;
        if (Integer.valueOf(dbHelper.getNewestGame()) == 1){
            numGames = 1;
        }else if (Integer.valueOf(dbHelper.getNewestGame()) == 2){
            numGames = 2;
        }else if (Integer.valueOf(dbHelper.getNewestGame()) >= 3){
            numGames = 3;
        }

        return numGames;
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
            startActivity(historyIntent);

        } else if (id == R.id.nav_settings) {
            startActivity(settingsIntent);

        } else if (id == R.id.nav_about) {
            startActivity(aboutIntent);
        }else if (id == R.id.nav_edit_game) {
            startActivity(editGameIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
