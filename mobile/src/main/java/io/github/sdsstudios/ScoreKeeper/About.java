package io.github.sdsstudios.ScoreKeeper;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class About extends AppCompatActivity implements View.OnClickListener {

    private Button buttonAbout;
    private Intent url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        url = new Intent(Intent.ACTION_VIEW);
        url.setData(Uri.parse(getResources().getString(R.string.git_link)));
        buttonAbout = (Button)findViewById(R.id.buttonInfo);
        buttonAbout.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonInfo:
                startActivity(url);

            break;


        }


    }
}
