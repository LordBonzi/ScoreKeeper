package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.github.sdsstudios.ScoreKeeper.Listeners.GameListener;
import io.github.sdsstudios.ScoreKeeper.Options.CheckBoxOption;
import io.github.sdsstudios.ScoreKeeper.Options.IntEditTextOption;
import io.github.sdsstudios.ScoreKeeper.Options.Option;
import io.github.sdsstudios.ScoreKeeper.Options.StringEditTextOption;

/**
 * Created by Seth on 06/10/2016.
 */

public class Game {

    public static final int WINNING = 154;
    public static final int LOSING = 45;
    private static String TAG = "Game";
    private final String CHECK_BOX_OPTIONS = "check_box_options";
    private final String INT_EDIT_TEXT_OPTIONS = "int_edit_text_options";
    private final String STRING_EDIT_TEXT_OPTIONS = "string_edit_text_options";
    private List<Integer> mSortedScoreList;

    private GameListener mGameListener;

    private List<Player> mPlayerArray;
    private TimeLimit mTimeLimit;
    private boolean mCompleted;
    private int mID;
    private int mNumSetsPlayed;

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

    void setmCompleted(boolean mCompleted) {
        this.mCompleted = mCompleted;
    }

    public String getmTime() {
        return getString(Option.DATE);
    }

    public void setmTime(String mTime) {
        setString(Option.DATE, mTime);
    }

    String getmLength() {
        String length = getString(Option.LENGTH);
        if (length == null || length.equals("")) {
            return "00:00:00:0";
        } else {
            return getString(Option.LENGTH);
        }
    }

    void setmLength(String mLength) {
        setString(Option.LENGTH, mLength);
    }

    public int getmID() {
        return mID;
    }

    public void setmID(int mID) {
        this.mID = mID;
    }

    public void noTimeLimit() {
        mTimeLimit = TimeLimit.BLANK_TIME_LIMIT;
    }

    public List<Player> getmPlayerArray() {
        return mPlayerArray;
    }

    public void setmPlayerArray(List<Player> mPlayerArray) {
        this.mPlayerArray = mPlayerArray;
    }

    public TimeLimit getmTimeLimit() {
        if (mTimeLimit == null) {
            mTimeLimit = TimeLimit.BLANK_TIME_LIMIT;
        }

        return mTimeLimit;
    }

    void setmTimeLimit(TimeLimit mTimeLimit) {
        if (mTimeLimit == null) {
            this.mTimeLimit = TimeLimit.BLANK_TIME_LIMIT;
        } else {
            this.mTimeLimit = mTimeLimit;
        }
    }

    public String getmTitle() {
        return getString(Option.TITLE);
    }

    public void setmTitle(String mTitle) {
        setString(Option.TITLE, mTitle);
    }

    public void setPlayer(Player player, int index){
        mPlayerArray.set(index, player);
    }

    public StringEditTextOption getStringEditTextOption(String id) {
        StringEditTextOption editTextOption = null;
        for (StringEditTextOption e : mStringEditTextOptions) {
            if (e.getmID().equals(id)) {
                editTextOption = e;
                break;
            }
        }
        return editTextOption;
    }

    public IntEditTextOption getIntEditTextOption(String id) {
        IntEditTextOption editTextOption = null;

        for (IntEditTextOption e : mIntEditTextOptions) {
            if (e.getmID().equals(id)) {
                editTextOption = e;
                break;
            }
        }
        return editTextOption;
    }

    public void setPlayerName(String name, int index){
        mPlayerArray.get(index).setmName(name);
    }

    public int size(){
        return mPlayerArray.size();
    }

    protected int numSetsPlayed() {
        return mNumSetsPlayed;
    }

    void startNewSet() {
        for (Player p : getmPlayerArray()) {
            p.addSet(getInt(Option.STARTING_SCORE));
        }

        mNumSetsPlayed += 1;
    }

    public void reset() {
        for (Player player : mPlayerArray) {
            player.reset();
        }

        setmLength("00:00:00:0");

    }

    public int getInt(String id) {
        int data = 1;

        for (int i = 0; i < mIntEditTextOptions.size(); i++) {
            IntEditTextOption e = mIntEditTextOptions.get(i);

            if (e.getmID().equals(id)) {
                data = Integer.valueOf(String.valueOf(e.getInt()));
                break;
            }
        }

        return data;
    }

    public List<Integer> getmSortedScoreList() {
        return mSortedScoreList;
    }

    public void onPlayerClick(int playerIndex) {
        Player player = getPlayer(playerIndex);

        player.playerClick(getInt(Option.SCORE_INTERVAL)
                , isChecked(Option.REVERSE_SCORING));


    }


    public void onPlayerLongClick(int playerIndex) {
        Player player = getPlayer(playerIndex);

        player.playerLongClick(getInt(Option.SCORE_INTERVAL)
                , isChecked(Option.REVERSE_SCORING));

    }

    public String getString(String id) {
        String data = "";

        for (StringEditTextOption e: mStringEditTextOptions){
            if (e.getmID().equals(id)) {
                data = e.getString();
                break;
            }
        }

        return data;
    }


    private List<String> currentOptionIDs(String whichList) {
        List<String> list = new ArrayList<>();

        switch (whichList) {
            case CHECK_BOX_OPTIONS:
                for (CheckBoxOption option : mCheckBoxOptions) {
                    list.add(option.getmID());
                }
                break;

            case STRING_EDIT_TEXT_OPTIONS:
                for (StringEditTextOption option : mStringEditTextOptions) {
                    list.add(option.getmID());
                }
                break;

            case INT_EDIT_TEXT_OPTIONS:
                for (IntEditTextOption option : mIntEditTextOptions) {
                    list.add(option.getmID());
                }
                break;

        }
        return list;
    }

    private List<String> newOptionIDs(String whichList, Context ctx) {
        List<String> list = new ArrayList<>();

        switch (whichList) {
            case CHECK_BOX_OPTIONS:
                for (CheckBoxOption option : CheckBoxOption.loadCheckBoxOptions(ctx)) {
                    list.add(option.getmID());
                }
                break;

            case STRING_EDIT_TEXT_OPTIONS:
                for (StringEditTextOption option : StringEditTextOption.loadEditTextOptions(ctx)) {
                    list.add(option.getmID());
                }
                break;

            case INT_EDIT_TEXT_OPTIONS:
                for (IntEditTextOption option : IntEditTextOption.loadEditTextOptions(ctx)) {
                    list.add(option.getmID());
                }
                break;

        }
        return list;
    }

    public List<CheckBoxOption> getmCheckBoxOptions(Context ctx) {

        if (mCheckBoxOptions.size() != Option.CHECK_BOX_OPTIONS.length) {

            List<String> newOptionIDs = newOptionIDs(CHECK_BOX_OPTIONS, ctx);
            List<String> currentOptionIDs = currentOptionIDs(CHECK_BOX_OPTIONS);

            for (int i = 0; i < newOptionIDs.size(); i++) {
                if (!currentOptionIDs.contains(newOptionIDs.get(i))) {
                    mCheckBoxOptions.add(CheckBoxOption.loadCheckBoxOptions(ctx).get(i));
                }
            }
        }

        return mCheckBoxOptions;
    }

    void setmCheckBoxOptions(List<CheckBoxOption> mCheckBoxOptions) {
        this.mCheckBoxOptions = mCheckBoxOptions;
    }

    protected int numSets() {
        return getInt(Option.NUMBER_SETS);
    }

    public boolean isChecked(String id) {
        boolean isChecked = false;
        for (CheckBoxOption c : mCheckBoxOptions) {
            if (c.getmID().equals(id)) {
                isChecked = c.isChecked();
                break;
            }

        }
        return isChecked;
    }

    public CheckBoxOption getCheckBoxOption(String id) {
        CheckBoxOption checkBoxOption = null;

        for (CheckBoxOption c : mCheckBoxOptions) {
            if (c.getmID().equals(id)) {
                checkBoxOption = c;
                break;
            }
        }

        return checkBoxOption;
    }

    public void setChecked(String id, boolean data) {
        for (CheckBoxOption s : mCheckBoxOptions) {
            if (s.getmID().equals(id)) {
                s.setData(data);
                break;
            }
        }
    }

    public List<IntEditTextOption> getmIntEditTextOptions(Context ctx) {

        if (mIntEditTextOptions.size() != Option.INT_EDIT_TEXT_OPTIONS.length) {
            List<String> newOptionIDs = newOptionIDs(INT_EDIT_TEXT_OPTIONS, ctx);
            List<String> currentOptionIDs = currentOptionIDs(INT_EDIT_TEXT_OPTIONS);

            for (int i = 0; i < newOptionIDs.size(); i++) {
                if (!currentOptionIDs.contains(newOptionIDs.get(i))) {
                    mIntEditTextOptions.add(IntEditTextOption.loadEditTextOptions(ctx).get(i));
                }
            }
        }

        return mIntEditTextOptions;
    }

    void setmIntEditTextOptions(List<IntEditTextOption> mIntEditTextOptions) {
        this.mIntEditTextOptions = mIntEditTextOptions;
    }

    public List<StringEditTextOption> getmStringEditTextOptions(Context ctx) {

        if (mStringEditTextOptions.size() != Option.STRING_EDIT_TEXT_OPTIONS.length) {
            List<String> newOptionIDs = newOptionIDs(STRING_EDIT_TEXT_OPTIONS, ctx);
            List<String> currentOptionIDs = currentOptionIDs(STRING_EDIT_TEXT_OPTIONS);

            for (int i = 0; i < newOptionIDs.size(); i++) {
                if (!currentOptionIDs.contains(newOptionIDs.get(i))) {
                    mStringEditTextOptions.add(StringEditTextOption.loadEditTextOptions(ctx).get(i));
                }
            }
        }

        return mStringEditTextOptions;
    }

    void setmStringEditTextOptions(List<StringEditTextOption> mStringEditTextOptions) {
        this.mStringEditTextOptions = mStringEditTextOptions;
    }

    void setString(String id, String data) {
        for (StringEditTextOption s : mStringEditTextOptions) {
            if (s.getmID().equals(id)) {
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

    private String getWinnerString() {
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

        int maxScore = getInt(Option.WINNING_SCORE);

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

    private void sortScoreList() {

        mSortedScoreList = getListOfScores();

        Collections.sort(mSortedScoreList, new Comparator<Integer>() {
            @Override
            public int compare(Integer score1, Integer score2) {
                if (isChecked(Option.REVERSE_SCORING)) {
                    return score1 - score2;
                } else {
                    return score2 - score1;
                }

            }
        });

    }

    private int playerPosition(Player player) {
        if (mSortedScoreList == null) {
            /** creates a list with sorted scores **/
            sortScoreList();
        }

        if (mSortedScoreList.get(0) == player.getmScore()) {
            return WINNING;
        } else if (mSortedScoreList.get(mSortedScoreList.size() - 1) == player.getmScore()) {
            return LOSING;
        } else {
            return 0;
        }
    }

    private int largestScoreIndex(List<Integer> mScoreList) {
        return mScoreList.indexOf(Collections.max(mScoreList));
    }

    private List<Integer> getListOfScores() {
        List<Integer> scoreList = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            scoreList.add(mPlayerArray.get(i).getmScore());

        }

        return scoreList;
    }

    private boolean scoreDifference() {
        boolean b = false;
        int maxScore = getInt(Option.WINNING_SCORE);

        if (maxScore != 0) {
            List<Integer> scoreListWithoutLargestScore = getListOfScores();
            scoreListWithoutLargestScore.remove(largestScoreIndex(scoreListWithoutLargestScore));

            int largestScore = (maxScore > 0) ? Collections.max(getListOfScores())
                    : Collections.min(getListOfScores());

            int secondLargestScore = (maxScore > 0) ? Collections.max(scoreListWithoutLargestScore)
                    : Collections.min(scoreListWithoutLargestScore);

            if (largestScore - secondLargestScore >= getInt(Option.SCORE_DIFF_TO_WIN)) {
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


