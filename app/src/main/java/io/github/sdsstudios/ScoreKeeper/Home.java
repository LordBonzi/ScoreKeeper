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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;

import io.github.sdsstudios.ScoreKeeper.Activity.ScoreKeeperActivity;
import io.github.sdsstudios.ScoreKeeper.Adapters.HistoryAdapter;

public class Home extends ScoreKeeperActivity implements HistoryAdapter.ViewHolder.ClickListener, NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private RecyclerView mRecyclerView;
    private String TAG = "Home";
    private int mLastPlayedGame;
    private boolean mReviewLaterBool;
    private int mNumRows = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReviewLaterBool = mSharedPreferences.getBoolean("reviewlater", true);

        Themes.themeActivity(this, R.layout.activity_home, false);

        AdView mAdView = (AdView) findViewById(R.id.adViewHome);
        AdCreator adCreator = new AdCreator(mAdView, this);
        adCreator.createAd();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, (Toolbar) findViewById(R.id.toolbar), R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.home_nav_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        if (mSharedPreferences.getBoolean("prefReceiveNotifications", true)){
            FirebaseMessaging.getInstance().subscribeToTopic("news");
        }else{
            FirebaseMessaging.getInstance().unsubscribeFromTopic("news");
        }

        mNumRows = mDbHelper.open().numRows();
        mDbHelper.close();

        mLastPlayedGame = mSharedPreferences.getInt("lastplayedgame", mDbHelper.open().getNewestGame());

        RelativeLayout mRelativeLayoutRecents = (RelativeLayout) findViewById(R.id.layoutRecentGames);

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

    @Override
    public io.github.sdsstudios.ScoreKeeper.Activity.Activity getActivity() {
        return io.github.sdsstudios.ScoreKeeper.Activity.Activity.HOME;
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
        int permission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
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

                HistoryAdapter historyAdapter = new HistoryAdapter(HistoryModel.getHistoryModelList(mDbHelper, this, io.github.sdsstudios.ScoreKeeper.Activity.Activity.HOME, HistoryAdapter.UNFINISHED)
                        , this, this, io.github.sdsstudios.ScoreKeeper.Activity.Activity.HISTORY);

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
    public void onBackPressed() {
        super.onBackPressed();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }

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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch(id){
            case R.id.nav_history:

                if (mDbHelper.open().numRows() > 0) {
                    startActivity(mHistoryIntent);
                } else {
                    Toast.makeText(this, R.string.no_games, Toast.LENGTH_SHORT).show();
                }

                break;

            case R.id.nav_players:
                Toast.makeText(this, R.string.graph_coming_soon, Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_settings:
                startActivity(mSettingsIntent);
                break;

            case R.id.nav_about:
                startActivity(mAboutIntent);
                break;

            case R.id.nav_rate_review:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=io.github.sdsstudios.ScoreKeeper"));
                startActivity(browserIntent);
                break;

            case R.id.nav_email_dev:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}



