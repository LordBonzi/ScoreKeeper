package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.google.firebase.crash.FirebaseCrash;

public class About extends AppCompatActivity implements View.OnClickListener {

    private Button buttonAbout;
    private Intent url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
        int accentColor = sharedPreferences.getInt("prefAccent", R.style.AppTheme);
        int primaryColor = sharedPreferences.getInt("prefPrimaryColor", getResources().getColor(R.color.primaryIndigo));
        int primaryDarkColor = sharedPreferences.getInt("prefPrimaryDarkColor", getResources().getColor(R.color.primaryIndigoDark));
        boolean colorNavBar = sharedPreferences.getBoolean("prefColorNavBar", false);
        if (colorNavBar){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setNavigationBarColor(primaryDarkColor);
            }
        }
        setTheme(accentColor);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(primaryColor);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(primaryDarkColor);
        }
        url = new Intent(Intent.ACTION_VIEW);
        url.setData(Uri.parse(getResources().getString(R.string.git_link)));
        buttonAbout = (Button)findViewById(R.id.buttonInfo);
        buttonAbout.setOnClickListener(this);

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable f) {
                f.printStackTrace();

                FirebaseCrash.report(new Exception(f.toString()));

            }
        });
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonInfo:
                startActivity(url);

            break;


        }


    }
}
