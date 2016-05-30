package io.github.sdsstudios.ScoreKeeper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener, View.OnLongClickListener{

    public static int gameID;
    public static Button buttonP1;
    public static Button buttonP2;
    public static boolean firstTime = true;
    TextView textViewP1;
    TextView textViewP2;
    RecyclerView bigGameList;
    String TAG = "MainActivity.class";
    int gameSize;
    RelativeLayout normal, big;
    ArrayList playersArray;
    ArrayList scoresArray;
    CursorHelper cursorHelper;
    SmallLayout smallLayout;
    Intent homeIntent;
    ScoreDBAdapter dbHelper;
    private RecyclerView.Adapter bigGameAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        homeIntent = new Intent(this, Home.class);

        buttonP1 = (Button) findViewById(R.id.buttonP1);
        buttonP1.setOnClickListener(this);
        buttonP1.setOnLongClickListener(this);

        buttonP2 = (Button) findViewById(R.id.buttonP2);
        buttonP2.setOnClickListener(this);
        buttonP2.setOnLongClickListener(this);

        textViewP1 = (TextView)findViewById(R.id.textViewP1);
        textViewP2 = (TextView)findViewById(R.id.textViewP2);

        bigGameList = (RecyclerView)findViewById(R.id.bigGameList);

        normal = (RelativeLayout)findViewById(R.id.layoutNormal);
        big = (RelativeLayout)findViewById(R.id.layoutBig);

        cursorHelper = new CursorHelper();

        dbHelper = new ScoreDBAdapter(this);
        dbHelper.open();

        smallLayout = new SmallLayout();

        gameID = Integer.valueOf(dbHelper.getNewestGame());

        playersArray = new ArrayList();
        playersArray = cursorHelper.getArrayById(ScoreDBAdapter.KEY_PLAYERS, gameID, dbHelper);

        scoresArray = new ArrayList();
        scoresArray = cursorHelper.getArrayById(ScoreDBAdapter.KEY_SCORE, gameID, dbHelper);

        gameSize = playersArray.size();


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

        if (gameSize > 2) {
            big.setVisibility(View.VISIBLE);
            mLayoutManager = new LinearLayoutManager(this);
            bigGameList.setLayoutManager(mLayoutManager);

            ArrayList<BigGameModel> bigGameModels = BigGameModel.createGameModel(playersArray.size(), playersArray,  scoresArray,  dbHelper);

            bigGameAdapter = new BigGameAdapter(bigGameModels, scoresArray, dbHelper, gameID);
            bigGameList.setAdapter(bigGameAdapter);
        }else{
            normal.setVisibility(View.VISIBLE);
            smallLayout.onCreate(buttonP1,  buttonP2, dbHelper, gameID);

        }

        textViewP1.setText(String.valueOf(playersArray.get(0)));
        textViewP2.setText(String.valueOf(playersArray.get(1)));

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

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onLongClick(View v) {

        switch (v.getId()) {
            case R.id.buttonP1:
                smallLayout.onLongClick(buttonP1, dbHelper, gameID);
                break;

            case R.id.buttonP2:
                smallLayout.onLongClick(buttonP2, dbHelper, gameID);

                break;
        }

        return true;
    }
}

class SmallLayout extends Activity{
    public static Integer P1Score =0 , P2Score =0;
    ArrayList scoresArray;
    boolean ft1;
    boolean ft2;


    public void onCreate(Button b1, Button b2, ScoreDBAdapter dbHelper, int id){
        scoresArray = new ArrayList();
        P1Score = 0;
        P2Score = 0;
        ft1 = MainActivity.firstTime;
        ft2 = MainActivity.firstTime;
        scoresArray.add(0, String.valueOf(P1Score));
        scoresArray.add(1, String.valueOf(P2Score));
        updateScores(dbHelper, id);

        b1.setText(String.valueOf(P1Score));
        b2.setText(String.valueOf(P2Score));
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
    public void onLongClick(Button button, ScoreDBAdapter dbHelper, int id){

        if (button == MainActivity.buttonP1 && !ft1 && P1Score != 0){
            P1Score -= 1;
            button.setText(String.valueOf(P1Score));
        }else if (button == MainActivity.buttonP2 && !ft2 && P2Score != 0){
            P2Score -= 1;
            button.setText(String.valueOf(P2Score));
        }

        if (button == MainActivity.buttonP1){
            ft1 = false;
        }else {
            ft2 = false;

        }

        updateScores(dbHelper, id);
    }

    public void updateScores(ScoreDBAdapter dbHelper, int id){
        scoresArray.set(0, String.valueOf(P1Score));
        scoresArray.set(1, String.valueOf(P2Score));

        dbHelper.updateGame(scoresArray, null, ScoreDBAdapter.KEY_SCORE, id);
    }

}
