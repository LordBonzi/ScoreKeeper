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
            modelList.add(new HistoryModel(playerString(game),
                    infoString(game), dateString(game), game.getmTitle(), isUnfinishedString(game, context), game.getmID()));
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

    private static String dateString(Game game){
        return TimeHelper.gameDate(game.getmTime());
    }

}
