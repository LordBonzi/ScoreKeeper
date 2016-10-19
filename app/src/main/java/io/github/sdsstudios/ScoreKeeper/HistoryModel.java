package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Seth on 03/10/2016.
 */

public class HistoryModel {
    private String mPlayers;
    private String mScores;
    private String mDate;
    private String mPresetGame;
    private int mID;
    private String mIsUnfinished;

    public HistoryModel(String mPlayers, String mScores, String mDate, String mPresetGame, String isUnfinished, int mID) {
        this.mPlayers = mPlayers;
        this.mScores = mScores;
        this.mDate = mDate;
        this.mPresetGame = mPresetGame;
        this.mID = mID;
        this.mIsUnfinished = isUnfinished;
    }

    public String getmIsUnfinished() {
        return mIsUnfinished;
    }

    public void setmIsUnfinished(String mIsUnfinished) {
        this.mIsUnfinished = mIsUnfinished;
    }

    public int getmID() {
        return mID;
    }

    public void setmID(int mID) {
        this.mID = mID;
    }

    public String getmPlayers() {
        return mPlayers;
    }

    public void setmPlayers(String mPlayers) {
        this.mPlayers = mPlayers;
    }

    public String getmScores() {
        return mScores;
    }

    public void setmScores(String mScores) {
        this.mScores = mScores;
    }

    public String getmDate() {
        return mDate;
    }

    public void setmDate(String mDate) {
        this.mDate = mDate;
    }

    public String getmPresetGame() {
        return mPresetGame;
    }

    public void setmPresetGame(String mPresetGame) {
        this.mPresetGame = mPresetGame;
    }

    public static List<HistoryModel> createHistoryModel(ScoreDBAdapter dbAdapter, Context context){
        List<HistoryModel> modelList = new ArrayList<>();
        List<Game> gameList = new ArrayList<>();
        DataHelper dataHelper = new DataHelper();

        for (int i = 0; i < dbAdapter.numRows(); i++){
            gameList.add(dataHelper.getGame(i, dbAdapter));
        }

        for (Game game: gameList){
            modelList.add(new HistoryModel(game.size() + "players",
                    scoreString(), dateString(), titleString(), isUnfinishedString(game, context), game.getmID()));
        }

        return modelList;
    }

    private static String scoreString(){
        return "";
    }

    private static String isUnfinishedString(Game game, Context ctx){
        if (game.ismCompleted()){
            return ctx.getResources().getString(R.string.completed);
        }else{
            return ctx.getResources().getString(R.string.unfinished);
        }
    }

    private static String dateString(){
        return "";
    }

    private static String titleString(){
        return "";
    }
}
