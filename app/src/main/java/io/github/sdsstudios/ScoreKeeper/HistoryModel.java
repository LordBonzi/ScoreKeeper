package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Seth on 03/10/2016.
 */

public class HistoryModel {
    private String mPlayers;
    private String mScores;
    private String mDate;
    private String mTitle;
    private int mID;
    private String mIsUnfinished;

    public HistoryModel(String mPlayers, String mScores, String mDate, String mPresetGame, String isUnfinished, int mID) {
        this.mPlayers = mPlayers;
        this.mScores = mScores;
        this.mDate = mDate;
        this.mTitle = mPresetGame;
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

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    static List<HistoryModel> retrieveHistoryListFromGSON(SharedPreferences mSharedPreferences, ScoreDBAdapter mDbHelper, Context ctx){
        List<HistoryModel> list = null;

        try {
            Type type = new TypeToken<List<HistoryModel>>(){}.getType();

            Gson gson = new GsonBuilder().serializeNulls().create();
            list = gson.fromJson(mSharedPreferences.getString("history_list_cache",
                    saveHistoryModelListToGSON(newHistoryModelList(mDbHelper, ctx))), type);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DataHelper.class", e.toString());
        }

        return list;
    }

    static String saveHistoryModelListToGSON(List<HistoryModel> list){
        Gson gson = new GsonBuilder().serializeNulls().create();
        return gson.toJson(list);
    }

    static List<HistoryModel> getHistoryModelList(ScoreDBAdapter mDbHelper, SharedPreferences mSharedPreferences, Context ctx){
        int numRows = mDbHelper.numRows();

        if (mSharedPreferences.getInt("history_cache_size", 0) == numRows){
            return retrieveHistoryListFromGSON(mSharedPreferences, mDbHelper, ctx);
        }else{
            List<HistoryModel> list = newHistoryModelList(mDbHelper, ctx);

            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putInt("history_cache_size", numRows);
            editor.putString("history_list_cache", saveHistoryModelListToGSON(list));
            editor.apply();

            return list;
        }
    }

    synchronized static List<HistoryModel> newHistoryModelList(ScoreDBAdapter dbAdapter, Context context){
        List<HistoryModel> modelList = new ArrayList<>();
        List<Game> gameList = new ArrayList<>();
        DataHelper dataHelper = new DataHelper();

        for (int i = 1; i <= dbAdapter.numRows(); i++){
            gameList.add(dataHelper.getGame(i, dbAdapter));
        }

        for (Game game: gameList){
            modelList.add(new HistoryModel(game.getmTitle(),
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
