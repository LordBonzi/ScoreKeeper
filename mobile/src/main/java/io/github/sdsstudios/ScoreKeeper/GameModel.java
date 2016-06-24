package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Seth Schroeder on 22/05/2016.
 */

public class GameModel{
    private String mPlayers;
    private String mScore;
    private String mType;
    private String mDate;
    private String mProgress;
    private int gameID;
    private static ScoreDBAdapter dbHelper;

    public void closeDB(){
        dbHelper.close();
    }

    public GameModel(ScoreDBAdapter dbHelper){
        this ( null, null, null, null, null, 0, dbHelper);
    }

    public GameModel(String players, String score, String date, String type, String progress, int gameIDm, ScoreDBAdapter dbAdapter) {
        super();
        mPlayers = players;
        mScore = score;
        mDate = date;
        mType = type;
        mProgress = progress;
        gameID = gameIDm;
        dbHelper = dbAdapter;
    }

    public static ArrayList<GameModel> createGameModel(int numGames, int activity, Context context) {
        DataHelper dataHelper = new DataHelper();
        TimeHelper dateHelper = new TimeHelper();
        String p, s ,d ,t, progress = null;
        int j;
        int gameID;

        ArrayList arrayListPlayer;
        ArrayList arrayListScore;
        String date;

        ArrayList<GameModel> gameModelArrayList = new ArrayList<>();

        dbHelper.open();

        if (activity == 1){
            j = 1;
        }else{
            j = dbHelper.getNewestGame() - numGames;
        }

        for (int i = 1; i <= dbHelper.open().getNewestGame(); i++) {
            progress = "";
            p = null;
            s = null;
            t = null;

            arrayListPlayer = dataHelper.getArrayById(ScoreDBAdapter.KEY_PLAYERS, i, dbHelper);
            arrayListScore = dataHelper.getArrayById(ScoreDBAdapter.KEY_SCORE, i, dbHelper);
            gameID = i;

            date = dataHelper.getStringById(i, ScoreDBAdapter.KEY_TIME, dbHelper);
            d = dateHelper.gameDate(date);

            if (arrayListPlayer.size() == 2){
                t = "2 Player Game";
                p = arrayListPlayer.get(0) + " vs " + arrayListPlayer.get(1);
                s = arrayListScore.get(0) + ":" + arrayListScore.get(1);

            }else if (arrayListPlayer.size() == 3){
                t = "3 Player Game";
                p = arrayListPlayer.get(0) + " vs " + arrayListPlayer.get(1) + " vs " + arrayListPlayer.get(2);
                s = arrayListScore.get(0) + " : " + arrayListScore.get(1) + " : " + arrayListScore.get(2);

            }else if (arrayListPlayer.size() > 3 && arrayListPlayer.size() < 10){
                t = "Group Game";
                p = arrayListPlayer.get(0) + " vs " + arrayListPlayer.get(1);
                s = arrayListScore.get(0) + ":" + arrayListScore.get(1);

            }else if (arrayListPlayer.size() > 10){
                t = "Huge Game";
                p = arrayListPlayer.get(0) + " vs " + arrayListPlayer.get(1);
                s = arrayListScore.get(0) + ":" + arrayListScore.get(1);

            }else if (arrayListPlayer.size() == 1){
                t = "Game is too small. How did you make it this small. it is a bug. you must report it.";
                p = String.valueOf(arrayListPlayer.get(0));
                s = String.valueOf(
            arrayListScore.get(0));
        }
            if (activity == 1){
                dbHelper.open();

                if (dataHelper.getCompletedById(i, dbHelper)== 0){
                    progress = context.getResources().getString(R.string.in_progress);

                    gameModelArrayList.add(new GameModel(p , s , d, t, progress, gameID, dbHelper));
                    dbHelper.close();

                }
            }else if (activity == 2){
                dbHelper.open();
                if (dataHelper.getCompletedById(i, dbHelper)== 1){
                    progress = context.getResources().getString(R.string.completed);

                    gameModelArrayList.add(new GameModel(p , s , d, t, progress, gameID, dbHelper));
                    dbHelper.close();

                }

            }else if (activity == 3 ){
                dbHelper.open();

                if (dataHelper.getCompletedById(i, dbHelper) == 1){
                    progress = context.getResources().getString(R.string.completed);
                }else if(dataHelper.getCompletedById(i, dbHelper) == 0){
                    progress = context.getResources().getString(R.string.in_progress);
                }
                gameModelArrayList.add(new GameModel(p , s , d, t, progress, gameID,dbHelper));
                dbHelper.close();

            }

        }
        dbHelper.close();
        return gameModelArrayList;
    }

    public String getPlayers() {
        return mPlayers;
    }

    public String getScore() {
        return mScore;
    }

    public String getDate() {
        return mDate;
    }

    public String getType() {
        return mType;
    }

    public String getState() {
        return mProgress;
    }
    public int getGameID() {
        return gameID;
    }


}
