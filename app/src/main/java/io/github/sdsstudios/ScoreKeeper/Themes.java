package io.github.sdsstudios.ScoreKeeper;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;

import com.google.android.gms.ads.AdView;

import io.github.sdsstudios.ScoreKeeper.Adapters.ColorGridViewAdapter;

public class Themes extends PreferenceActivity{

    public final static int ACCENT_COLORS = 1;
    public final static int PRIMARY_COLORS = 2;

    public final static int DEFAULT_ACCENT_COLOR = R.style.AppTheme_Red;
    public final static boolean DEFAULT_COLOR_NAV_BAR = true;

    private Intent mSettingsIntent;
    private AppCompatDelegate mDelegate;
    private SharedPreferences mSharedPreferences;
    private boolean mDarkTheme;
    private int mAccentColor, mPrimaryColor, mPrimaryDarkColor;

    public static void themeActivity(AppCompatActivity activity, int layout, boolean backButton) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

        int accentColor = sharedPreferences.getInt("prefAccentColor", DEFAULT_ACCENT_COLOR);
        int primaryColor = sharedPreferences.getInt("prefPrimaryColor", DEFAULT_PRIMARY_COLOR(activity));
        int primaryDarkColor = sharedPreferences.getInt("prefPrimaryDarkColor"
                , DEFAULT_PRIMARY_DARK_COLOR(activity));

        boolean colorNavBar = sharedPreferences.getBoolean("prefColorNavBar", DEFAULT_COLOR_NAV_BAR);

        activity.setTheme(accentColor);
        activity.setContentView(layout);

        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);

        if (toolbar != null) {
            activity.getSupportActionBar();
            toolbar.setBackgroundColor(primaryColor);
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(backButton);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            activity.getWindow().setStatusBarColor(primaryDarkColor);

            if (colorNavBar) {
                activity.getWindow().setNavigationBarColor(primaryDarkColor);
            }

            Bitmap bm = BitmapFactory.decodeResource(activity.getResources(), R.mipmap.ic_launcher);
            ActivityManager.TaskDescription taskDesc;

            taskDesc = new ActivityManager.TaskDescription(activity.getString(R.string.app_name), bm
                    , primaryDarkColor);

            activity.setTaskDescription(taskDesc);

        }

    }

    /**
     * DEFAULT COLORS
     **/

    public static int DEFAULT_PRIMARY_COLOR(Context ctx) {
        return ctx.getResources().getColor(R.color.primaryBlue);
    }

    public static int DEFAULT_PRIMARY_DARK_COLOR(Context ctx) {
        return ctx.getResources().getColor(R.color.primaryBlueDark);
    }

    public static int[] PRIMARY_COLORS(Context ctx) {
        return new int[]{
                ctx.getResources().getColor(R.color.primaryIndigo),
                ctx.getResources().getColor(R.color.primaryRed),
                ctx.getResources().getColor(R.color.primaryPurple),
                ctx.getResources().getColor(R.color.primaryTeal),
                ctx.getResources().getColor(R.color.primaryOrange),
                ctx.getResources().getColor(R.color.primaryGrey),
                ctx.getResources().getColor(R.color.primaryWhite),
                ctx.getResources().getColor(R.color.primaryBlue)

        };
    }

    public static int[] PRIMARY_DARK_COLORS(Context ctx) {
        return new int[]{
                ctx.getResources().getColor(R.color.primaryIndigoDark),
                ctx.getResources().getColor(R.color.primaryRedDark),
                ctx.getResources().getColor(R.color.primaryPurpleDark),
                ctx.getResources().getColor(R.color.primaryTealDark),
                ctx.getResources().getColor(R.color.primaryOrangeDark),
                ctx.getResources().getColor(R.color.primaryGreyDark),
                ctx.getResources().getColor(R.color.primaryWhiteDark),
                ctx.getResources().getColor(R.color.primaryBlueDark)

        };
    }

    public static int[] RAW_ACCENT_COLORS(Context ctx) {
        return new int[]{
                ctx.getResources().getColor(R.color.accentGrey),
                ctx.getResources().getColor(R.color.accentPink),
                ctx.getResources().getColor(R.color.accentYellow),
                ctx.getResources().getColor(R.color.accentGreen),
                ctx.getResources().getColor(R.color.accentRed),
                ctx.getResources().getColor(R.color.accentPurple),
                ctx.getResources().getColor(R.color.accentOrange),
                ctx.getResources().getColor(R.color.accentBlue)

        };
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mDarkTheme = mSharedPreferences.getBoolean("prefDarkTheme", false);

        boolean colorNavBar = mSharedPreferences.getBoolean("prefColorNavBar", Themes.DEFAULT_COLOR_NAV_BAR);
        mAccentColor = mSharedPreferences.getInt("prefAccentColor", DEFAULT_ACCENT_COLOR);
        mPrimaryColor = mSharedPreferences.getInt("prefPrimaryColor", DEFAULT_PRIMARY_COLOR(this));
        mPrimaryDarkColor = mSharedPreferences.getInt("prefPrimaryDarkColor"
                , DEFAULT_PRIMARY_DARK_COLOR(this));


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

            if (colorNavBar){
                getWindow().setNavigationBarColor(mPrimaryDarkColor);
            }
            getWindow().setStatusBarColor(mPrimaryDarkColor);

            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            ActivityManager.TaskDescription taskDesc;

            taskDesc = new ActivityManager.TaskDescription(getString(R.string.app_name), bm
                    , mPrimaryDarkColor);

            setTaskDescription(taskDesc);

        }

        addPreferencesFromResource(R.xml.theme_settings);

        Preference darkThemePreference = findPreference("prefDarkTheme");
        Preference accentPreference = findPreference("prefAccentColor");
        Preference primaryPreference = findPreference("prefPrimaryColor");
        Preference defaultThemePreference = findPreference("prefDefaultTheme");
        Preference colorNavBarPreference = findPreference("prefColorNavBar");

        colorNavBarPreference.setEnabled(deviceHasSoftwareKeys(getResources()));

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

                mAccentColor = DEFAULT_ACCENT_COLOR;
                mPrimaryColor = DEFAULT_PRIMARY_COLOR(Themes.this);
                mPrimaryDarkColor = DEFAULT_PRIMARY_DARK_COLOR(Themes.this);

                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt("prefPrimaryDarkColor", mPrimaryDarkColor);
                editor.putInt("prefAccentColor", mAccentColor);
                editor.putInt("prefPrimaryColor", mPrimaryColor);
                editor.putBoolean("prefDarkTheme", false);
                editor.apply();

                reloadActivity();

                return true;
            }
        });

        darkThemePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                final int oldColorIndex = accentColorIndex();

                mDarkTheme = !mDarkTheme;

                mAccentColor = accentThemes()[oldColorIndex];

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
                colorDialog(ACCENT_COLORS);
                return true;
            }
        });

        primaryPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                colorDialog(PRIMARY_COLORS);
                return true;
            }
        });

    }

    private int[] accentThemes(){
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

    public boolean deviceHasSoftwareKeys(Resources resources) {
        int id = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        return id > 0 && resources.getBoolean(id);
    }

    private void colorDialog(final int type){

        final View dialogView;

        LayoutInflater inflter = LayoutInflater.from(getBaseContext());

        final AlertDialog alertDialog;
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        dialogView = inflter.inflate(R.layout.color_fragment, null);

        final GridView gridView = (GridView)dialogView.findViewById(R.id.gridView);

        final ColorGridViewAdapter colorGridViewAdapter = new ColorGridViewAdapter(this
                , type == ACCENT_COLORS ? accentColorIndex() : primaryColorIndex()
                , type == ACCENT_COLORS ? accentThemes() : PRIMARY_DARK_COLORS(Themes.this)
                , type == ACCENT_COLORS ? RAW_ACCENT_COLORS(Themes.this) : PRIMARY_COLORS(Themes.this)
                , type);

        gridView.setAdapter(colorGridViewAdapter);

        dialogBuilder.setNeutralButton(R.string._default, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();

                if (type == ACCENT_COLORS) {
                    mAccentColor = DEFAULT_ACCENT_COLOR;
                    editor.putInt("prefAccentColor", mAccentColor);
                }else{
                    mPrimaryColor = DEFAULT_PRIMARY_COLOR(Themes.this);
                    mPrimaryDarkColor = DEFAULT_PRIMARY_DARK_COLOR(Themes.this);
                    editor.putInt("prefPrimaryColor", mPrimaryColor);
                    editor.putInt("prefPrimaryDarkColor", mPrimaryDarkColor);
                }

                editor.apply();

                reloadActivity();
            }
        });

        dialogBuilder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
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

    private void setSupportActionBar(@Nullable Toolbar toolbar) {
        getDelegate().setSupportActionBar(toolbar);
    }

    private int primaryColorIndex(){
        int index = 0;
        for (int i = 0; i < PRIMARY_COLORS(this).length; i++){
            if (mPrimaryColor == PRIMARY_COLORS(this)[i]){
                index = i;
            }
        }

        return index;
    }

    private int accentColorIndex(){
        int index = 0;
        for (int i = 0; i < accentThemes().length; i++){
            if (mAccentColor == accentThemes()[i]){
                index = i;
                break;
            }
        }

        return index;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
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

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
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
