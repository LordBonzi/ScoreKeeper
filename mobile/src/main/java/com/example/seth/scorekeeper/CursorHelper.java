package com.example.seth.scorekeeper;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Seth Schroeder on 21/05/2016.
 */

public class CursorHelper extends Activity {
    ScoreDBAdapter dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new ScoreDBAdapter(this);
        dbHelper.open();
    }

    public String convertToString(ArrayList<String> arrayList) {
        String str = TextUtils.join(",", arrayList);

        return str;
    }

    public ArrayList convertToArray(String string) {

        String[] strValues = string.split(",");

        ArrayList array = new ArrayList<String>(Arrays.asList(strValues));

        return array;
    }

    public String getNewestGame(String request) {
        int index = dbHelper.getNewestGame(request).getColumnIndex(request);
        String valueStr = dbHelper.getNewestGame(request).getString(index);
        return valueStr;
    }

    public String getDBCursorArray(String request, int id) {
        int index = dbHelper.fetchGamesById(id).getColumnIndex(request);
        String value = dbHelper.fetchAllGames().getString(index);
        return value;
    }


    public String getDBCursorString(String request, int id) {
        int index = dbHelper.fetchGamesById(id).getColumnIndex(request);
        String value = dbHelper.fetchAllGames().getString(index);
        return value;
    }

}
