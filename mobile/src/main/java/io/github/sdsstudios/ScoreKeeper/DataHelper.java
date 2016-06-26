package io.github.sdsstudios.ScoreKeeper;

import android.database.Cursor;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Seth Schroeder on 21/05/2016.
 */

public class DataHelper {

    public List convertToArray(String s){
        List arrayList = null;

        String[] strValues = s.split(",");
        arrayList = new ArrayList<>(Arrays.asList(strValues));

        return arrayList;
    }

    public String convertToString(List array) {

        String str = TextUtils.join(",", array);

        return str;
    }

    public ArrayList getArrayById(String request, int gameID, ScoreDBAdapter dbHelper){
        ArrayList array;
        dbHelper.open();

        Cursor cursor = dbHelper.fetchGamesById(gameID);
        dbHelper.close();
        int index = cursor.getColumnIndex(request);
        String s = cursor.getString(index);
        String[] strValues = s.split(",");
        array = new ArrayList<>(Arrays.asList(strValues));
        cursor.close();

        return array;
    }

    public String getStringById(int i, String request, ScoreDBAdapter dbHelper){

        String s;

        dbHelper.open();
        Cursor cursor = dbHelper.fetchGamesById(i);
        dbHelper.close();
        int index = cursor.getColumnIndex(request);
        s = cursor.getString(index);

        cursor.close();
        return s;
    }

    public int getCompletedById(int i, ScoreDBAdapter dbHelper){
        int s;

        dbHelper.open();
        Cursor cursor = dbHelper.fetchGamesById(i);
        dbHelper.close();
        int index = cursor.getColumnIndex(ScoreDBAdapter.KEY_COMPLETED);
        s = cursor.getInt(index);

        cursor.close();

        return s;
    }

}
