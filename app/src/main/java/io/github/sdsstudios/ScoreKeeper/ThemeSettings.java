package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;

import com.google.firebase.analytics.FirebaseAnalytics;

public class ThemeSettings extends PreferenceActivity{
    private Intent settingsIntent;
    private FirebaseAnalytics mFirebaseAnalytics;
    private AppCompatDelegate mDelegate;
    private SharedPreferences settings;
    private Preference  darkThemePreference, accentPreference, primaryPreference;
    SharedPreferences.OnSharedPreferenceChangeListener listener;
    private boolean darkTheme;
    private int accentColor, primaryColor, primaryDarkColor;
    int[] colors = {};
    int[] primaryColors = {};
    int[] primaryDarkColors = {};
    int index;
    int oldColorIndex;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
        darkTheme = sharedPreferences.getBoolean("prefDarkTheme", false);
        colorIndex();
        accentColor = sharedPreferences.getInt("prefAccent", colors[1]);
        colorIndex();
        oldColorIndex = index;
        primaryColor = sharedPreferences.getInt("prefPrimaryColor", getPrimaryColors()[0]);
        primaryDarkColor = sharedPreferences.getInt("prefPrimaryDarkColor", getPrimaryDarkColors()[0]);

        setTheme(accentColor);
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(primaryColor);
        setSupportActionBar(toolbar);
        getDelegate().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(primaryDarkColor);
        }
        addPreferencesFromResource(R.xml.theme_settings);

        darkThemePreference = findPreference("prefDarkTheme");
        accentPreference = findPreference("prefAccentColor");
        primaryPreference = findPreference("prefPrimaryColor");

        settingsIntent = new Intent(this, Settings.class);

        final int[] rawColors = new int[] {
                getResources().getColor(R.color.accentGrey),
                getResources().getColor(R.color.accentPink),
                getResources().getColor(R.color.accentYellow),
                getResources().getColor(R.color.accentGreen),
                getResources().getColor(R.color.accentRed),
                getResources().getColor(R.color.accentPurple),
                getResources().getColor(R.color.accentOrange),
                getResources().getColor(R.color.accentBlue)

        };

        darkThemePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                darkTheme = !darkTheme;
                saveInfo();
                colorIndex();
                accentColor = colors[oldColorIndex-1];
                saveInfo();
                Intent intent = getIntent();
                finish();
                startActivity(intent);

                return true;
            }
        });

        accentPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                accentColor = settings.getInt("prefAccent", colors[1]);


                final View dialogView;

                LayoutInflater inflter = LayoutInflater.from(getBaseContext());
                final AlertDialog alertDialog;
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ThemeSettings.this);
                dialogView = inflter.inflate(R.layout.accent_color_fragment, null);

                colorIndex();

                final GridView gridView = (GridView)dialogView.findViewById(R.id.gridView);
                final GridViewAdapter gridViewAdapter = new GridViewAdapter(ThemeSettings.this, index, colors, rawColors, true);

                gridView.setAdapter(gridViewAdapter);

                dialogBuilder.setNeutralButton(R.string._default, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        accentColor = colors[1];
                        saveInfo();

                        colorIndex();
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                });

                dialogBuilder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        colorIndex();
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }

                });

                dialogBuilder.setView(dialogView);

                alertDialog = dialogBuilder.create();


                alertDialog.show();

                return true;
            }
        });

        primaryPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                primaryColor = settings.getInt("prefPrimaryColor", getPrimaryColors()[1]);
                primaryDarkColor = settings.getInt("prefPrimaryDarkColor", getPrimaryDarkColors()[1]);

                final View dialogView;

                LayoutInflater inflter = LayoutInflater.from(getBaseContext());
                final AlertDialog alertDialog;
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ThemeSettings.this);
                dialogView = inflter.inflate(R.layout.accent_color_fragment, null);

                index = 1;
                for (int i = 0; i < getPrimaryColors().length; i++){
                    if (primaryColor == getPrimaryColors()[i]){
                        index = i+1;
                    }
                }
                final GridView gridView = (GridView)dialogView.findViewById(R.id.gridView);
                final GridViewAdapter gridViewAdapter = new GridViewAdapter(ThemeSettings.this, index, getPrimaryColors(), getPrimaryDarkColors(),false);

                gridView.setAdapter(gridViewAdapter);

                dialogBuilder.setNeutralButton(R.string._default, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        primaryColor = getPrimaryColors()[0];
                        primaryDarkColor = getPrimaryDarkColors()[0];
                        saveInfo();
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                });

                dialogBuilder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }

                });

                dialogBuilder.setView(dialogView);

                alertDialog = dialogBuilder.create();


                alertDialog.show();

                return true;
            }
        });

        //Shared prefs stuff
        settings = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
        darkTheme = settings.getBoolean("prefDarkTheme", false);
        accentColor = settings.getInt("prefAccent", colors[1]);
    }

    public int[] getPrimaryColors(){
        primaryColors = new int[]{
                getResources().getColor(R.color.primaryIndigo),
                getResources().getColor(R.color.primaryRed),
                getResources().getColor(R.color.primaryPurple),
                getResources().getColor(R.color.primaryTeal),
                getResources().getColor(R.color.primaryOrange),
                getResources().getColor(R.color.primaryGrey),
                getResources().getColor(R.color.primaryWhite),
                getResources().getColor(R.color.primaryBlue)

        };
        return primaryColors;
    }

    public int[] getPrimaryDarkColors(){
        primaryDarkColors = new int[]{
                getResources().getColor(R.color.primaryIndigoDark),
                getResources().getColor(R.color.primaryRedDark),
                getResources().getColor(R.color.primaryPurpleDark),
                getResources().getColor(R.color.primaryTealDark),
                getResources().getColor(R.color.primaryOrangeDark),
                getResources().getColor(R.color.primaryGreyDark),
                getResources().getColor(R.color.primaryWhiteDark),
                getResources().getColor(R.color.primaryBlueDark)

        };
        return primaryDarkColors;
    }

    private void setSupportActionBar(@Nullable Toolbar toolbar) {
        getDelegate().setSupportActionBar(toolbar);
    }

    private void getSupportActionBar() {
        getDelegate().getSupportActionBar();
    }

    public void colorIndex(){
        if (darkTheme) {
            colors = new int[]{
                    R.style.DarkTheme_Grey,
                    R.style.DarkTheme_Pink,
                    R.style.DarkTheme_Yellow,
                    R.style.DarkTheme_Green,
                    R.style.DarkTheme_Red,
                    R.style.DarkTheme_Purple,
                    R.style.DarkTheme_Orange,
                    R.style.DarkTheme_Blue

            };
        }else{
            colors = new int[]{
                    R.style.AppTheme_Grey,
                    R.style.AppTheme_Pink,
                    R.style.AppTheme_Yellow,
                    R.style.AppTheme_Green,
                    R.style.AppTheme_Red,
                    R.style.AppTheme_Purple,
                    R.style.AppTheme_Orange,
                    R.style.AppTheme_Blue

            };
        }
        index = 1;
        for (int i = 0; i < colors.length; i++){
            if (accentColor == colors[i]){
                index = i+1;
            }
        }


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
        editor.putBoolean("prefDarkTheme", darkTheme);
        editor.putInt("prefAccent", accentColor);
        editor.putInt("prefPrimaryColor", primaryColor);
        editor.putInt("prefPrimaryDarkColor", primaryDarkColor);

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
