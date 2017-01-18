package io.github.sdsstudios.ScoreKeeper;

import android.os.Bundle;

import io.github.sdsstudios.ScoreKeeper.Activity.Activity;
import io.github.sdsstudios.ScoreKeeper.Activity.ScoreKeeperActivity;

public class PlayersActivity extends ScoreKeeperActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Themes.themeActivity(this, R.layout.activity_recyclerview, true);
    }

    @Override
    public Activity getActivity() {
        return null;
    }

}
