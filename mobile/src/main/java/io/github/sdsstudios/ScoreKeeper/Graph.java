package io.github.sdsstudios.ScoreKeeper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.crash.FirebaseCrash;

import java.util.ArrayList;

/**
 * Created by Seth Schroeder on 31/05/2016.
 */

public class Graph extends AppCompatActivity{
    private ArrayList arrayListPlayers, arrayListScores;
    private ArrayList<String> labels = new ArrayList<String>();
    private ArrayList entries = new ArrayList<>();
    private int gameID;
    private ScoreDBAdapter dbHelper;
    private CursorHelper cursorHelper;
    private LineChart lineChart;
    private Intent editGameIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
                FirebaseCrash.report(new Exception(e.toString()));

            }
        });

        editGameIntent = new Intent(this, EditGame.class);

        lineChart = (LineChart)findViewById(R.id.chart);

        dbHelper = new ScoreDBAdapter(this).open();
        cursorHelper = new CursorHelper();

        Bundle extras = getIntent().getExtras();
        gameID = extras.getInt("gameID");

        arrayListPlayers = cursorHelper.getArrayById(ScoreDBAdapter.KEY_PLAYERS, gameID,dbHelper);
        arrayListScores = cursorHelper.getArrayById(ScoreDBAdapter.KEY_SCORE, gameID,dbHelper);

        entries.add(new Entry(4f, 0));
        entries.add(new Entry(8f, 1));

        LineDataSet dataset = new LineDataSet(entries, "# of Calls");


        LineData data = new LineData(arrayListPlayers, dataset);
        lineChart.setData(data); // set the data and list of lables into chart

    }

    @Override
    public void onBackPressed() {
        editGameIntent.putExtra("gameID", gameID);
        startActivity(editGameIntent);
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
        if (id == android.R.id.home) {
            onBackPressed();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
