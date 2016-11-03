package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class Settings extends PreferenceActivity{
    private ScoreDBAdapter dbHelper;
    private Intent homeIntent;
    private FirebaseAnalytics mFirebaseAnalytics;
    private AppCompatDelegate mDelegate;
    private Preference deletePreference, timeLimitPreference, numGamesPreference
            , themesPreference, maxNumOnDicePreference, exportPreference, importPreference, mCreateGamesPreference, mDeleteAllPresets;
    SharedPreferences.OnSharedPreferenceChangeListener listener;
    AlertDialog dialog;
    private DataHelper dataHelper;
    private boolean colorise;
    private String numGamesToShow;
    private SharedPreferences sharedPreferences;
    private int maxNumDice;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
        maxNumDice = sharedPreferences.getInt("maxNumDice", 6);
        int accentColor = sharedPreferences.getInt("prefAccent", R.style.AppTheme);
        int primaryColor = sharedPreferences.getInt("prefPrimaryColor", getResources().getColor(R.color.primaryIndigo));
        int primaryDarkColor = sharedPreferences.getInt("prefPrimaryDarkColor", getResources().getColor(R.color.primaryIndigoDark));
        boolean colorNavBar = sharedPreferences.getBoolean("prefColorNavBar", false);

        if (colorNavBar) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setNavigationBarColor(primaryDarkColor);
            }
        }
        setTheme(accentColor);
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        AdView mAdView = (AdView) findViewById(R.id.adViewHome);
        AdCreator adCreator = new AdCreator(mAdView, this);
        adCreator.createAd();
        addPreferencesFromResource(R.xml.content_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(primaryColor);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getDelegate().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(primaryDarkColor);
        }

        dataHelper = new DataHelper();

        deletePreference = findPreference("prefDeleteAllGames");
        timeLimitPreference = findPreference("prefDeleteTimeLimit");
        numGamesPreference = findPreference("prefNumGames");
        themesPreference = findPreference("prefThemes");
        maxNumOnDicePreference = findPreference("prefDiceMaxNum");
        exportPreference = findPreference("prefExport");
        importPreference = findPreference("prefImport");
        mCreateGamesPreference = findPreference("prefCreateGames");
        mDeleteAllPresets = findPreference("prefDeleteAllPresets");

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        exportPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                builder.setTitle(getResources().getString(R.string.export_games));

                builder.setMessage(R.string.export_games_message);

                builder.setPositiveButton(R.string.export, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            exportDatabase();
                            Toast.makeText(Settings.this, "Successfully exported games to /ScoreKeeper", Toast.LENGTH_SHORT).show();
                        }catch (Exception e){
                            Toast.makeText(Settings.this, e.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

                dialog = builder.create();
                dialog.show();                return true;
            }

        });

        importPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                builder.setTitle(getResources().getString(R.string.import_games));

                builder.setMessage(R.string.import_games_message);

                builder.setPositiveButton(R.string._import, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            importDatabase();
                            Toast.makeText(Settings.this, "Successfully imported games", Toast.LENGTH_SHORT).show();
                        }catch (IOException e){
                            Toast.makeText(Settings.this, "Is the database in /ScoreKeeper folder?", Toast.LENGTH_SHORT).show();
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

        maxNumOnDicePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                maxNumDice = Integer.valueOf(String.valueOf(o));
                saveInfo();
                return true;
            }
        });

        deletePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                builder.setTitle(getResources().getString(R.string.delete_all_games) + "?");

                builder.setMessage(R.string.delete_all_games_mes);

                builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            dbHelper.open().deleteAllgames();
                            Toast.makeText(Settings.this, "Successfully deleted games", Toast.LENGTH_SHORT).show();
                        }catch (Exception e){
                            Toast.makeText(Settings.this, e.toString(), Toast.LENGTH_SHORT).show();
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

        mDeleteAllPresets.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                builder.setTitle(getResources().getString(R.string.delete_all_games) + "?");

                builder.setMessage(R.string.delete_all_games_mes);

                builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            PresetDBAdapter presetDBAdapter = new PresetDBAdapter(Settings.this);
                            presetDBAdapter.open().deleteAllPresets();
                            presetDBAdapter.close();
                            Toast.makeText(Settings.this, "Successfully deleted presets", Toast.LENGTH_SHORT).show();
                        }catch (Exception e){
                            e.printStackTrace();
                            Log.e("Settings", e.toString());
                            Toast.makeText(Settings.this, e.toString(), Toast.LENGTH_SHORT).show();
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

        themesPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Settings.this, ThemeSettings.class);
                startActivity(intent);
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

        homeIntent = new Intent(this, Home.class);

        //Shared prefs stuff

        numGamesToShow = sharedPreferences.getString("numgamestoshow", "3");

    }

    private void exportDatabase(){
        File sd = Environment.getExternalStorageDirectory();
        String DB_PATH;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DB_PATH = getFilesDir().getAbsolutePath().replace("files", "databases") + File.separator;
        }
        else {
            DB_PATH =  this.getDatabasePath("ScoreKeeper").toString();
        }

        if (sd.canWrite()) {
            String currentDBPath = "ScoreKeeper";
            String backupDBPath = "/ScoreKeeper/ScoreKeeper.db";
            File currentDB = new File(DB_PATH, currentDBPath);
            File backupDB = new File(sd, backupDBPath);

            try{
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();

            }catch (Exception e){
                e.printStackTrace();
                Log.e("Settings", e.toString());
            }
        }
    }

    private void importDatabase() throws IOException {
        File sd = Environment.getExternalStorageDirectory();
        String DB_PATH;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DB_PATH = getFilesDir().getAbsolutePath().replace("files", "databases") + File.separator;
        }
        else {
            DB_PATH =  this.getDatabasePath("ScoreKeeper").toString();
        }

        if (sd.canWrite()) {
            String currentDBPath = "ScoreKeeper";
            String backupDBPath = "/ScoreKeeper/ScoreKeeper.db";
            File currentDB = new File(sd, backupDBPath);
            File backupDB = new File(DB_PATH, currentDBPath);

            FileChannel src = new FileInputStream(currentDB).getChannel();
            FileChannel dst = new FileOutputStream(backupDB).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();


        }
    }

    private void setSupportActionBar(@Nullable Toolbar toolbar) {
        getDelegate().setSupportActionBar(toolbar);
    }

    private void getSupportActionBar() {
        getDelegate().getSupportActionBar();
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
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("numgamestoshow", numGamesToShow);
        editor.putInt("maxNumDice", maxNumDice);
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
        startActivity(homeIntent);
    }

}
