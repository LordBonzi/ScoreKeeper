package io.github.sdsstudios.ScoreKeeper;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;

import io.github.sdsstudios.ScoreKeeper.Helper.DataHelper;
import io.github.sdsstudios.ScoreKeeper.Helper.TimeHelper;

import static io.github.sdsstudios.ScoreKeeper.Activity.EDIT_GAME;

/**
 * Created by seth on 11/01/17.
 */

public abstract class ScoreKeeperActivity extends AppCompatActivity {

    public static Activity CURRENT_ACTIVITY;

    public Game mGame;

    public GameDBAdapter mDbHelper;
    public DataHelper mDataHelper;
    public TimeHelper mTimeHelper;

    public Snackbar mSnackBar;

    public SharedPreferences mSharedPreferences;

    public Intent mNewGameIntent, mSettingsIntent, mAboutIntent, mHistoryIntent, mPlayersIntent, mHomeIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CURRENT_ACTIVITY = getActivity();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mDbHelper = new GameDBAdapter(this);
        mDataHelper = new DataHelper();
        mTimeHelper = new TimeHelper();
        mDbHelper = new GameDBAdapter(this);

        mNewGameIntent = new Intent(this, NewGame.class);
        mHomeIntent = new Intent(this, Home.class);
        mAboutIntent = new Intent(this, About.class);
        mSettingsIntent = new Intent(this, Settings.class);
        mHistoryIntent = new Intent(this, History.class);
        mPlayersIntent = new Intent(this, PlayersActivity.class);
    }

    public void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener victim) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            removeLayoutListenerJB(v, victim);
        } else removeLayoutListener(v, victim);
    }

    abstract Activity getActivity();

    @SuppressWarnings("deprecation")
    private void removeLayoutListenerJB(View v, ViewTreeObserver.OnGlobalLayoutListener victim) {
        v.getViewTreeObserver().removeGlobalOnLayoutListener(victim);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void removeLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener victim) {
        v.getViewTreeObserver().removeOnGlobalLayoutListener(victim);
    }

    public void updateGame() {

        if (CURRENT_ACTIVITY != EDIT_GAME) {
            mDbHelper.open().updateGame(mGame);
            mDbHelper.close();
        }
    }

}
