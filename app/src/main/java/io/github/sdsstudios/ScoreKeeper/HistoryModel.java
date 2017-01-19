package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import io.github.sdsstudios.ScoreKeeper.Activity.Activity;
import io.github.sdsstudios.ScoreKeeper.Adapters.GameDBAdapter;
import io.github.sdsstudios.ScoreKeeper.Adapters.HistoryAdapter;
import io.github.sdsstudios.ScoreKeeper.Helper.DataHelper;

/**
 * Created by Seth on 03/10/2016.
 */

public class HistoryModel {
    private static String TAG = "HistoryModel";

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

    synchronized static List<HistoryModel> getHistoryModelList(GameDBAdapter dbAdapter, Context context, Activity activity, int gamesToShow) {
        List<HistoryModel> modelList = new ArrayList<>();
        List<Game> gameList = new ArrayList<>();
        DataHelper dataHelper = new DataHelper();

        int numGames;

        if (activity == Activity.HOME) {
            numGames = Integer.valueOf(PreferenceManager
                    .getDefaultSharedPreferences(context).getString("prefNumGames", "3"));
        }else{

            numGames = dbAdapter.numRows();

        }

        for (int i = 1; i <= dbAdapter.numRows(); i++){
            if (gameList.size() != numGames) {

                Game game = dataHelper.getGame(i, dbAdapter);

                if (!game.ismCompleted() && activity == Activity.HOME) {
                    gameList.add(game);
                } else if (activity == Activity.HISTORY) {

                    switch (gamesToShow){
                        case HistoryAdapter.BOTH:
                            gameList.add(game);
                            break;

                        case HistoryAdapter.COMPLETED:
                            if (game.ismCompleted()) {
                                gameList.add(game);
                            }
                            break;

                        case HistoryAdapter.UNFINISHED:
                            if (!game.ismCompleted()) {
                                gameList.add(game);
                            }
                            break;


                    }

                }
            }
        }

        for (Game game: gameList){
            modelList.add(new HistoryModel(playerString(game),
                    infoString(game), game.getmTime(), game.getmTitle(), isUnfinishedString(game, context), game.getmID()));
        }

        Collections.sort(modelList, new Comparator<HistoryModel>() {
            @Override
            public int compare(HistoryModel model1, HistoryModel model2) {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date1 = null;
                Date date2 = null;

                try {
                    date1 = df.parse(model1.getmDate());
                    date2 = df.parse(model2.getmDate());
                } catch (ParseException e) {
                    e.printStackTrace();
                    Log.e(TAG, e.toString());
                }

                int i= 0;

                if (date1 != null) {
                    i = date1.compareTo(date2);
                }

                return i;

            }
        });

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

}
