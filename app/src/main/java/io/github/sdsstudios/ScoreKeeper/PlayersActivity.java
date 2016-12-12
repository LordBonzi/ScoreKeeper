package io.github.sdsstudios.ScoreKeeper;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class PlayersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Themes.themeActivity(this, R.layout.activity_recyclerview, true);
    }

}
