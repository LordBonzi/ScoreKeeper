package io.github.sdsstudios.ScoreKeeper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.github.sdsstudios.ScoreKeeper.Listeners.GameListener;
import io.github.sdsstudios.ScoreKeeper.Options.CheckBoxOption;
import io.github.sdsstudios.ScoreKeeper.Options.IntEditTextOption;
import io.github.sdsstudios.ScoreKeeper.Options.Option.OptionID;
import io.github.sdsstudios.ScoreKeeper.Options.StringEditTextOption;

/**
 * Created by Seth on 06/10/2016.
 */

public class Game {

    private static String TAG = "Game";
    private GameListener mGameListener;

    private List<Player> mPlayerArray;
    private TimeLimit mTimeLimit;
    private boolean mCompleted;
    private int mID;
    private int mNumSetsPlayed;
    private int mScoreIntervalIndex;

    private List<IntEditTextOption> mIntEditTextOptions;
    private List<StringEditTextOption> mStringEditTextOptions;
    private List<CheckBoxOption> mCheckBoxOptions;

    public Game(List<Player> mPlayerArray, TimeLimit mTimeLimit, boolean mCompleted, int mID
            , List<IntEditTextOption> intEditTextOptions, List<CheckBoxOption> checkBoxOptions, List<StringEditTextOption> stringEditTextOptions,
                GameListener mGameListener) {

        this.mPlayerArray = mPlayerArray;
        this.mTimeLimit = mTimeLimit;
        this.mID = mID;
        this.mCompleted = mCompleted;
        this.mCheckBoxOptions = checkBoxOptions;
        this.mIntEditTextOptions = intEditTextOptions;
        this.mStringEditTextOptions = stringEditTextOptions;
        this.mGameListener = mGameListener;

    }

    public boolean ismCompleted() {
        return mCompleted;
    }

    public void setmCompleted(boolean mCompleted) {
        this.mCompleted = mCompleted;
    }

    public String getmTime() {
        return getString(OptionID.DATE);
    }

    public void setmTime(String mTime) {
        setString(OptionID.DATE, mTime);
    }

    public String getmLength() {
        return getString(OptionID.LENGTH);
    }

    public void setmLength(String mLength) {
        setString(OptionID.LENGTH, mLength);
    }

    public int getmID() {
        return mID;
    }

    public void setmID(int mID) {
        this.mID = mID;
    }

    public List<Player> getmPlayerArray() {
        return mPlayerArray;
    }

    public void setmPlayerArray(List<Player> mPlayerArray) {
        this.mPlayerArray = mPlayerArray;
    }

    public TimeLimit getmTimeLimit() {
        return mTimeLimit;
    }

    public void setmTimeLimit(TimeLimit mTimeLimit) {
        this.mTimeLimit = mTimeLimit;

    }

    public String getmTitle() {
        return getString(OptionID.TITLE);
    }

    public void setmTitle(String mTitle) {
        setString(OptionID.TITLE, mTitle);
    }

    public void setPlayer(Player player, int index){
        mPlayerArray.set(index, player);
    }

    public void setPlayerName(String name, int index){
        mPlayerArray.get(index).setmName(name);
    }

    public int size(){
        return mPlayerArray.size();
    }

    public int numSetsPlayed(){
        return mNumSetsPlayed;
    }

    public void startNewSet() {
        for (Player p : getmPlayerArray()) {
            p.addSet(getInt(OptionID.STARTING_SCORE));
        }

        mNumSetsPlayed += 1;
    }

    public int getInt(OptionID id) {
        int data = 1;

        for (int i = 0; i < mIntEditTextOptions.size(); i++) {
            IntEditTextOption e = mIntEditTextOptions.get(i);

            if (e.getmID() == id){
                data = Integer.valueOf(String.valueOf(e.getInt()));
                break;
            } else if (e.getmID() == OptionID.SCORE_INTERVAL) {
                /** update score interval to quickly get scoreinterval when changing score **/
                mScoreIntervalIndex = i;
            }
        }

        return data;
    }

    public void onPlayerClick(int playerIndex) {
        getPlayer(playerIndex).playerClick(mIntEditTextOptions.get(mScoreIntervalIndex).getInt()
                , isChecked(OptionID.REVERSE_SCORING));


    }

    public void onPlayerLongClick(int playerIndex) {
        getPlayer(playerIndex).playerLongClick(mIntEditTextOptions.get(mScoreIntervalIndex).getInt()
                , isChecked(OptionID.REVERSE_SCORING));
    }

    public String getString(OptionID id) {
        String data = "";

        for (StringEditTextOption e: mStringEditTextOptions){
            if (e.getmID() == id ){
                data = e.getString();
                break;
            }
        }

        return data;
    }

    public List<CheckBoxOption> getmCheckBoxOptions() {
        return mCheckBoxOptions;
    }

    public void setmCheckBoxOptions(List<CheckBoxOption> mCheckBoxOptions) {
        this.mCheckBoxOptions = mCheckBoxOptions;
    }

    public int numSets(){
        return getInt(OptionID.NUMBER_SETS);
    }

    public boolean isChecked(OptionID id) {
        boolean isChecked = false;
        for (CheckBoxOption c : mCheckBoxOptions) {
            if (c.getmID() == id) {
                isChecked = c.isChecked();
                break;
            }

        }
        return isChecked;
    }

    public void setChecked(OptionID id, boolean data) {
        for (CheckBoxOption s : mCheckBoxOptions) {
            if (s.getmID() == id) {
                s.setData(data);
            }
        }
    }

    public List<IntEditTextOption> getmIntEditTextOptions() {
        return mIntEditTextOptions;
    }

    public void setmIntEditTextOption(List<IntEditTextOption> mIntEditTextOptions) {
        this.mIntEditTextOptions = mIntEditTextOptions;
    }

    public List<StringEditTextOption> getmStringEditTextOptions() {
        return mStringEditTextOptions;
    }

    public void setmStringEditTextOptions(List<StringEditTextOption> mStringEditTextOptions) {
        this.mStringEditTextOptions = mStringEditTextOptions;
    }

    public void setString(OptionID id, String data) {
        for (StringEditTextOption s : mStringEditTextOptions) {
            if (s.getmID() == id) {
                s.setData(data);
            }
        }
    }

    public void addPlayerAtPosition(Player player, int position) {
        mPlayerArray.add(position, player);
    }

    public void addNewPlayer(Player player) {

        /** fill player set array with 0s before the score specified in the dialog in GameActivity
         * so the score displayed after adding the player is the score specified
         */

        if (size() > 0) {
            for (int i = 0; i < mPlayerArray.get(0).getNumSetsPlayed() - 1; i++) {
                player.addSetAtPosition(i, 0);
            }
        }

        mPlayerArray.add(player);
    }

    public void removePlayer(int position){
        mPlayerArray.remove(position);
    }

    public Player getPlayer(int position){
        return mPlayerArray.get(position);
    }

    public String getWinnerString(){
        Player winningPlayer = mPlayerArray.get(0);

        for (Player p : mPlayerArray) {
            if (numSetsPlayed() == numSets()) {
                if (p.overallScore() > winningPlayer.overallScore()) {
                    winningPlayer = p;
                }
            }else{
                if (p.getmScore() > winningPlayer.getmScore()) {
                    winningPlayer = p;
                }
            }
        }

        return winningPlayer.getmName();
    }

    public boolean isGameWon() {

        int maxScore = getInt(OptionID.WINNING_SCORE);

        boolean isWon = false;

        for (int i = 0; i < size(); i++) {

            Player p = mPlayerArray.get(i);

            if (maxScore != 0) {
                if (maxScore < 0) {
                    if (p.getmScore() <= maxScore && scoreDifference()) {
                        mGameListener.onGameWon(getWinnerString());
                        isWon = true;
                        break;
                    }

                } else if (maxScore >= 0) {

                    if (p.getmScore() >= maxScore && scoreDifference()) {
                        mGameListener.onGameWon(getWinnerString());
                        isWon = true;
                        break;
                    }

                }

            }

        }

        return isWon;

    }

    private List<Integer> sortedListOfScores() {
        List<Integer> sortedList = getListOfScores();

        Collections.sort(sortedList, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                if (isChecked(OptionID.REVERSE_SCORING)) {
                    return o1 + o2;
                } else {
                    return o1 - o2;
                }
            }
        });

        return sortedList;
    }

    public int scorePosition(int score) {
        return sortedListOfScores().indexOf(score);
    }

    public int largestScoreIndex(List<Integer> mScoreList) {
        return mScoreList.indexOf(Collections.max(mScoreList));
    }

    public List<Integer> getListOfScores() {
        List<Integer> scoreList = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            scoreList.add(mPlayerArray.get(i).getmScore());

        }

        return scoreList;
    }

    private boolean scoreDifference() {
        boolean b = false;
        int maxScore = getInt(OptionID.WINNING_SCORE);

        if (maxScore != 0) {
            List<Integer> scoreListWithoutLargestScore = getListOfScores();
            scoreListWithoutLargestScore.remove(largestScoreIndex(scoreListWithoutLargestScore));

            int largestScore = (maxScore > 0) ? Collections.max(getListOfScores())
                    : Collections.min(getListOfScores());

            int secondLargestScore = (maxScore > 0) ? Collections.max(scoreListWithoutLargestScore)
                    : Collections.min(scoreListWithoutLargestScore);

            if (largestScore - secondLargestScore >= getInt(OptionID.SCORE_DIFF_TO_WIN)) {
                b = true;
            }
        }

        return b;
    }

    public void setGameListener(GameListener listener) {
        mGameListener = listener;
    }

    public GameListener getmGameListener() {
        return mGameListener;
    }

}


