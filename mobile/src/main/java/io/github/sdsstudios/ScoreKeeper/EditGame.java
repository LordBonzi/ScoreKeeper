package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class EditGame extends AppCompatActivity {
    ArrayList arrayListPlayers, arrayListScores;
    String date;
    int gameID;
    private TextView textViewP1, textViewP2, textViewScoreP1, textViewScoreP2, textViewDate;
    private RelativeLayout relativeLayout;
    private RecyclerView recyclerView;
    private ScoreDBAdapter dbHelper;
    private CursorHelper cursorHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_game);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("custom title");

        Bundle extras = getIntent().getExtras();
        gameID = extras.getInt("gameID");

        dbHelper = new ScoreDBAdapter(this);
        dbHelper.open();

        cursorHelper = new CursorHelper();

        textViewP1 = (TextView)findViewById(R.id.textViewEditGameP1);
        textViewP2 = (TextView)findViewById(R.id.textViewEditGameP2);
        textViewScoreP1 = (TextView)findViewById(R.id.textViewEditGameScore1);
        textViewScoreP2 = (TextView)findViewById(R.id.textViewEditGameScore2);
        textViewDate = (TextView)findViewById(R.id.textViewEditGameDate);

        relativeLayout = (RelativeLayout) findViewById(R.id.layoutEditGame);
        recyclerView = (RecyclerView)findViewById(R.id.recyclerViewEditGame);

        arrayListPlayers = cursorHelper.getArrayById(ScoreDBAdapter.KEY_PLAYERS, gameID, dbHelper);
        arrayListScores = cursorHelper.getArrayById(ScoreDBAdapter.KEY_SCORE, gameID, dbHelper);

        if (arrayListPlayers.size() > 2){
            recyclerView.setVisibility(View.VISIBLE);
        }else{
            relativeLayout.setVisibility(View.VISIBLE);
            textViewP1.setText(String.valueOf(arrayListPlayers.get(0)));
            textViewP2.setText(String.valueOf(arrayListPlayers.get(1)));
            textViewScoreP1.setText(String.valueOf(arrayListScores.get(0)));
            textViewScoreP2.setText(String.valueOf(arrayListScores.get(1)));

        }

        date = cursorHelper.getTimeById(gameID, dbHelper);
        textViewDate.setText(date);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

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

    public void saveInfo(){
        SharedPreferences sharedPref = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("gameID", gameID = 0);


        editor.apply();
    }
}
