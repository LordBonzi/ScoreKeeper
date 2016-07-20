package io.github.sdsstudios.ScoreKeeper;

import java.util.ArrayList;

/**
 * Created by seth on 12/07/16.
 */

public interface PresetListener {
    void updateEditText(ArrayList players, String timeLimit, int maxscore, int reversescrolling, int scoreInterval);
}
