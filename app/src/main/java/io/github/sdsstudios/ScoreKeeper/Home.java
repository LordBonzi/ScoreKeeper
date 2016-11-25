package io.github.sdsstudios.ScoreKeeper;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
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
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;

public class Home extends AppCompatActivity implements HistoryAdapter.ViewHolder.ClickListener{

    private Intent mNewGameIntent;
    private Intent mSettingsIntent;
    private Intent mAboutIntent;
    private Intent mHistoryIntent;
    private ScoreDBAdapter mDbHelper;
    private RecyclerView mRecyclerView;
    private SharedPreferences mSharedPreferences;
    private RelativeLayout mRelativeLayoutRecents;
    private String TAG = "Home";
    private int mLastPlayedGame;
    private boolean mReviewLaterBool;
    private DataHelper mDataHelper = new DataHelper();
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private int mNumRows = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int accentColor = mSharedPreferences.getInt("prefAccentColor", R.style.DarkTheme);
        int primaryColor = mSharedPreferences.getInt("prefPrimaryColor", getResources().getColor(R.color.primaryIndigo));
        int primaryDarkColor = mSharedPreferences.getInt("prefPrimaryDarkColor", getResources().getColor(R.color.primaryIndigoDark));
        boolean colorNavBar = mSharedPreferences.getBoolean("prefColorNavBar", false);
        mReviewLaterBool = mSharedPreferences.getBoolean("reviewlater", true);

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

        if (mSharedPreferences.getBoolean("prefReceiveNotifications", true)){
            FirebaseMessaging.getInstance().subscribeToTopic("news");
        }else{
            FirebaseMessaging.getInstance().unsubscribeFromTopic("news");
        }

        mDbHelper = new ScoreDBAdapter(this);
        mNumRows = mDbHelper.open().numRows();
        mDbHelper.close();

        mLastPlayedGame = mSharedPreferences.getInt("lastplayedgame", mDbHelper.open().getNewestGame());
        mNewGameIntent = new Intent(this, NewGame.class);
        mAboutIntent = new Intent(this, About.class);
        mSettingsIntent = new Intent(this, Settings.class);
        mHistoryIntent = new Intent(this, History.class);
        mRelativeLayoutRecents = (RelativeLayout)findViewById(R.id.layoutRecentGames);

        Button buttonMore = (Button) findViewById(R.id.buttonMore);
        buttonMore.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startActivity(mHistoryIntent);
            }
        });

        Button buttonLastGame = (Button) findViewById(R.id.buttonContinueLastGame);

        mRecyclerView = (RecyclerView)findViewById(R.id.homeRecyclerView);

        TextView textViewNumGames = (TextView) findViewById(R.id.textViewNumGamesPlayed);
        textViewNumGames.setText(String.valueOf(mDbHelper.numRows()));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabNewGame);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(mNewGameIntent);
            }
        });

        buttonLastGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Home.this, GameActivity.class);
                intent.putExtra("GAME_ID", mLastPlayedGame);
                startActivity(intent);
            }
        });

        if (mNumRows == 0){
            mRelativeLayoutRecents.setVisibility(View.INVISIBLE);
            buttonMore.setVisibility(View.INVISIBLE);
            buttonLastGame.setVisibility(View.INVISIBLE);

        }else if (!anyUnfinishedGames()){
            mRelativeLayoutRecents.setVisibility(View.INVISIBLE);
            buttonLastGame.setVisibility(View.INVISIBLE);

        }else{

            displayRecyclerView();

        }

        if (mNumRows == 1 && mReviewLaterBool){
            createReviewDialog();
        }

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

    public boolean anyUnfinishedGames() {

        boolean unfinishedGames = false;

        try {

            for (int i = 1; i <= mNumRows; i++) {
                if (!mDataHelper.getGame(i, mDbHelper.open()).ismCompleted()) {
                    unfinishedGames = true;
                }
            }

        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, e.toString());
            Toast.makeText(this, "Error running anyUnfinishedGames() method", Toast.LENGTH_SHORT).show();
        }

        mDbHelper.close();
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
                mReviewLaterBool = false;
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putBoolean("reviewlater", mReviewLaterBool);
                editor.apply();
            }
        });

        builder.setNeutralButton(R.string.remind_me_later, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mReviewLaterBool = true;
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putBoolean("reviewlater", mReviewLaterBool);
                editor.apply();
                dialogInterface.dismiss();
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                mReviewLaterBool = false;
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putBoolean("reviewlater", mReviewLaterBool);
                editor.apply();
                dialog.dismiss();
            }
        });

        dialog = builder.create();

        dialog.show();
    }

    public synchronized void displayRecyclerView(){

        mDbHelper.open();

        try {

            if (mNumRows != 0) {

                RecyclerView.LayoutManager mLayoutManager;
                mLayoutManager = new LinearLayoutManager(this);
                mRecyclerView.setLayoutManager(mLayoutManager);

                HistoryAdapter historyAdapter = new HistoryAdapter(HistoryModel.getHistoryModelList(mDbHelper, this, Pointers.HOME)
                        , this, this, Pointers.HISTORY, HistoryAdapter.UNFINISHED);

                mRecyclerView.setAdapter(historyAdapter);

            } else {
                mRecyclerView.setVisibility(View.INVISIBLE);
            }
            mDbHelper.close();
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
        mDbHelper.open();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mDbHelper.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(mSettingsIntent);
            return true;
        }if (id == R.id.action_about) {
            startActivity(mAboutIntent);
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
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra("GAME_ID", gameID);
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



