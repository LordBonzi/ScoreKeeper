package io.github.sdsstudios.ScoreKeeper.Helper;

import android.database.Cursor;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.sdsstudios.ScoreKeeper.Adapters.GameDBAdapter;
import io.github.sdsstudios.ScoreKeeper.Adapters.PresetDBAdapter;
import io.github.sdsstudios.ScoreKeeper.Game;
import io.github.sdsstudios.ScoreKeeper.Player;

/**
 * Created by Seth Schroeder on 21/05/2016.
 */

public class DataHelper {

    private String TAG = "DataHelper";

    public Game getGame(int id, GameDBAdapter dbHelper) {

        dbHelper.open();
        Cursor cursor = dbHelper.fetchGamesById(id);

        int index = cursor.getColumnIndex(GameDBAdapter.KEY_GAME);

        Game game = null;
        try {
            Gson gson = new GsonBuilder().serializeNulls().create();

            Type type = new TypeToken<Game>(){}.getType();
            game = gson.fromJson(cursor.getString(index), type);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DataHelper.class", e.toString());
        }

        return game;
    }

    public Game getPreset(int id, PresetDBAdapter dbHelper){

        dbHelper.open();
        Cursor cursor = dbHelper.fetchPresetByID(id);

        int index = cursor.getColumnIndex(PresetDBAdapter.KEY_GAME);

        Game game = null;

        try {
            Gson gson = new GsonBuilder().serializeNulls().create();

            Type type = new TypeToken<Game>(){}.getType();
            game = gson.fromJson(cursor.getString(index), type);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DataHelper.class", e.toString());
        }

        return game;
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
