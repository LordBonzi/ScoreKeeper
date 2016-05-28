package io.github.sdsstudios.ScoreKeeper;

import android.database.Cursor;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Seth Schroeder on 21/05/2016.
 */

public class CursorHelper {

    public String convertToString(ArrayList arrayList) {

        arrayList = new ArrayList<>();
        String str = TextUtils.join(",", arrayList);

        return str;
    }

    public ArrayList getArrayById(String request, int gameID, ScoreDBAdapter dbHelper){
        Cursor cursor = dbHelper.fetchGamesById(gameID);
        int index = cursor.getColumnIndex(request);
        String s = cursor.getString(index);
        String[] strValues = s.split(",");
        ArrayList array = new ArrayList<>(Arrays.asList(strValues));

        return array;
    }

    public String getTimeById(int i, ScoreDBAdapter dbHelper){
        Cursor cursor = dbHelper.fetchGamesById(i);
        int index = cursor.getColumnIndex(ScoreDBAdapter.KEY_TIME);
        String s = cursor.getString(index);

        return s;
    }

    public ArrayList convertToArray(String string) {

        String[] strValues = string.split(",");
        ArrayList array = new ArrayList<String>(Arrays.asList(strValues));

        return array;
    }


}
