package io.github.sdsstudios.ScoreKeeper;

import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
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
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Settings extends PreferenceActivity{
    private GameDBAdapter mDbHelper;
    private Intent mHomeIntent;
    private AppCompatDelegate mDelegate;
    private SharedPreferences.OnSharedPreferenceChangeListener mSharedPreferenceChangeListener;
    private AlertDialog mDialog;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        int accentColor = mSharedPreferences.getInt("prefAccentColor", Themes.DEFAULT_ACCENT_COLOR);
        int primaryColor = mSharedPreferences.getInt("prefPrimaryColor", Themes.DEFAULT_PRIMARY_COLOR(this));
        int primaryDarkColor = mSharedPreferences.getInt("prefPrimaryDarkColor"
                , Themes.DEFAULT_PRIMARY_DARK_COLOR(this));

        boolean colorNavBar = mSharedPreferences.getBoolean("prefColorNavBar", false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if (colorNavBar){
                getWindow().setNavigationBarColor(primaryDarkColor);
            }
            getWindow().setStatusBarColor(primaryDarkColor);

            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            ActivityManager.TaskDescription taskDesc;

            taskDesc = new ActivityManager.TaskDescription(getString(R.string.app_name), bm
                    , primaryDarkColor);

            setTaskDescription(taskDesc);

        }

        setTheme(accentColor);
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);

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

        Preference deletePreference = findPreference("prefDeleteAllGames");
        Preference themesPreference = findPreference("prefThemes");
        Preference exportPreference = findPreference("prefExport");
        Preference importPreference = findPreference("prefImport");
        Preference deleteAllPresets = findPreference("prefDeleteAllPresets");
        Preference notificationsPreference = findPreference("prefReceiveNotifications");
        Preference createGamesPref = findPreference("prefCreateGames");

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        createGamesPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                GameDBAdapter dbAdapter = new GameDBAdapter(Settings.this);
                SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
                Date now = new Date();
                String mTime = sdfDate.format(now);

                boolean lastBool = false;

                for (int i = 1; i <= 10; i++){

                    Game game = new Game(new ArrayList<Player>(), null, false, 0
                            , IntEditTextOption.loadEditTextOptions(Settings.this), CheckBoxOption.loadCheckBoxOptions()
                            , StringEditTextOption.loadEditTextOptions(Settings.this), null);

                    dbAdapter.open().createGame(game);

                    game.setmLength("00:00:00:0");
                    game.setmTime(mTime);
                    game.setmTitle("Debugging Game " + i);

                    game.setmID(dbAdapter.getNewestGame());

                    dbAdapter.updateGame(game);
                    dbAdapter.close();

                    lastBool = !lastBool;

                }

                return true;
            }
        });

        notificationsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (mSharedPreferences.getBoolean("prefReceiveNotifications", true)){
                    FirebaseMessaging.getInstance().subscribeToTopic("news");
                }else{
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("news");
                }
                return false;
            }
        });

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

                mDialog = builder.create();
                mDialog.show();                return true;
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
                            Toast.makeText(Settings.this, "Error. Is the database in /ScoreKeeper folder?", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

                mDialog = builder.create();
                mDialog.show();
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
                            mDbHelper.open().deleteAllGames();
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

                mDialog = builder.create();
                mDialog.show();
                return true;
            }
        });


        deleteAllPresets.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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

                mDialog = builder.create();
                mDialog.show();
                return true;
            }
        });

        themesPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Settings.this, Themes.class);
                startActivity(intent);
                return true;
            }
        });

        mDbHelper = new GameDBAdapter(this);

        mHomeIntent = new Intent(this, Home.class);
    }

    private void exportDatabase(){
        File sd = Environment.getExternalStorageDirectory();
        String DB_PATH;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            DB_PATH = getFilesDir().getAbsolutePath().replace("files", "databases") + File.separator;
        } else {
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
        startActivity(mHomeIntent);
    }

}
