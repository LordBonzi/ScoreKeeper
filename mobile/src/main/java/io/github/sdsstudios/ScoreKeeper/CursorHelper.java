package io.github.sdsstudios.ScoreKeeper;

import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Seth Schroeder on 21/05/2016.
 */

public class CursorHelper {


    public ArrayList getArrayById(String request, int gameID, ScoreDBAdapter dbHelper){
        ArrayList array;

        Cursor cursor = dbHelper.fetchGamesById(gameID);
        int index = cursor.getColumnIndex(request);
        String s = cursor.getString(index);
        String[] strValues = s.split(",");
        array = new ArrayList<>(Arrays.asList(strValues));

        return array;
    }

    public String getTimeById(int i, ScoreDBAdapter dbHelper){

        String s;

        Cursor cursor = dbHelper.fetchGamesById(i);
        int index = cursor.getColumnIndex(ScoreDBAdapter.KEY_TIME);
        s = cursor.getString(index);

        return s;
    }

    public int getCompletedById(int i, ScoreDBAdapter dbHelper){
        int s;

        Cursor cursor = dbHelper.fetchGamesById(i);
        int index = cursor.getColumnIndex(ScoreDBAdapter.KEY_COMPLETED);
        s = cursor.getInt(index);


        return s;
    }

}
