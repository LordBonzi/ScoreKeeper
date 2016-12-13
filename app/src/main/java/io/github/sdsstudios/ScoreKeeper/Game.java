package io.github.sdsstudios.ScoreKeeper;

import java.util.List;

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
        return getString(EditTextOption.DATE);
    }

    public void setmTime(String mTime) {
        setString(EditTextOption.DATE, mTime);
    }

    public String getmLength() {
        return getString(EditTextOption.LENGTH);
    }

    public void setmLength(String mLength) {
        setString(EditTextOption.LENGTH, mLength);
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
        return getString(EditTextOption.TITLE);
    }

    public void setmTitle(String mTitle) {
        setString(EditTextOption.TITLE, mTitle);
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
            num += p.getmSetScores().size();
        }

        return num  / size();
    }

    public int getInt(int id){
        int data = 1;

        for (IntEditTextOption e: mIntEditTextOptions){

            if (e.getmID() == id){
                data = Integer.valueOf(String.valueOf(e.getInt()));
                break;
            }

        }

        return data;
    }

    public String getString(int id){
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
        return mIntEditTextOptions.get(IntEditTextOption.NUMBER_SETS).getInt();
    }

    public boolean isChecked(int id){
        return mCheckBoxOptions.get(id).isChecked();
    }

    public void setChecked(int id, boolean data){
        mCheckBoxOptions.get(id).setChecked(data);
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

    public void setString(int id, String data){
        mStringEditTextOptions.get(id - EditTextOption.NUM_INT_OPTIONS).setString(data);
    }

    public void setInt(int id, int data){
        mIntEditTextOptions.get(id).setInt(data);
    }

    public void addPlayer(Player player){
        mPlayerArray.add(player);
    }

    public void removePlayer(int position){
        mPlayerArray.remove(position);
    }

    public Player getPlayer(int position){
        return mPlayerArray.get(position);
    }

    public void setmIntEditTextOption(IntEditTextOption option){
        mIntEditTextOptions.set(option.getmID(), option);
    }

    public void setmCheckBoxOption(CheckBoxOption checkBoxOption){
        mCheckBoxOptions.set(checkBoxOption.getmID(), checkBoxOption);
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

        int maxScore = getInt(IntEditTextOption.WINNING_SCORE);
        boolean isWon = false;

        for (Player p : mPlayerArray) {

            if (maxScore != 0) {
                if (maxScore < 0) {
                    if (p.getmScore() <= maxScore && scoreDifference(maxScore)) {
                        mGameListener.gameWon(getWinnerString());
                        isWon = true;
                    }

                } else if (maxScore >= 0) {
                    if (p.getmScore() >= maxScore && scoreDifference(maxScore)) {
                        mGameListener.gameWon(getWinnerString());
                        isWon = true;
                    }

                }

            }

        }

        return isWon;

    }

    private boolean scoreDifference(int score) {
        boolean b = false;
        for (Player p : mPlayerArray) {
            if (getInt(IntEditTextOption.WINNING_SCORE) != 0) {
                if (Math.abs(score - p.getmScore()) >= getInt(IntEditTextOption.SCORE_DIFF_TO_WIN)) {
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
        void gameWon(String winner);

        void deletePlayer(int position);

        void editPlayer(int position);

    }
}


