package io.github.sdsstudios.ScoreKeeper;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
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
import android.widget.Toast;

import com.google.android.gms.ads.AdView;

import java.io.File;
import java.util.ArrayList;

public class Home extends AppCompatActivity implements HistoryAdapter.ViewHolder.ClickListener{

    private Intent newGameIntent;
    private Intent settingsIntent;
    private Intent aboutIntent;
    private Intent historyIntent;
    private ScoreDBAdapter dbHelper;
    private RecyclerView recyclerView;
    private MenuItem historyMenuItem;
    private SharedPreferences sharedPreferences;
    private int numGamesToShow;
    private RelativeLayout relativeLayoutRecent;
    private String TAG = "Home";
    private int lastPlayedGame;
    private boolean reviewLaterBool;
    private DataHelper dataHelper = new DataHelper();
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
        int accentColor = sharedPreferences.getInt("prefAccent", R.style.AppTheme);
        int primaryColor = sharedPreferences.getInt("prefPrimaryColor", getResources().getColor(R.color.primaryIndigo));
        int primaryDarkColor = sharedPreferences.getInt("prefPrimaryDarkColor", getResources().getColor(R.color.primaryIndigoDark));
        boolean colorNavBar = sharedPreferences.getBoolean("prefColorNavBar", false);
        reviewLaterBool = sharedPreferences.getBoolean("reviewlater", true);

        if (colorNavBar){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setNavigationBarColor(primaryDarkColor);
            }
        }

        setTheme(accentColor);
        setContentView(R.layout.activity_home);
        AdView mAdView = (AdView) findViewById(R.id.adViewHome);
        AdCreator adCreator = new AdCreator(mAdView, this);
        adCreator.createAd();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(primaryDarkColor);
        }

        getSupportActionBar();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
        Button buttonMore = (Button) findViewById(R.id.buttonMore);
        buttonMore.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startActivity(historyIntent);
            }
        });

        Button buttonLastGame = (Button) findViewById(R.id.buttonContinueLastGame);

        recyclerView = (RecyclerView)findViewById(R.id.homeRecyclerView);

        TextView textViewNumGames = (TextView) findViewById(R.id.textViewNumGamesPlayed);
        textViewNumGames.setText(String.valueOf(dbHelper.numRows()));

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

        if (dbHelper.numRows() == 0){
            relativeLayoutRecent.setVisibility(View.INVISIBLE);
            buttonMore.setVisibility(View.INVISIBLE);
            buttonLastGame.setVisibility(View.INVISIBLE);
        }
        if (!anyUnfinishedGames()){
            relativeLayoutRecent.setVisibility(View.INVISIBLE);
            buttonLastGame.setVisibility(View.INVISIBLE);
        }

        if (dbHelper.open().numRows() == 1 && reviewLaterBool){
            createReviewDialog();
        }


        displayRecyclerView();

        verifyStoragePermissions(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            File sdcard = Environment.getExternalStorageDirectory();
            File file = new File(sdcard, "/ScoreKeeper");
            file.mkdirs();
            String file_url = "https://raw.githubusercontent.com/SDS-Studios/ScoreKeeper/buggy/CHANGELOG.txt";
            new DownloadFileFromURL("/ScoreKeeper/changelog_scorekeeper.txt").execute(file_url);

            String downloadUrl = "https://raw.githubusercontent.com/SDS-Studios/ScoreKeeper/buggy/LICENSE.txt";
            new DownloadFileFromURL("/ScoreKeeper/license_scorekeeper.txt").execute(downloadUrl);
        }

    }

    public boolean anyUnfinishedGames(){
        boolean unfinishedGames = false;

        for (int i = 1; i < dbHelper.open().numRows(); i++){
            if (dataHelper.getIntByID(i, ScoreDBAdapter.KEY_COMPLETED, dbHelper.open()) == 0){
                unfinishedGames = true;
            }
        }

        dbHelper.close();
        return unfinishedGames;
    }

    public void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
            File sdcard = Environment.getExternalStorageDirectory();
            File file = new File(sdcard, "/ScoreKeeper");
            file.mkdirs();
            String file_url = "https://raw.githubusercontent.com/SDS-Studios/ScoreKeeper/buggy/CHANGELOG.txt";
            new DownloadFileFromURL("/ScoreKeeper/changelog_scorekeeper.txt").execute(file_url);

            String downloadUrl = "https://raw.githubusercontent.com/SDS-Studios/ScoreKeeper/buggy/LICENSE.txt";
            new DownloadFileFromURL("/ScoreKeeper/license_scorekeeper.txt").execute(downloadUrl);
        }else{
            File sdcard = Environment.getExternalStorageDirectory();
            File file = new File(sdcard, "/ScoreKeeper");
            file.mkdirs();
            String file_url = "https://raw.githubusercontent.com/SDS-Studios/ScoreKeeper/buggy/CHANGELOG.txt";
            new DownloadFileFromURL("/ScoreKeeper/changelog_scorekeeper.txt").execute(file_url);

            String downloadUrl = "https://raw.githubusercontent.com/SDS-Studios/ScoreKeeper/buggy/LICENSE.txt";
            new DownloadFileFromURL("/ScoreKeeper/license_scorekeeper.txt").execute(downloadUrl);

        }

    }

    private void createReviewDialog(){
        final AlertDialog dialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.please_review);

        builder.setMessage(R.string.review_message);

        builder.setPositiveButton(R.string.review, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=io.github.sdsstudios.ScoreKeeper"));
                startActivity(browserIntent);
                reviewLaterBool = false;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("reviewlater", reviewLaterBool);
                editor.apply();
            }
        });

        builder.setNeutralButton(R.string.remind_me_later, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                reviewLaterBool = true;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("reviewlater", reviewLaterBool);
                editor.apply();
                dialogInterface.dismiss();
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                reviewLaterBool = false;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("reviewlater", reviewLaterBool);
                editor.apply();
                dialog.dismiss();
            }
        });

        dialog = builder.create();

        dialog.show();
    }

    public void displayRecyclerView(){
        dbHelper.open();

        try {
            if (dbHelper.numRows() != 0) {

                RecyclerView.LayoutManager mLayoutManager;
                mLayoutManager = new LinearLayoutManager(this);
                recyclerView.setLayoutManager(mLayoutManager);
                ArrayList<GameModel> gameModel = GameModel.createGameModel(numGamesToShow, 1, this, dbHelper);
                if (gameModel.isEmpty()){
                    relativeLayoutRecent.setVisibility(View.INVISIBLE);
                    recyclerView.setVisibility(View.INVISIBLE);
                }else{
                    relativeLayoutRecent.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                    HistoryAdapter historyAdapter = new HistoryAdapter(gameModel, this, this, true);
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
        MenuItem settingsMenuItem = menu.findItem(R.id.action_settings);
        MenuItem aboutMenuItem = menu.findItem(R.id.action_about);
        settingsMenuItem.setVisible(true);
        aboutMenuItem.setVisible(true);

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
    public void onItemClicked(int position, final int gameID) {
        try {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("gameID", gameID);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getCause().toString(), Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public boolean onItemLongClicked(int position, int gameID) {
        return false;
    }
}



