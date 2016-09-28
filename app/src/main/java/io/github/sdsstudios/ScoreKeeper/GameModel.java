package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Seth Schroeder on 22/05/2016.
 */

public class GameModel{
    private String mPlayers;
    private String mType;
    private String mDate;
    private String mProgress;
    private String mScore;
    private int gameID;


    public GameModel(String players, String score, String date, String type, String progress, int gameIDm) {
        super();
        mPlayers = players;
        mDate = date;
        mScore = score;
        mType = type;
        mProgress = progress;
        gameID = gameIDm;
    }

    public static ArrayList<GameModel> createGameModel(int numGames, int activity, Context context, ScoreDBAdapter dbHelper) {
        DataHelper dataHelper = new DataHelper();
        TimeHelper dateHelper = new TimeHelper();
        String p, s, d ,t, progress;
        int gameID;

        String date;

        ArrayList<GameModel> gameModelArrayList = new ArrayList<>();

        dbHelper.open();

        if (numGames > dbHelper.numRows()){
            numGames = dbHelper.numRows();
        }

        for (int i = 1; i <= dbHelper.open().numRows(); i++) {
            progress = "";
            p = "";
            t = "";
            s = "";

            List<Player> mPlayerArray = dataHelper.getPlayerArray(i, dbHelper);
            gameID = i;

            date = dataHelper.getStringById(i, ScoreDBAdapter.KEY_TIME, dbHelper);
            d = dateHelper.gameDate(date);

            if (mPlayerArray.size() == 2){
                try {
                    t = "2 Player Game";
                    p = mPlayerArray.get(0).getmName() + " vs " + mPlayerArray.get(1).getmName();
                    s = mPlayerArray.get(0).getmScore() + " vs " + mPlayerArray.get(1).getmScore();
                }catch (Exception e){
                    Toast.makeText(context, "Error creating an item", Toast.LENGTH_SHORT).show();
                }

            }else if (mPlayerArray.size() == 3){

                try {
                    t = "3 Player Game";
                    p = mPlayerArray.get(0).getmName() + " vs " + mPlayerArray.get(1).getmName() + " vs " + mPlayerArray.get(2).getmName();
                    s = mPlayerArray.get(0).getmScore() + " : " + mPlayerArray.get(1).getmScore() + " : " + mPlayerArray.get(2).getmScore();
                }catch (Exception e){
                    Toast.makeText(context, "Error creating an item", Toast.LENGTH_SHORT).show();
                }

            }else if (mPlayerArray.size() > 3){
                try {

                    if (mPlayerArray.size() > 10){
                        t = "Huge Game";
                    }else{
                        t = "Group Game";

                    }
                    for (int j = 0; j < mPlayerArray.size(); j++){
                        p += mPlayerArray.get(j).getmName();
                        if (j != mPlayerArray.size()-1){
                            p += ", ";
                        }
                        s += mPlayerArray.get(j).getmScore();
                        if (j != mPlayerArray.size()-1){
                            s += " : ";

                        }
                    }
                }catch (Exception e){
                    Toast.makeText(context, "Error creating an item", Toast.LENGTH_SHORT).show();
                }

            }else if (mPlayerArray.size() == 1){
                try {

                    t = "Game is too small. How did you make it this small. it is a bug. you must report it.";
                    p = String.valueOf(mPlayerArray.get(0).getmName());
                    s = String.valueOf(mPlayerArray.get(0).getmScore());
                }catch (Exception e){
                    Toast.makeText(context, "Error creating an item", Toast.LENGTH_SHORT).show();
                }
            }

            if (activity == 1){
                dbHelper.open();
                if (dataHelper.getCompletedById(i, dbHelper)== 0){
                    progress = context.getResources().getString(R.string.unfinished);
                    t += " ·";
                    if (gameModelArrayList.size() < numGames) {
                        gameModelArrayList.add(new GameModel(p, s, d, t, progress, gameID));
                    }

                    dbHelper.close();

                }

            }else if (activity == 2){

                dbHelper.open();
                if (dataHelper.getCompletedById(i, dbHelper)== 1){
                    progress = context.getResources().getString(R.string.completed);
                    t += " ·";
                    gameModelArrayList.add(new GameModel(p , s , d, t, progress, gameID));

                    dbHelper.close();
                }

            }else if (activity == 3){
                dbHelper.open();

                if (dataHelper.getCompletedById(i, dbHelper) == 1){
                    progress = context.getResources().getString(R.string.completed);
                }else if(dataHelper.getCompletedById(i, dbHelper) == 0){
                    progress = context.getResources().getString(R.string.unfinished);
                }
                t += " ·";

                gameModelArrayList.add(new GameModel(p,s, d, t, progress, gameID));

                dbHelper.close();

            }

        }

        dbHelper.close();
        return gameModelArrayList;
    }

    public String getPlayers() {
        return mPlayers;
    }


    public String getDate() {
        return mDate;
    }

    public String getmScore() {
        return mScore;
    }

    public void setmScore(String mScore) {
        this.mScore = mScore;
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
