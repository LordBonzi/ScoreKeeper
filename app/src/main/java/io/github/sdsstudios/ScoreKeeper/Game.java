package io.github.sdsstudios.ScoreKeeper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Seth on 06/10/2016.
 */

public class Game {
    private List<Player> mPlayerArray;
    private String mTimeLimit;
    private String mLength;
    private String mTitle;
    private String mTime;
    private boolean mCompleted;
    private int mID;
    private List<Option> mOptions;

    public Game(List<Player> mPlayerArray, String mTimeLimit, String mTitle, String mLength, String mTime, boolean mCompleted, int mID
            , List<Option> options) {
        this.mPlayerArray = mPlayerArray;
        this.mTimeLimit = mTimeLimit;
        this.mTitle = mTitle;
        this.mID = mID;
        this.mLength = mLength;
        this.mTime = mTime;
        this.mCompleted = mCompleted;
        this.mOptions = options;
    }

    public static List<Option> createOptionArray(){
        List<Option> options = new ArrayList<>();
        for (int i = 0; i < Option.EDIT_TEXT_IDS.length; i++){
            options.add(new Option(Option.EDIT_TEXT_IDS[i], 0, Option.EDIT_TEXT));
        }

        for (int i = 0; i < Option.CHECK_BOX_IDS.length; i++){
            options.add(new Option(Option.CHECK_BOX_IDS[i], 0, Option.CHECK_BOX));
        }

        return options;
    }

    public List<Option> getmOptions() {
        return mOptions;
    }

    public void setmOptions(List<Option> mOptions) {
        this.mOptions = mOptions;
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

        return num;
    }

    public int getData(int id){
        int data = 1;
        for (Option e: mOptions){
            if (e.getmID() == id){
                data = e.getmData();
                break;
            }
        }

        return data;
    }

    public int numSets(){
        return mOptions.get(Option.NUMBER_SETS).getmData();
    }

    public boolean isChecked(int id){
        Option o = mOptions.get(id);
        return o.getmData() != 0;
    }

    public void setOptionData(int id, boolean data){
        if (!data){
            mOptions.get(id).setmData(0);
        }else{
            mOptions.get(id).setmData(1);
        }
    }

    public void setOptionData(int id, int data){
        mOptions.get(id).setmData(data);
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

    public void setOption(Option option){
        mOptions.set(option.getmID(), option);
    }

}
