package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Seth on 03/10/2016.
 */

public class HistoryModel {
    private String mPlayers;
    private String mInfo;
    private String mDate;
    private String mTitle;
    private int mID;
    private String mIsUnfinished;

    public HistoryModel(String mPlayers, String mInfo, String mDate, String mTitle, String isUnfinished, int mID) {
        this.mPlayers = mPlayers;
        this.mInfo = mInfo;
        this.mDate = mDate;
        this.mTitle = mTitle;
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

    public String getmInfo() {
        return mInfo;
    }

    public void setmInfo(String mInfo) {
        this.mInfo = mInfo;
    }

    public String getmDate() {
        return mDate;
    }

    public void setmDate(String mDate) {
        this.mDate = mDate;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    synchronized static List<HistoryModel> getHistoryModelList(ScoreDBAdapter mDbHelper, Context ctx){
        return newHistoryModelList(mDbHelper, ctx);

    }

    synchronized static List<HistoryModel> newHistoryModelList(ScoreDBAdapter dbAdapter, Context context){
        List<HistoryModel> modelList = new ArrayList<>();
        List<Game> gameList = new ArrayList<>();
        DataHelper dataHelper = new DataHelper();

        for (int i = 1; i <= dbAdapter.numRows(); i++){
            gameList.add(dataHelper.getGame(i, dbAdapter));
        }

        for (Game game: gameList){
            modelList.add(new HistoryModel(playerString(game),
                    infoString(game), game.getmTime(), game.getmTitle(), isUnfinishedString(game, context), game.getmID()));
        }

        return modelList;
    }

    private static String playerString(Game game){
        String string = "";
        int size = game.getmPlayerArray().size();

        for (int i = 0; i < size; i++){

            Player p = game.getmPlayerArray().get(i);
            string += p.getmName();

            if (i != size - 1){
                string += ", ";
            }

        }

        return string;
    }

    private static String infoString(Game game){
        return game.size() + " Players, " + game.numSets() + " Sets";
    }

    private static String isUnfinishedString(Game game, Context ctx){
        if (game.ismCompleted()){
            return ctx.getResources().getString(R.string.completed);
        }else{
            return ctx.getResources().getString(R.string.unfinished);
        }
    }

}
