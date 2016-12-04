package io.github.sdsstudios.ScoreKeeper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Seth on 06/10/2016.
 */

public class TimeLimit {
    private static String TAG = "TimeLimit";

    private String mTitle;
    private String mTime;

    public TimeLimit(String mTitle, String mTime) {
        this.mTitle = mTitle;
        this.mTime = mTime;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getmTime() {
        return mTime;
    }

    public void setmTime(String mTime) {
        this.mTime = mTime;
    }

    public static void deleteAllTimeLimits(Context ctx) {
        saveTimeLimit(new ArrayList<TimeLimit>(), ctx);
    }

    public static List<TimeLimit> getTimeLimitArray(Context ctx) {

        Gson gson = new GsonBuilder().serializeNulls().create();
        Type type = new TypeToken<List<TimeLimit>>() {
        }.getType();

        return gson.fromJson(PreferenceManager.getDefaultSharedPreferences(ctx).getString("new_timelimit_array", null), type);
    }

    public static void saveTimeLimit(List<TimeLimit> timeLimitArray, Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();

        Gson gson = new GsonBuilder().serializeNulls().create();

        editor.putString("new_timelimit_array", gson.toJson(timeLimitArray));

        editor.apply();
    }

    public static String updateTimeLimit(View dialogView, String oldTimeLimit) {

        final EditText editTextHour = (EditText) dialogView.findViewById(R.id.editTextHour);
        final EditText editTextMinute = (EditText) dialogView.findViewById(R.id.editTextMinute);
        final EditText editTextSecond = (EditText) dialogView.findViewById(R.id.editTextSeconds);

        String hour = editTextHour.getText().toString().trim();
        String minute = editTextMinute.getText().toString().trim();
        String seconds = editTextSecond.getText().toString().trim();

        String[] oldTimeLimitSplit;
        String oldHour = null, oldMinute = null, oldSeconds = null;

        if (oldTimeLimit != null) {
            oldTimeLimitSplit = oldTimeLimit.split(":");

            oldHour = oldTimeLimitSplit[0];
            oldMinute = oldTimeLimitSplit[1];
            oldSeconds = oldTimeLimitSplit[2];

        }

        if (oldTimeLimit != null){
            hour = String.valueOf(Integer.valueOf(hour) + Integer.valueOf(oldHour));
            minute = String.valueOf(Integer.valueOf(minute) + Integer.valueOf(oldMinute));
            seconds = String.valueOf(Integer.valueOf(seconds) + Integer.valueOf(oldSeconds));

        }

        String timeLimitString = "";

        if (TextUtils.isEmpty(hour)) {

            if (oldTimeLimit != null) {
                editTextHour.setError("Hour must be less than " + String.valueOf(24 - Integer.valueOf(oldHour)));
            } else {
                editTextHour.setError("Can't be empty");
            }

            return null;

        } else if (TextUtils.isEmpty(minute)) {

            if (oldTimeLimit != null) {
                editTextMinute.setError("Minute must be less than " + String.valueOf(60 - Integer.valueOf(oldMinute)));
            } else {
                editTextMinute.setError("Can't be empty");
            }

            return null;

        } else if (TextUtils.isEmpty(seconds)) {

            if (oldTimeLimit != null) {
                editTextSecond.setError("Seconds must be less than " + String.valueOf(60 - Integer.valueOf(oldSeconds)));
            } else {
                editTextSecond.setError("Can't be empty");

            }
            return null;

        } else {

            if (Integer.valueOf(hour) >= 24) {
                editTextHour.setError("Hour must be less than 24");
                return null;

            } else if (Integer.valueOf(minute) >= 60) {
                editTextMinute.setError("Minute must be less than 60");
                return null;

            } else if (Integer.valueOf(seconds) >= 60) {
                editTextSecond.setError("Seconds must be less than 60");
                return null;

            } else {

                if (hour.length() == 1 && !hour.equals("0")) {
                    hour = ("0" + hour);

                }

                if (minute.length() == 1 && !minute.equals("0")) {
                    minute = ("0" + minute);

                }
                if (seconds.length() == 1 && !seconds.equals("0")) {
                    seconds = ("0" + seconds);
                }

                if (hour.equals("0")) {
                    hour = "00";
                }

                if (minute.equals("0")) {
                    minute = "00";
                }

                if (seconds.equals("0")) {
                    seconds = "00";
                }

                timeLimitString += hour + ":";
                timeLimitString += minute + ":";
                timeLimitString += seconds + ":";
                timeLimitString += "0";

                Log.e(TAG, timeLimitString);

                return timeLimitString;

            }
        }

    }
}
