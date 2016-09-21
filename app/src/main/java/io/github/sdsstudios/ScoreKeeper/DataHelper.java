package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public int getIntByID(int i, String request, ScoreDBAdapter dbHelper){

        int maxscore = 0;
        int index = 1;
        dbHelper.open();
        Cursor cursor = dbHelper.fetchGamesById(i);

        index = cursor.getColumnIndex(request);

        try {
            maxscore = cursor.getInt(index);
        }catch (Exception e){
            e.printStackTrace();
            Log.e("DataHelper", e.toString());
        }

        dbHelper.close();
        cursor.close();
        return maxscore;
    }

    public ArrayList getArrayById(String request, int gameID, ScoreDBAdapter dbHelper){
        ArrayList array = new ArrayList();
        dbHelper.open();

        Cursor cursor = dbHelper.fetchGamesById(gameID);
        dbHelper.close();
        int index = cursor.getColumnIndex(request);

        String s = cursor.getString(index);
        
        if (s != null) {
            String[] strValues = s.split(",");
            array = new ArrayList<>(Arrays.asList(strValues));
        }
        
        cursor.close();

        return array;
    }

    public void saveSharedPrefs(List array, List arrayNum, Context context){
        SharedPreferences sharedPref = context.getSharedPreferences("scorekeeper", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putString("timelimitarray", convertToString(array));
        editor.putString("timelimitarraynum", convertToString(arrayNum));

        editor.apply();
    }

    public boolean checkDuplicates(List arrayList){
        boolean duplicate = false;

        Set<Integer> set = new HashSet<Integer>(arrayList);
        if(set.size() < arrayList.size()){
            duplicate = true;
        }

        return duplicate;
    }


    public String createTimeLimitCondensed(String timeLimit){
        Pattern p = Pattern.compile("^\\d+$");
        Matcher m = p.matcher(timeLimit);
        String timeLimitCondensed = "";
        StringBuilder stringBuilder = new StringBuilder();

        if(m.matches()){
            timeLimit.replaceAll("^0+", "");
        }

        String[] timeLimitSplit = timeLimit.split(":");

        String hour = timeLimitSplit[0];
        String minute = timeLimitSplit[1];
        String second = timeLimitSplit[2];

        if (!hour.equals("00")){
            stringBuilder.append(Integer.valueOf(hour).toString()).append(" Hrs ");
            timeLimitCondensed = stringBuilder.toString();

        }

        if(!minute.equals("00")){
            if (!timeLimitCondensed.equals("")){
                stringBuilder.append(" · " + Integer.valueOf(minute).toString()).append(" Mins ");

            }else{
                stringBuilder.append(Integer.valueOf(minute).toString()).append(" Mins ");

            }
            timeLimitCondensed = stringBuilder.toString();

        }

        if(!second.equals("00")){

            if (!timeLimitCondensed.equals("")){
                stringBuilder.append(" · " + Integer.valueOf(second).toString()).append(" Secs ");

            }else{
                stringBuilder.append(Integer.valueOf(second).toString()).append(" Secs ");

            }
            timeLimitCondensed = stringBuilder.toString();
        }

        return timeLimitCondensed;

    }

    public String getStringById(int i, String request, ScoreDBAdapter dbHelper){

        String s = "";

        dbHelper.open();
        Cursor cursor = dbHelper.fetchGamesById(i);
        int index = cursor.getColumnIndex(request);
        try {
            s = cursor.getString(index);
        }catch (Exception e){
            e.printStackTrace();
        }
        dbHelper.close();
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

    public String getPresetStringByID(int i, String request, PresetDBAdapter dbHelper){

        String s = "";
        int index = 1;
        dbHelper.open();
        Cursor cursor = dbHelper.fetchPresetById(i);

        if (request == PresetDBAdapter.KEY_TIME_LIMIT) {
            index = cursor.getColumnIndex(PresetDBAdapter.KEY_TIME_LIMIT);
        }else if(request == PresetDBAdapter.KEY_TITLE){
            index = cursor.getColumnIndex(PresetDBAdapter.KEY_TITLE);

        }

        try {
            s = cursor.getString(index);
        }catch (Exception e){
            e.printStackTrace();
            Log.e("DataHelper", e.toString());
        }

        dbHelper.close();
        cursor.close();
        return s;
    }

    public int getPresetIntByID(int i, String request, PresetDBAdapter dbHelper){

        int maxscore = 0;
        int index = 1;
        dbHelper.open();
        Cursor cursor = dbHelper.fetchPresetById(i);

        index = cursor.getColumnIndex(request);

        try {
            maxscore = cursor.getInt(index);
        }catch (Exception e){
            e.printStackTrace();
            Log.e("DataHelper", e.toString());
        }

        dbHelper.close();
        cursor.close();
        return maxscore;
    }

    public ArrayList getPresetPlayerArrayByID(int gameID,  PresetDBAdapter dbHelper){
        ArrayList array;
        dbHelper.open();

        Cursor cursor = dbHelper.fetchPresetById(gameID);
        int index = cursor.getColumnIndex(PresetDBAdapter.KEY_PLAYERS);
        String s = cursor.getString(index);
        String[] strValues = s.split(",");
        array = new ArrayList<>(Arrays.asList(strValues));
        cursor.close();
        dbHelper.close();

        return array;
    }


}
