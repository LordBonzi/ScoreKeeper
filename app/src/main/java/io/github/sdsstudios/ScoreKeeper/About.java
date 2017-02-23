package io.github.sdsstudios.ScoreKeeper;

import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;

import io.github.sdsstudios.ScoreKeeper.Helper.DialogHelper;

import static io.github.sdsstudios.ScoreKeeper.FileReader.CHANGELOG;
import static io.github.sdsstudios.ScoreKeeper.FileReader.LICENSE;

public class About extends PreferenceActivity {

    private AppCompatDelegate mDelegate;
    private Intent mHomeIntent;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        int accentColor = sharedPreferences.getInt("prefAccentColor", Themes.DEFAULT_ACCENT_COLOR);

        boolean colorNavBar = sharedPreferences.getBoolean("prefColorNavBar", Themes.DEFAULT_COLOR_NAV_BAR);
        int primaryColor = sharedPreferences.getInt("prefPrimaryColor", Themes.DEFAULT_PRIMARY_COLOR(this));
        int primaryDarkColor = sharedPreferences.getInt("prefPrimaryDarkColor"
                , Themes.DEFAULT_PRIMARY_DARK_COLOR(this));

        setTheme(accentColor);
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(primaryColor);
        getDelegate().setSupportActionBar(toolbar);
        getDelegate().getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            if (colorNavBar) {
                getWindow().setNavigationBarColor(primaryDarkColor);
            }
            getWindow().setStatusBarColor(primaryDarkColor);

            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            ActivityManager.TaskDescription taskDesc;

            taskDesc = new ActivityManager.TaskDescription(getString(R.string.app_name), bm
                    , primaryDarkColor);

            setTaskDescription(taskDesc);

        }

        AdView mAdView = (AdView) findViewById(R.id.adViewHome);
        AdCreator adCreator = new AdCreator(mAdView, this);
        adCreator.createAd();

        addPreferencesFromResource(R.xml.about_settings);
        Preference changeLogPreference = findPreference("prefChangelog");
        Preference developersPreference = findPreference("prefDevelopers");
        Preference translatorsPreference = findPreference("prefTranslators");
        Preference communityPreference = findPreference("prefCommunity");
        Preference ratePreference = findPreference("prefRate");
        Preference githubPreference = findPreference("prefGithub");
        Preference licensePreference = findPreference("prefLicense");

        mHomeIntent = new Intent(this, Home.class);

        translatorsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(About.this, R.string.no_translaters, Toast.LENGTH_SHORT).show();

                return true;
            }
        });

        communityPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/communities/102846051932575311974"));
                startActivity(browserIntent);
                return true;
            }
        });

        githubPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/SDS-Studios/ScoreKeeper"));
                startActivity(browserIntent);
                return true;
            }
        });

        licensePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogHelper.textViewAlertDialog(About.this, FileReader.textFromFileToString(LICENSE, getBaseContext()), getString(R.string.opensourcelicenses));

                return true;
            }
        });

        changeLogPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogHelper.textViewAlertDialog(About.this, FileReader.textFromFileToString(CHANGELOG, getBaseContext()), getString(R.string.changelog));
                return true;
            }
        });

        developersPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                final View dialogView;

                LayoutInflater inflter = LayoutInflater.from(getBaseContext());
                final AlertDialog alertDialog;
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(About.this);
                dialogView = inflter.inflate(R.layout.developers_fragment, null);
                ImageButton sethGoogle = (ImageButton) dialogView.findViewById(R.id.sethGoogleButton);
                Button sethGithub = (Button) dialogView.findViewById(R.id.sethGithubButton);
                Button sethEmail = (Button) dialogView.findViewById(R.id.sethEmailButton);

                sethGoogle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/118423554132509773191"));
                        startActivity(browserIntent);
                    }
                });

                sethGithub.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/orgs/SDS-Studios"));
                        startActivity(browserIntent);
                    }
                });

                sethEmail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent send = new Intent(Intent.ACTION_SENDTO);
                        String uriText = "mailto:" + Uri.encode("seth.d.schroeder@gmail.com") +
                                "?subject=" + Uri.encode("Feedback for Score Keeper app") +
                                "&body=" + Uri.encode("");
                        Uri uri = Uri.parse(uriText);

                        send.setData(uri);
                        startActivity(Intent.createChooser(send, "Send mail..."));
                        startActivity(send);
                    }
                });

                dialogBuilder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }

                });

                dialogBuilder.setView(dialogView);
                alertDialog = dialogBuilder.create();
                alertDialog.show();

                return true;
            }
        });


        ratePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=io.github.sdsstudios.ScoreKeeper"));
                startActivity(browserIntent);
                return true;
            }
        });

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
        if (id == android.R.id.home) {
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
        startActivity(mHomeIntent);
    }

}
