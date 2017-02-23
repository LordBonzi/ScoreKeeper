package io.github.sdsstudios.ScoreKeeper;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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

import io.github.sdsstudios.ScoreKeeper.Activity.ScoreKeeperActivity;
import io.github.sdsstudios.ScoreKeeper.Adapters.HistoryAdapter;
import io.github.sdsstudios.ScoreKeeper.Helper.DialogHelper;

public class Home extends ScoreKeeperActivity implements HistoryAdapter.ViewHolder.ClickListener, NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private RecyclerView mRecyclerView;
    private int mLastPlayedGame;
    private boolean mReviewLaterBool;
    private int mNumRows = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mReviewLaterBool = sharedPreferences.getBoolean("reviewlater", true);

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

        if (sharedPreferences.getBoolean("prefReceiveNotifications", true)) {
            FirebaseMessaging.getInstance().subscribeToTopic("news");
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("news");
        }

        mNumRows = gameDBAdapter.open().numRows();
        gameDBAdapter.close();

        mLastPlayedGame = sharedPreferences.getInt("lastplayedgame", gameDBAdapter.open().getNewestGame());

        RelativeLayout relativeLayoutRecents = (RelativeLayout) findViewById(R.id.layoutRecentGames);
        Button buttonLastGame = (Button) findViewById(R.id.buttonContinueLastGame);
        TextView textViewNoUnfinishedGames = (TextView) findViewById(R.id.textViewNoUnfinishedGames);

        mRecyclerView = (RecyclerView) findViewById(R.id.homeRecyclerView);

        TextView textViewNumGames = (TextView) findViewById(R.id.textViewNumGamesPlayed);
        textViewNumGames.setText(String.valueOf(gameDBAdapter.numRows()));

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
                Intent intent = new Intent(Home.this, GameActivity.class);
                intent.putExtra("GAME_ID", mLastPlayedGame);
                startActivity(intent);
            }
        });

        if (mNumRows == 0) {
            textViewNoUnfinishedGames.setVisibility(View.VISIBLE);
            textViewNoUnfinishedGames.setText(getString(R.string.you_have_played_no_games));

            relativeLayoutRecents.setVisibility(View.INVISIBLE);
            buttonLastGame.setVisibility(View.INVISIBLE);

        } else if (!anyUnfinishedGames()) {
            textViewNoUnfinishedGames.setText(getString(R.string.you_have_no_unfinished_games));
            textViewNoUnfinishedGames.setVisibility(View.VISIBLE);

            relativeLayoutRecents.setVisibility(View.INVISIBLE);
            buttonLastGame.setVisibility(View.INVISIBLE);

        } else {

            textViewNoUnfinishedGames.setVisibility(View.GONE);
            displayRecyclerView();

        }

        if (mNumRows == 1 && mReviewLaterBool) {
            createReviewDialog();
        }

        verifyStoragePermissions(this);

        showWhatsNewDialog();
    }

    private void showWhatsNewDialog() {
        int currentVersionCode = Integer.parseInt(getString(R.string.versionCode));

        if (sharedPreferences.getInt("version_code", 1) < currentVersionCode) {
            DialogHelper.textViewAlertDialog(this, FileReader.textFromFileToString(FileReader.WHATS_NEW, this), getString(R.string.whats_new));
        }

        sharedPreferences.edit().putInt("version_code", currentVersionCode).apply();
    }

    @Override
    public io.github.sdsstudios.ScoreKeeper.Activity.Activity getActivity() {
        return io.github.sdsstudios.ScoreKeeper.Activity.Activity.HOME;
    }

    @Override
    public void onDialogDismissed() {

    }

    private boolean anyUnfinishedGames() {

        boolean unfinishedGames = false;

        try {

            for (int i = 1; i <= mNumRows; i++) {
                if (!dataHelper.getGame(i, gameDBAdapter.open()).ismCompleted()) {
                    unfinishedGames = true;
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
            Toast.makeText(this, "Error running anyUnfinishedGames() method", Toast.LENGTH_SHORT).show();
        }

        gameDBAdapter.close();
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
        }
    }

    private void createReviewDialog() {

        showAlertDialog(getString(R.string.please_review), getString(R.string.review_message), getString(R.string.review),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=io.github.sdsstudios.ScoreKeeper"));
                        startActivity(browserIntent);
                        mReviewLaterBool = false;
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("reviewlater", mReviewLaterBool);
                        editor.apply();
                    }
                }, getString(R.string.remind_me_later), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mReviewLaterBool = true;
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("reviewlater", mReviewLaterBool);
                        editor.apply();
                        dialogInterface.dismiss();
                    }
                }, getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        mReviewLaterBool = false;
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("reviewlater", mReviewLaterBool);
                        editor.apply();
                        dialog.dismiss();
                    }
                });
    }

    private synchronized void displayRecyclerView() {

        gameDBAdapter.open();

        try {

            if (mNumRows != 0) {

                RecyclerView.LayoutManager mLayoutManager;
                mLayoutManager = new LinearLayoutManager(this);
                mRecyclerView.setLayoutManager(mLayoutManager);

                HistoryAdapter historyAdapter = new HistoryAdapter(HistoryModel.getHistoryModelList(gameDBAdapter, this, io.github.sdsstudios.ScoreKeeper.Activity.Activity.HOME, HistoryAdapter.UNFINISHED)
                        , this, this, io.github.sdsstudios.ScoreKeeper.Activity.Activity.HOME);

                mRecyclerView.setAdapter(historyAdapter);

            } else {
                mRecyclerView.setVisibility(View.INVISIBLE);
            }
            gameDBAdapter.close();

        } catch (Exception e) {

            e.printStackTrace();
            Log.e(TAG, e.toString());

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameDBAdapter.open();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameDBAdapter.close();
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

        switch (id) {
            case R.id.nav_history:

                if (gameDBAdapter.open().numRows() > 0) {
                    startActivity(historyIntent);
                } else {
                    Toast.makeText(this, R.string.no_games, Toast.LENGTH_SHORT).show();
                }

                break;

            case R.id.nav_players:
                Toast.makeText(this, R.string.graph_coming_soon, Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_settings:
                startActivity(settingsIntent);
                break;

            case R.id.nav_about:
                startActivity(aboutIntent);
                break;

            case R.id.nav_rate_review:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=io.github.sdsstudios.ScoreKeeper"));
                startActivity(browserIntent);
                break;

            case R.id.nav_email_dev:
                Intent send = new Intent(Intent.ACTION_SENDTO);
                String uriText = "mailto:" + Uri.encode("seth.d.schroeder@gmail.com") +
                        "?subject=" + Uri.encode("Feedback for Score Keeper app") +
                        "&body=" + Uri.encode("");
                Uri uri = Uri.parse(uriText);

                send.setData(uri);
                startActivity(Intent.createChooser(send, "Send mail..."));
                startActivity(send);
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}



