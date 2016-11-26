package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
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


    Game getGame(int id, ScoreDBAdapter dbHelper){

        dbHelper.open();
        Cursor cursor = dbHelper.fetchGamesById(id);

        int index = cursor.getColumnIndex(ScoreDBAdapter.KEY_GAME);

        Game gameList = null;
        try {
            Gson gson = new GsonBuilder().serializeNulls().create();

            Type type = new TypeToken<Game>(){}.getType();
            gameList = gson.fromJson(cursor.getString(index), type);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DataHelper.class", e.toString());
        }

        return gameList;
    }

    public Game getPreset(int id, PresetDBAdapter dbHelper){

        dbHelper.open();
        Cursor cursor = dbHelper.fetchPresetByID(id);

        int index = cursor.getColumnIndex(ScoreDBAdapter.KEY_GAME);

        Game gameList = null;
        try {
            Gson gson = new Gson();

            Type type = new TypeToken<Game>(){}.getType();
            gameList = gson.fromJson(cursor.getString(index), type);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DataHelper.class", e.toString());
        }

        return gameList;
    }

    public String convertToString(List array) {

        String str = TextUtils.join(",", array);

        return str;
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

    public boolean checkPlayerDuplicates(List<Player> arrayList){
        boolean duplicate = false;
        Set<String> set = new HashSet<String>();

        for(Player p : arrayList){
            set.add(p.getmName());
        }

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



}
