package io.github.sdsstudios.ScoreKeeper;

import java.util.List;

/**
 * Created by Seth on 06/10/2016.
 */

public class Game {

    private String TAG = "Game";

    private GameListener mGameListener;

    private List<Player> mPlayerArray;
    private String mTimeLimit;
    private String mLength;
    private String mTitle;
    private String mTime;
    private boolean mCompleted;
    private int mID;

    private List<EditTextOption> mEditTextOptions;
    private List<CheckBoxOption> mCheckBoxOptions;

    public Game(List<Player> mPlayerArray, String mTimeLimit, String mTitle, String mLength, String mTime, boolean mCompleted, int mID
            , List<EditTextOption> editTextOptions, List<CheckBoxOption> checkBoxOptions, GameListener mGameListener) {
        this.mPlayerArray = mPlayerArray;
        this.mTimeLimit = mTimeLimit;
        this.mTitle = mTitle;
        this.mID = mID;
        this.mLength = mLength;
        this.mTime = mTime;
        this.mCompleted = mCompleted;
        this.mEditTextOptions = editTextOptions;
        this.mCheckBoxOptions = checkBoxOptions;
        this.mGameListener = mGameListener;
    }

    public boolean ismCompleted() {
        return mCompleted;
    }

    public void setmCompleted(boolean mCompleted) {
        this.mCompleted = mCompleted;
    }

    public String getmTime() {
        return mTime;
    }

    public void setmTime(String mTime) {
        this.mTime = mTime;
    }

    public String getmLength() {
        return mLength;
    }

    public void setmLength(String mLength) {
        this.mLength = mLength;
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

    public String getmTimeLimit() {
        return mTimeLimit;
    }

    public void setmTimeLimit(String mTimeLimit) {
        this.mTimeLimit = mTimeLimit;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
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

    public int getData(int id){
        int data = 1;

        for (EditTextOption e: mEditTextOptions){
            if (e.getmID() == id){
                data = e.getmData();
                break;
            }
        }

        return data;
    }

    public List<EditTextOption> getmEditTextOptions() {
        return mEditTextOptions;
    }

    public void setmEditTextOptions(List<EditTextOption> mEditTextOptions) {
        this.mEditTextOptions = mEditTextOptions;
    }

    public List<CheckBoxOption> getmCheckBoxOptions() {
        return mCheckBoxOptions;
    }

    public void setmCheckBoxOptions(List<CheckBoxOption> mCheckBoxOptions) {
        this.mCheckBoxOptions = mCheckBoxOptions;
    }

    public int numSets(){
        return mEditTextOptions.get(EditTextOption.NUMBER_SETS).getmData();
    }

    public boolean isChecked(int id){
        return mCheckBoxOptions.get(id).getmData() != 0;
    }

    public void setChecked(int id, boolean data){

        if (!data){
            mCheckBoxOptions.get(id).setmData(0);
        }else{
            mCheckBoxOptions.get(id).setmData(1);
        }
    }

    public void setData(int id, int data){
        mEditTextOptions.get(id).setmData(data);
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

    public void setmEditTextOption(EditTextOption option){
        mEditTextOptions.set(option.getmID(), option);
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

    public boolean isGameWon(){
        int maxScore = getData(EditTextOption.WINNING_SCORE);
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
            if (getData(EditTextOption.WINNING_SCORE) != 0) {
                if (Math.abs(score - p.getmScore()) >= getData(EditTextOption.SCORE_DIFF_TO_WIN)) {
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


