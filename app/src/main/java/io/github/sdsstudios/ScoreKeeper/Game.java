package io.github.sdsstudios.ScoreKeeper;

import java.util.List;

import io.github.sdsstudios.ScoreKeeper.Option.OptionID;

/**
 * Created by Seth on 06/10/2016.
 */

public class Game {

    private GameListener mGameListener;

    private List<Player> mPlayerArray;
    private TimeLimit mTimeLimit;
    private boolean mCompleted;
    private int mID;

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
        int num = 0;
        for (Player p: mPlayerArray){
            num += p.getNumSetsPlayed();
        }
        return num  / size();
    }

    public int getInt(OptionID id) {
        int data = 1;

        for (IntEditTextOption e: mIntEditTextOptions){
            if (e.getmID() == id){
                data = Integer.valueOf(String.valueOf(e.getInt()));
                break;
            }
        }

        return data;
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

    public void setmIntEditTextOption(IntEditTextOption option){
        for (int i = 0; i < mIntEditTextOptions.size(); i++) {
            if (mIntEditTextOptions.get(i).getmID() == option.getmID()) {
                mIntEditTextOptions.set(i, option);
            }
        }
    }

    public void setmStringEditTextOption(StringEditTextOption option) {
        for (int i = 0; i < mStringEditTextOptions.size(); i++) {
            if (mStringEditTextOptions.get(i).getmID() == option.getmID()) {
                mStringEditTextOptions.set(i, option);
            }
        }
    }

    public void setmCheckBoxOption(CheckBoxOption checkBoxOption){
        for (int i = 0; i < mCheckBoxOptions.size(); i++) {
            if (mCheckBoxOptions.get(i).getmID() == checkBoxOption.getmID()) {
                mCheckBoxOptions.set(i, checkBoxOption);
            }
        }
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

    boolean isGameWon() {

        int maxScore = getInt(OptionID.WINNING_SCORE);

        boolean isWon = false;

        for (Player p : mPlayerArray) {

            if (maxScore != 0) {
                if (maxScore < 0) {
                    if (p.getmScore() <= maxScore && scoreDifference(maxScore)) {
                        mGameListener.onGameWon(getWinnerString());
                        isWon = true;
                        break;
                    }

                } else if (maxScore >= 0) {
                    if (p.getmScore() >= maxScore && scoreDifference(maxScore)) {
                        mGameListener.onGameWon(getWinnerString());
                        isWon = true;
                        break;
                    }

                }

            }

        }

        return isWon;

    }

    private boolean scoreDifference(int score) {
        boolean b = false;
        for (Player p : mPlayerArray) {
            if (getInt(OptionID.WINNING_SCORE) != 0) {
                if (Math.abs(score - p.getmScore()) >= getInt(OptionID.SCORE_DIFF_TO_WIN)) {
                    b = true;
                }
            }
        }
        return b;
    }

    void setGameListener(GameListener listener){
        mGameListener = listener;
    }

    GameListener getmGameListener(){
        return mGameListener;
    }

    interface GameListener {
        void onGameWon(String winner);

        void deletePlayer(int position);

        void editPlayer(int position);

    }
}


