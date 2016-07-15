package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

public class Themes extends PreferenceActivity{
    private ScoreDBAdapter dbHelper;
    private Intent homeIntent;
    private FirebaseAnalytics mFirebaseAnalytics;
    private AppCompatDelegate mDelegate;
    private SharedPreferences settings;
    private Preference deletePreference, timeLimitPreference, colorisePreference, numGamesPreference;
    SharedPreferences.OnSharedPreferenceChangeListener listener;
    AlertDialog dialog;
    private DataHelper dataHelper;
    private boolean colorise;
    private String numGamesToShow;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getDelegate().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        addPreferencesFromResource(R.xml.content_settings);

        dataHelper = new DataHelper();

        deletePreference = findPreference("prefDeleteAllGames");
        timeLimitPreference = findPreference("prefDeleteTimeLimit");
        colorisePreference = findPreference("prefColoriseUnfinishedGames");
        numGamesPreference = findPreference("prefNumGames");

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        deletePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                builder.setTitle(getResources().getString(R.string.delete_all_games) + "?");

                builder.setMessage(R.string.delete_all_games_mes);

                builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            dbHelper.deleteAllgames();
                            Toast.makeText(Themes.this, "Successfully deleted games", Toast.LENGTH_SHORT).show();
                        }catch (Exception e){
                            Toast.makeText(Themes.this, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

                dialog = builder.create();
                dialog.show();
                return true;
            }
        });

        colorisePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                colorise = !colorise;
                saveInfo();
                return true;
            }
        });

        numGamesPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                numGamesToShow = (String) o;
                saveInfo();
                return true;
            }

        });


        dbHelper = new ScoreDBAdapter(this);
        dbHelper.open();

        homeIntent = new Intent(this, Home.class);

        //Shared prefs stuff

        settings = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
        colorise = settings.getBoolean("prefColoriseUnfinishedGames", false);
        numGamesToShow = settings.getString("numgamestoshow", "3");

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
        if (id == android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveInfo(){
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("prefColoriseUnfinishedGames", colorise);
        editor.putString("numgamestoshow", numGamesToShow);
        editor.apply();

    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getDelegate().onPostResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        getDelegate().onStop();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(listener);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(listener);
    }


    private void setSupportActionBar(@Nullable Toolbar toolbar) {
        getDelegate().setSupportActionBar(toolbar);
    }

    private void getSupportActionBar() {
        getDelegate().getSupportActionBar();
    }

    private AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, null);
        }
        return mDelegate;
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        getDelegate().setContentView(layoutResID);
    }


    @Override
    public void onBackPressed() {
        startActivity(homeIntent);
    }

}
