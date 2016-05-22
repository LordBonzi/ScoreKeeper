package io.github.sdsstudios.ScoreKeeper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

    public String getDBCursorString(String request, int id, ScoreDBAdapter dbHelper) {

        int index = dbHelper.fetchGamesById(id).getColumnIndex(request);
        String value = dbHelper.fetchAllGames().getString(index);
        return value;
    }
}