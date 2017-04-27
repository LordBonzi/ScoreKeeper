package io.github.sdsstudios.ScoreKeeper;

import android.os.Bundle;

import io.github.sdsstudios.ScoreKeeper.Activity.Activity;
import io.github.sdsstudios.ScoreKeeper.Activity.OptionActivity;

public class PlayersActivity extends OptionActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Themes.themeActivity(this, R.layout.activity_recyclerview, true);

    }

    @Override
    protected boolean inEditableMode() {
        return true;
    }

    @Override
    public Activity getActivity() {
        return null;
    }

    @Override
    public void onDialogDismissed() {

    }

}
