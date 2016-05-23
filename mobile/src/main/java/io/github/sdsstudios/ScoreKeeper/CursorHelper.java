package io.github.sdsstudios.ScoreKeeper;

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

    public ArrayList convertToArray(String string) {

        String[] strValues = string.split(",");
        ArrayList array = new ArrayList<String>(Arrays.asList(strValues));

        return array;
    }

    public ArrayList getDBCursorArray(String request, ScoreDBAdapter dbHelper) {

        return convertToArray(dbHelper.getNewestGame());

    }

    public ArrayList getDBCursorArrayBYId(String request, int id, ScoreDBAdapter dbHelper) {

        int index = dbHelper.fetchGamesById(id).getColumnIndex(request);
        String value = dbHelper.fetchAllGames().getString(index);
        return convertToArray(value);

    }

    public String getDBCursorString(String request, int id, ScoreDBAdapter dbHelper) {

        int index = dbHelper.fetchGamesById(id).getColumnIndex(request);
        String value = dbHelper.fetchAllGames().getString(index);
        return value;
    }
}
