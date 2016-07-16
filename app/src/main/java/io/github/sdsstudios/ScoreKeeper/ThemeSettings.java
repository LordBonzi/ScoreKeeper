package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.analytics.FirebaseAnalytics;

public class ThemeSettings extends PreferenceActivity{
    private Intent settingsIntent;
    private FirebaseAnalytics mFirebaseAnalytics;
    private AppCompatDelegate mDelegate;
    private SharedPreferences settings;
    private Preference colorisePreference, darkThemePreference;
    SharedPreferences.OnSharedPreferenceChangeListener listener;
    private boolean colorise, darkTheme;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
        darkTheme = sharedPreferences.getBoolean("prefDarkTheme", false);

        if (darkTheme){
            setTheme(R.style.DarkTheme);
        }else{
            setTheme(R.style.AppTheme);
        }
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getDelegate().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        addPreferencesFromResource(R.xml.theme_settings);
        colorisePreference = findPreference("prefColoriseUnfinishedGames");
        darkThemePreference = findPreference("prefDarkTheme");

        settingsIntent = new Intent(this, Settings.class);

        colorisePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                colorise = !colorise;
                saveInfo();
                return true;
            }
        });

        darkThemePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                darkTheme = !darkTheme;
                saveInfo();

                return true;
            }
        });

        //Shared prefs stuff
        settings = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
        colorise = settings.getBoolean("prefColoriseUnfinishedGames", false);
        darkTheme = settings.getBoolean("prefDarkTheme", false);
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
        editor.putBoolean("prefDarkTheme", darkTheme);

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
        startActivity(settingsIntent);
    }

}
