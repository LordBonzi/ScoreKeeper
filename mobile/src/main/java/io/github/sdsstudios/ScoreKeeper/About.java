package io.github.sdsstudios.ScoreKeeper;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

public class About extends AppCompatActivity
        implements View.OnClickListener {
    Button buttonAbout;
    Intent url;
    String url_str = "https://github.com/SDS-Studios/ScoreKeeper";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        url = new Intent(Intent.ACTION_VIEW);
        url.setData(Uri.parse(url_str));
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
