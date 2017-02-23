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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

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

/**
 * Created by seth on 11/01/17.
 */

public abstract class ScoreKeeperActivity extends AppCompatActivity {

    public static String TAG;
    public static Activity CURRENT_ACTIVITY;

    public Game game;

    public GameDBAdapter gameDBAdapter;
    public DataHelper dataHelper = new DataHelper();
    public TimeHelper timeHelper = new TimeHelper();

    public LayoutInflater layoutInflater;

    public int accentColor, primaryColor;

    public SharedPreferences sharedPreferences;

    public Intent newGameIntent, settingsIntent, aboutIntent, historyIntent, playersIntent, homeIntent;

    public DialogInterface.OnClickListener dismissDialogListener = new DialogInterface.OnClickListener() {
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

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        accentColor = sharedPreferences.getInt("prefAccentColor", Themes.DEFAULT_ACCENT_COLOR);
        primaryColor = sharedPreferences.getInt("prefPrimaryColor", Themes.DEFAULT_PRIMARY_COLOR(this));

        gameDBAdapter = new GameDBAdapter(this);

        newGameIntent = new Intent(this, NewGame.class);
        homeIntent = new Intent(this, Home.class);
        aboutIntent = new Intent(this, About.class);
        settingsIntent = new Intent(this, Settings.class);
        historyIntent = new Intent(this, History.class);
        playersIntent = new Intent(this, PlayersActivity.class);

        layoutInflater = getLayoutInflater();
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

    public void saveGameToDatabase() {
        gameDBAdapter.open().updateGame(game);
        gameDBAdapter.close();

    }

    public void onDialogShown() {

    }

    public AlertDialog showCustomAlertDialog(String title, String message,
                                             String positiveButtonText, DialogInterface.OnClickListener positiveButtonClickListener,
                                             String negativeButtonText, DialogInterface.OnClickListener negativeButtonClickListener, View layout) {

        return createDialogBuilder(title, message, positiveButtonText, positiveButtonClickListener, negativeButtonText, negativeButtonClickListener)
                .setView(layout).show();

    }

    public AlertDialog showCustomAlertDialog(String title, String message,
                                             String positiveButtonText, DialogInterface.OnClickListener positiveButtonClickListener,
                                             String neutralButtonText, DialogInterface.OnClickListener neutralButtonClickListener,
                                             String negativeButtonText, DialogInterface.OnClickListener negativeButtonClickListener, View layout) {

        return createDialogBuilder(title, message, positiveButtonText, positiveButtonClickListener, negativeButtonText, negativeButtonClickListener)
                .setNeutralButton(neutralButtonText, neutralButtonClickListener).setView(layout).show();
    }

    public AlertDialog showAlertDialog(String title, String message,
                                       String positiveButtonText, DialogInterface.OnClickListener positiveButtonClickListener,
                                       String neutralButtonText, DialogInterface.OnClickListener neutralButtonClickListener,
                                       String negativeButtonText, DialogInterface.OnClickListener negativeButtonClickListener) {

        return createDialogBuilder(title, message, positiveButtonText, positiveButtonClickListener, negativeButtonText, negativeButtonClickListener)
                .setNeutralButton(neutralButtonText, neutralButtonClickListener).show();
    }

    public AlertDialog showAlertDialog(String title, String message,
                                       String positiveButtonText, DialogInterface.OnClickListener positiveButtonClickListener,
                                       String negativeButtonText, DialogInterface.OnClickListener negativeButtonClickListener) {

        return createDialogBuilder(title, message, positiveButtonText, positiveButtonClickListener, negativeButtonText, negativeButtonClickListener).show();
    }

    public abstract void onDialogDismissed();

    public AlertDialog.Builder createDialogBuilder(String title, String message,
                                                   String positiveButtonText, DialogInterface.OnClickListener positiveButtonClickListener,
                                                   String negativeButtonText, DialogInterface.OnClickListener negativeButtonClickListener) {

        AlertDialog.Builder builder = createDialogBuilder(title, message);

        if (positiveButtonText != null) {
            builder.setPositiveButton(positiveButtonText, positiveButtonClickListener);
        }
        if (negativeButtonText != null) {
            builder.setNegativeButton(negativeButtonText, negativeButtonClickListener);
        }

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                onDialogDismissed();
            }
        });

        onDialogShown();

        return builder;
    }

    public AlertDialog.Builder createDialogBuilder(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (title != null) {
            builder.setTitle(title);
        }

        if (message != null) {
            builder.setMessage(message);
        }

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                onDialogDismissed();
            }
        });

        onDialogShown();

        return builder;
    }

    public void showTextDialog(String title, String message, String positiveButtonText) {
        createDialogBuilder(title, message).setPositiveButton(positiveButtonText, dismissDialogListener).create().show();
    }

    public int getOrientation() {
        return getResources().getConfiguration().orientation;
    }

    public View.OnClickListener dismissSnackBarListener(final Snackbar snackbar) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        };
    }

    public void createSnackbar(RelativeLayout layout, String message) {
        Snackbar snackbar = Snackbar.make(layout, message, Snackbar.LENGTH_SHORT);
        snackbar.setAction("Dismiss", dismissSnackBarListener(snackbar));
        snackbar.show();

    }

}
