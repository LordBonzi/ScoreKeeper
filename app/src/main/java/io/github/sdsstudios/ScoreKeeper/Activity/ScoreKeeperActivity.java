package io.github.sdsstudios.ScoreKeeper.Activity;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;

import io.github.sdsstudios.ScoreKeeper.About;
import io.github.sdsstudios.ScoreKeeper.Adapters.GameDBAdapter;
import io.github.sdsstudios.ScoreKeeper.Game;
import io.github.sdsstudios.ScoreKeeper.Helper.DataHelper;
import io.github.sdsstudios.ScoreKeeper.Helper.TimeHelper;
import io.github.sdsstudios.ScoreKeeper.History;
import io.github.sdsstudios.ScoreKeeper.Home;
import io.github.sdsstudios.ScoreKeeper.NewGame;
import io.github.sdsstudios.ScoreKeeper.PlayersActivity;
import io.github.sdsstudios.ScoreKeeper.Settings;
import io.github.sdsstudios.ScoreKeeper.Themes;

import static io.github.sdsstudios.ScoreKeeper.Activity.Activity.EDIT_GAME;

/**
 * Created by seth on 11/01/17.
 */

public abstract class ScoreKeeperActivity extends AppCompatActivity {

    public static String TAG;
    public static Activity CURRENT_ACTIVITY;

    public Game mGame;

    public GameDBAdapter mDbHelper;
    public DataHelper mDataHelper = new DataHelper();
    public TimeHelper mTimeHelper = new TimeHelper();

    public AlertDialog mAlertDialog;
    public Snackbar mSnackBar;
    public int mAccentColor, mPrimaryColor;

    public SharedPreferences mSharedPreferences;

    public Intent mNewGameIntent, mSettingsIntent, mAboutIntent, mHistoryIntent, mPlayersIntent, mHomeIntent;

    public DialogInterface.OnClickListener mDismissDialogListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CURRENT_ACTIVITY = getActivity();
        TAG = getLocalClassName();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mAccentColor = mSharedPreferences.getInt("prefAccentColor", Themes.DEFAULT_ACCENT_COLOR);
        mPrimaryColor = mSharedPreferences.getInt("prefPrimaryColor", Themes.DEFAULT_PRIMARY_COLOR(this));

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

    public abstract Activity getActivity();

    @SuppressWarnings("deprecation")
    private void removeLayoutListenerJB(View v, ViewTreeObserver.OnGlobalLayoutListener victim) {
        v.getViewTreeObserver().removeGlobalOnLayoutListener(victim);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void removeLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener victim) {
        v.getViewTreeObserver().removeOnGlobalLayoutListener(victim);
    }

    public void updateGameInDatabase() {
        if (CURRENT_ACTIVITY != EDIT_GAME) {
            mDbHelper.open().updateGame(mGame);
            mDbHelper.close();
        }
    }

    public int getOrientation() {
        return getResources().getConfiguration().orientation;
    }

}
