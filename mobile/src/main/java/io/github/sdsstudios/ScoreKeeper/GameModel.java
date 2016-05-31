package io.github.sdsstudios.ScoreKeeper;

import android.app.Activity;
import android.content.Context;

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


    public GameModel(String players, String score, String date, String type, String progress) {
        mPlayers = players;
        mScore = score;
        mDate = date;
        mType = type;
        mProgress = progress;
    }

    public static ArrayList<GameModel> createGameModel(int numGames, ScoreDBAdapter dbHelper, int activity, Context context) {
        CursorHelper cursorHelper = new CursorHelper();
        DateHelper dateHelper = new DateHelper();
        String p, s ,d ,t, progress = null;
        int j;

        ArrayList arrayListPlayer;
        ArrayList arrayListScore;
        String date;

        ArrayList<GameModel> gameModelArrayList = new ArrayList<>();

        if (activity == 1){
            j = 1;
        }else{
            j = Integer.valueOf(dbHelper.getNewestGame())- numGames;
        }

        for (int i = 1; i <= Integer.valueOf(dbHelper.getNewestGame()); i++) {
            progress = "";
            p = null;
            s = null;
            t = null;

            arrayListPlayer = cursorHelper.getArrayById(ScoreDBAdapter.KEY_PLAYERS, i, dbHelper);
            arrayListScore = cursorHelper.getArrayById(ScoreDBAdapter.KEY_SCORE, i, dbHelper);

            date = cursorHelper.getTimeById(i, dbHelper);
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
                if (cursorHelper.getCompletedById(i, dbHelper)== 1){
                    progress = context.getResources().getString(R.string.in_progress);

                    gameModelArrayList.add(new GameModel(p , s , d, t, progress));

                }
            }else if (activity == 2){
                if (cursorHelper.getCompletedById(i, dbHelper)== 0){
                    progress = context.getResources().getString(R.string.completed);

                    gameModelArrayList.add(new GameModel(p , s , d, t, progress));

                }

            }else if (activity == 3 ){

                if (cursorHelper.getCompletedById(i, dbHelper) == 0){
                    progress = context.getResources().getString(R.string.completed);
                }else if(cursorHelper.getCompletedById(i, dbHelper) == 1){
                    progress = context.getResources().getString(R.string.in_progress);
                }
                gameModelArrayList.add(new GameModel(p , s , d, t, progress));
            }

        }
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

}
