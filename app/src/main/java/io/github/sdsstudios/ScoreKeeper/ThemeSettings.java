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

import com.google.android.gms.ads.AdView;

public class ThemeSettings extends PreferenceActivity{
    private Intent mSettingsIntent;
    private AppCompatDelegate mDelegate;
    private SharedPreferences mSharedPreferences;
    SharedPreferences.OnSharedPreferenceChangeListener mSharedPreferenceChangeListener;
    private boolean mDarkTheme, mClassicTheme, mColorNavBar;
    private int mAccentColor, mPrimaryColor, mPrimaryDarkColor;
    private int[] mColors = {};
    private int mColorIndex;
    private int mOldColorIndex;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPreferences = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
        mClassicTheme = mSharedPreferences.getBoolean("prefClassicTheme", false);
        mDarkTheme = mSharedPreferences.getBoolean("prefDarkTheme", false);
        mColorNavBar = mSharedPreferences.getBoolean("prefColorNavBar", false);
        colorIndex();
        mAccentColor = mSharedPreferences.getInt("prefAccent", mColors[1]);
        colorIndex();
        mOldColorIndex = mColorIndex;
        mPrimaryColor = mSharedPreferences.getInt("prefPrimaryColor", getPrimaryColors()[0]);
        mPrimaryDarkColor = mSharedPreferences.getInt("prefPrimaryDarkColor", getPrimaryDarkColors()[0]);

        setTheme(mAccentColor);
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        AdView mAdView = (AdView) findViewById(R.id.adViewHome);
        AdCreator adCreator = new AdCreator(mAdView, this);
        adCreator.createAd();
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(mPrimaryColor);
        setSupportActionBar(toolbar);
        getDelegate().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(mPrimaryDarkColor);
        }
        if (mColorNavBar){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setNavigationBarColor(mPrimaryDarkColor);
            }
        }
        addPreferencesFromResource(R.xml.theme_settings);

        Preference darkThemePreference = findPreference("prefDarkTheme");
        Preference accentPreference = findPreference("prefAccentColor");
        Preference primaryPreference = findPreference("prefPrimaryColor");
        Preference classicPreference = findPreference("prefClassicScoreboard");
        Preference defaultThemePreference = findPreference("prefDefaultTheme");
        Preference colorNavBarPreference = findPreference("prefColorNavBar");

        mSettingsIntent = new Intent(this, Settings.class);

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
        mDarkTheme = mSharedPreferences.getBoolean("prefDarkTheme", false);
        mAccentColor = mSharedPreferences.getInt("prefAccent", mColors[1]);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            colorNavBarPreference.setEnabled(true);
            colorNavBarPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    mColorNavBar = !mColorNavBar;
                    saveInfo();
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                    return true;
                }
            });
        }else{
            colorNavBarPreference.setEnabled(false);
        }

        defaultThemePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mAccentColor = mColors[1];
                mPrimaryColor = getPrimaryColors()[0];
                mPrimaryDarkColor = getPrimaryDarkColors()[0];
                saveInfo();
                Intent intent = getIntent();
                finish();
                startActivity(intent);
                return true;
            }
        });

        darkThemePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mDarkTheme = !mDarkTheme;
                saveInfo();
                colorIndex();
                mAccentColor = mColors[mOldColorIndex -1];
                saveInfo();
                Intent intent = getIntent();
                finish();
                startActivity(intent);

                return true;
            }
        });

        classicPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mClassicTheme = !mClassicTheme;
                saveInfo();
                return true;
            }
        });

        accentPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mAccentColor = mSharedPreferences.getInt("prefAccent", mColors[1]);


                final View dialogView;

                LayoutInflater inflter = LayoutInflater.from(getBaseContext());
                final AlertDialog alertDialog;
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ThemeSettings.this);
                dialogView = inflter.inflate(R.layout.accent_color_fragment, null);

                colorIndex();

                final GridView gridView = (GridView)dialogView.findViewById(R.id.gridView);
                final GridViewAdapter gridViewAdapter = new GridViewAdapter(ThemeSettings.this, mColorIndex, mColors, rawColors, true);

                gridView.setAdapter(gridViewAdapter);

                dialogBuilder.setNeutralButton(R.string._default, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mAccentColor = mColors[1];
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
                mPrimaryColor = mSharedPreferences.getInt("prefPrimaryColor", getPrimaryColors()[1]);
                mPrimaryDarkColor = mSharedPreferences.getInt("prefPrimaryDarkColor", getPrimaryDarkColors()[1]);

                final View dialogView;

                LayoutInflater inflter = LayoutInflater.from(getBaseContext());
                final AlertDialog alertDialog;
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ThemeSettings.this);
                dialogView = inflter.inflate(R.layout.accent_color_fragment, null);

                mColorIndex = 1;
                for (int i = 0; i < getPrimaryColors().length; i++){
                    if (mPrimaryColor == getPrimaryColors()[i]){
                        mColorIndex = i+1;
                    }
                }
                final GridView gridView = (GridView)dialogView.findViewById(R.id.gridView);
                final GridViewAdapter gridViewAdapter = new GridViewAdapter(ThemeSettings.this, mColorIndex, getPrimaryColors(), getPrimaryDarkColors(),false);

                gridView.setAdapter(gridViewAdapter);

                dialogBuilder.setNeutralButton(R.string._default, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mPrimaryColor = getPrimaryColors()[0];
                        mPrimaryDarkColor = getPrimaryDarkColors()[0];
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

    }

    public int[] getPrimaryColors(){
        int[] primaryColors = new int[]{
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
        int[] primaryDarkColors = new int[]{
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
        if (mDarkTheme) {
            mColors = new int[]{
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
            mColors = new int[]{
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
        mColorIndex = 1;
        for (int i = 0; i < mColors.length; i++){
            if (mAccentColor == mColors[i]){
                mColorIndex = i+1;
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
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("prefDarkTheme", mDarkTheme);
        editor.putInt("prefAccent", mAccentColor);
        editor.putInt("prefPrimaryColor", mPrimaryColor);
        editor.putInt("prefPrimaryDarkColor", mPrimaryDarkColor);
        editor.putBoolean("prefClassicTheme", mClassicTheme);
        editor.putBoolean("prefColorNavBar", mColorNavBar);

        editor.apply();

    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
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
                .unregisterOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
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
        startActivity(mSettingsIntent);
    }

}
