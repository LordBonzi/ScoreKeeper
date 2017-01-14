package io.github.sdsstudios.ScoreKeeper;

import android.os.Bundle;

public class PlayersActivity extends ScoreKeeperActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Themes.themeActivity(this, R.layout.activity_recyclerview, true);
    }

    @Override
    Activity getActivity() {
        return null;
    }

}
