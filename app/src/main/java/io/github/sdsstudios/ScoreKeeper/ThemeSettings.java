package io.github.sdsstudios.ScoreKeeper;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
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
    public static int ACCENT_COLOR = 0;
    public static int PRIMARY_COLOR = 1;

    private Intent mSettingsIntent;
    private AppCompatDelegate mDelegate;
    private SharedPreferences mSharedPreferences;
    SharedPreferences.OnSharedPreferenceChangeListener mSharedPreferenceChangeListener;
    private boolean mDarkTheme, mColorNavBar;
    private int mAccentColor, mPrimaryColor, mPrimaryDarkColor;
    private int mColorIndex;
    private int mOldColorIndex;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mDarkTheme = mSharedPreferences.getBoolean("prefDarkTheme", true);

        mColorNavBar = mSharedPreferences.getBoolean("prefColorNavBar", false);
        mAccentColor = mSharedPreferences.getInt("prefAccentColor", accentColors()[0]);
        mPrimaryColor = mSharedPreferences.getInt("prefPrimaryColor", primaryColors()[0]);
        mPrimaryDarkColor = mSharedPreferences.getInt("prefPrimaryDarkColor", primaryDarkColors()[0]);

        assignColorIndex();
        mOldColorIndex = mColorIndex;

        setTheme(mAccentColor);
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);

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
        Preference defaultThemePreference = findPreference("prefDefaultTheme");
        Preference colorNavBarPreference = findPreference("prefColorNavBar");

        mSettingsIntent = new Intent(this, Settings.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            colorNavBarPreference.setEnabled(true);
            colorNavBarPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    reloadActivity();

                    return true;
                }
            });
        }else{
            colorNavBarPreference.setEnabled(false);
        }

        defaultThemePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                mAccentColor = accentColors()[1];
                mPrimaryColor = primaryColors()[0];
                mPrimaryDarkColor = primaryDarkColors()[0];

                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt("prefPrimaryDarkColor", mPrimaryDarkColor);
                editor.putInt("prefAccentColor", mAccentColor);
                editor.putInt("prefPrimaryColor", mPrimaryColor);
                editor.apply();

                reloadActivity();
                return true;
            }
        });

        darkThemePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                mDarkTheme = !mDarkTheme;

                accentColors();
                assignColorIndex();

                mAccentColor = accentColors()[mOldColorIndex];

                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt("prefAccentColor", mAccentColor);
                editor.putBoolean("prefDarkTheme", mDarkTheme);
                editor.apply();

                reloadActivity();

                return true;
            }
        });

        accentPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                colorDialog(ACCENT_COLOR);
                return true;
            }
        });

        primaryPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                colorDialog(PRIMARY_COLOR);
                return true;
            }
        });

    }

    private void colorDialog(final int type){

        final View dialogView;

        LayoutInflater inflter = LayoutInflater.from(getBaseContext());
        final AlertDialog alertDialog;
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        dialogView = inflter.inflate(R.layout.color_fragment, null);

        assignColorIndex();

        final GridView gridView = (GridView)dialogView.findViewById(R.id.gridView);

        final GridViewAdapter gridViewAdapter = new GridViewAdapter(this, mColorIndex
                , type == ACCENT_COLOR ? accentColors() : primaryColors()
                , type == ACCENT_COLOR ? rawAccentColors() : primaryDarkColors()
                , type);

        gridView.setAdapter(gridViewAdapter);

        dialogBuilder.setNeutralButton(R.string._default, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();

                if (type == ACCENT_COLOR) {
                    mAccentColor = accentColors()[1];
                    editor.putInt("prefAccentColor", mAccentColor);
                }else{
                    mPrimaryColor = primaryColors()[0];
                    mPrimaryDarkColor = primaryDarkColors()[0];
                    editor.putInt("prefPrimaryColor", mPrimaryColor);
                    editor.putInt("prefPrimaryDarkColor", mPrimaryDarkColor);
                }

                editor.apply();

                assignColorIndex();
                reloadActivity();
            }
        });

        dialogBuilder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                assignColorIndex();

                reloadActivity();
            }

        });

        dialogBuilder.setView(dialogView);

        alertDialog = dialogBuilder.create();


        alertDialog.show();

    }

    private void reloadActivity(){
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    private int[] primaryColors(){
        return new int[]{
                getResources().getColor(R.color.primaryIndigo),
                getResources().getColor(R.color.primaryRed),
                getResources().getColor(R.color.primaryPurple),
                getResources().getColor(R.color.primaryTeal),
                getResources().getColor(R.color.primaryOrange),
                getResources().getColor(R.color.primaryGrey),
                getResources().getColor(R.color.primaryWhite),
                getResources().getColor(R.color.primaryBlue)

        };
    }

    private int[] primaryDarkColors(){
        return new int[]{
                getResources().getColor(R.color.primaryIndigoDark),
                getResources().getColor(R.color.primaryRedDark),
                getResources().getColor(R.color.primaryPurpleDark),
                getResources().getColor(R.color.primaryTealDark),
                getResources().getColor(R.color.primaryOrangeDark),
                getResources().getColor(R.color.primaryGreyDark),
                getResources().getColor(R.color.primaryWhiteDark),
                getResources().getColor(R.color.primaryBlueDark)

        };
    }

    private void setSupportActionBar(@Nullable Toolbar toolbar) {
        getDelegate().setSupportActionBar(toolbar);
    }

    private void getSupportActionBar() {
        getDelegate().getSupportActionBar();
    }

    private void assignColorIndex(){
        mColorIndex = 1;
        for (int i = 0; i < accentColors().length; i++){
            if (mAccentColor == accentColors()[i]){
                mColorIndex = i;
            }
        }
    }

    private int[] rawAccentColors(){
        return new int[] {
                getResources().getColor(R.color.accentGrey),
                getResources().getColor(R.color.accentPink),
                getResources().getColor(R.color.accentYellow),
                getResources().getColor(R.color.accentGreen),
                getResources().getColor(R.color.accentRed),
                getResources().getColor(R.color.accentPurple),
                getResources().getColor(R.color.accentOrange),
                getResources().getColor(R.color.accentBlue)

        };
    }

    private int[] accentColors(){
        if (mDarkTheme) {
            return new int[]{
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
            return new int[]{
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
