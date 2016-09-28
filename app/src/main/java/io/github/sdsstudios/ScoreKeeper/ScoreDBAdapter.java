package io.github.sdsstudios.ScoreKeeper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Seth Schroeder on 27/11/2015.
 */

public class ScoreDBAdapter {
    public static final String KEY_ROWID = "_id";
    public static final String KEY_SCORE = "_score";
    public static final String KEY_PLAYERS = "_players";
    public static final String KEY_TIME = "_time";
    public static final String KEY_COMPLETED = "_completed";
    public static final String KEY_TIMER = "_timer";
    public static final String KEY_CHRONOMETER = "_chronometer";
    public static final String KEY_MAX_SCORE = "_maxscore";
    public static final String KEY_REVERSE_SCORING = "_reversescoring";
    public static final String KEY_SCORE_INTERVAL = "_scoreinterval";
    public static final String KEY_DIFF_TO_WIN = "_difftowin";
    public static final String KEY_STOPWATCH = "_stopwatch";
    public static final String KEY_SETS = "_sets";
    public static final String KEY_NUM_SETS = "_numsets";
    public static final String SQLITE_TABLE = "score";
    private static final String TAG = "ScoreDBAdapter";
    private static final String DATABASE_NAME = "ScoreKeeper";
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_CREATE =
            "CREATE TABLE if not exists " + SQLITE_TABLE + " (" +
                    KEY_ROWID + " ," +
                    KEY_PLAYERS + "," +
                    KEY_SCORE + " , " +
                    KEY_TIME + " , " +
                    KEY_COMPLETED + " , " +
                    KEY_CHRONOMETER + " , " +
                    KEY_TIMER + " , " +
                    KEY_MAX_SCORE + " , " +
                    KEY_REVERSE_SCORING + " , " +
                    KEY_SCORE_INTERVAL + " , " +
                    KEY_DIFF_TO_WIN + " , " +
                    KEY_STOPWATCH + " , " +
                    KEY_SETS+ " , " +
                    KEY_NUM_SETS +
                    " );";

    public static String[] COLUMN_ARRAY = {KEY_ROWID, KEY_PLAYERS
            , KEY_SCORE, KEY_TIME, KEY_COMPLETED, KEY_CHRONOMETER, KEY_TIMER, KEY_MAX_SCORE
            , KEY_REVERSE_SCORING, KEY_SCORE_INTERVAL,KEY_DIFF_TO_WIN, KEY_STOPWATCH
            , KEY_SETS, KEY_NUM_SETS};
    private final Context mCtx;
    private DatabaseHelper mDbHelper;
    public static SQLiteDatabase DATABASE;

    public ScoreDBAdapter(Context ctx) {
        this.mCtx = ctx;
    }


    public ScoreDBAdapter open() throws SQLException {
        close();
        if (mDbHelper == null) {
            mDbHelper = new DatabaseHelper(mCtx);
        }
        DATABASE = mDbHelper.getWritableDatabase();
        return this;
    }
    public String convertToString(ArrayList array) {

        String str = TextUtils.join(",", array);

        return str;
    }


    public void close() {
        if (mDbHelper != null) {
            mDbHelper.close();
        }
    }

    public String convertObjectListToString(List<Player> playerList){
        Gson gson = new Gson();
        return gson.toJson(playerList);
    }

    public void updatePlayers(List<Player> playerList, int gameID){
        ContentValues initialValues = new ContentValues();
        String arrayList = null;

        try {

            arrayList = convertObjectListToString(playerList);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }


        initialValues.put(KEY_PLAYERS, arrayList);
        open();
        DATABASE.update(SQLITE_TABLE, initialValues, KEY_ROWID + "=" + gameID, null);
        close();
    }

    public void updateGame(String time_or_completed_or_timeLimit, int maxscoreorstopwatch, String request, int id) {

        ContentValues initialValues = new ContentValues();

        if (request.equals(KEY_TIME) || request.equals(KEY_CHRONOMETER)){
            initialValues.put(request, time_or_completed_or_timeLimit);

        }else if (request.equals(KEY_COMPLETED)){
            int c = Integer.valueOf(time_or_completed_or_timeLimit);
            initialValues.put(request, c);

        }else if (request.equals(KEY_TIMER)){
            initialValues.put(request, time_or_completed_or_timeLimit);

        }else if(request.equals(KEY_REVERSE_SCORING) || request.equals(KEY_MAX_SCORE) || request.equals(KEY_SCORE_INTERVAL)
                    || request.equals(KEY_STOPWATCH) || request.equals(KEY_DIFF_TO_WIN) || request.equals(KEY_NUM_SETS)){
            initialValues.put(request, maxscoreorstopwatch);

        }

        open();
        DATABASE.update(SQLITE_TABLE, initialValues, KEY_ROWID + "=" + id, null);
        close();

    }

    public long createGame(List<Player> players, String time, int completed, String timeLimit, List<EditTextOption> editTextOptions
            , List<CheckBoxOption> checkBoxOptions) {

        ContentValues initialValues = new ContentValues();

        if (numRows() == 0){
            initialValues.put(KEY_ROWID, 1);
        }else{
            initialValues.put(KEY_ROWID, getNewestGame() + 1);
        }

        initialValues.put(KEY_PLAYERS, convertObjectListToString(players));
        initialValues.put(KEY_TIME, time);
        initialValues.put(KEY_COMPLETED, completed);
        initialValues.put(KEY_TIMER, timeLimit);

        for (EditTextOption e : editTextOptions){
            initialValues.put(e.getmDatabaseColumn(), e.getmData());
        }
        for (CheckBoxOption c : checkBoxOptions){
            initialValues.put(c.getmDatabaseColumn(), c.getmData());
        }

        return DATABASE.insert(SQLITE_TABLE, null, initialValues);
    }

    public int numRows(){
        Cursor cursor = DATABASE.query(SQLITE_TABLE, COLUMN_ARRAY, null, null, null, null, null);

        return  cursor.getCount();
    }

    public int getNewestGame(){

        int value = 1;
        Cursor cursor = null;

        cursor = DATABASE.query(SQLITE_TABLE, COLUMN_ARRAY, null, null, null, null, null);
        cursor.moveToLast();
        int index = cursor.getColumnIndex(KEY_ROWID);
        try{
            value = cursor.getInt(index) ;

        }catch (Exception e){
            value = 1;

        }
        cursor.close();

        return value;
    }

    public boolean deleteGame(int id){

        open();
        DATABASE.delete(SQLITE_TABLE, KEY_ROWID + "=" + String.valueOf(id), null);
        Cursor cursor = DATABASE.query(SQLITE_TABLE, COLUMN_ARRAY, null, null, null, null, null);

        for (int i = 1; i <= numRows(); i++){
            ContentValues initialValues = new ContentValues();
            initialValues.put(KEY_ROWID, i);
            cursor.moveToNext();
            int index = cursor.getColumnIndex(KEY_ROWID);
            int rowID = cursor.getInt(index);
            DATABASE.update(SQLITE_TABLE, initialValues,KEY_ROWID + "=" + rowID, null);
        }

        cursor.close();
        close();
        return true;
    }

    public boolean deleteAllgames() {

        int doneDelete = 0;
        doneDelete = DATABASE.delete(SQLITE_TABLE, null , null);
        return doneDelete > 0;
    }

    public Cursor fetchGamesById(int id) throws SQLException {
        Cursor mCursor = null;
        if (id == 0) {
            mCursor = DATABASE.query(SQLITE_TABLE, COLUMN_ARRAY,
                    null, null, null, null, null);

        } else {
            mCursor = DATABASE.query(true, SQLITE_TABLE, COLUMN_ARRAY,
                    KEY_ROWID + " like '%" + id + "%'", null,
                    null, null, null, null);
        }
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }


    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + SQLITE_TABLE);

            if (newVersion > oldVersion) {
                for (int i = 0; i < COLUMN_ARRAY.length; i++) {
                    if (!columnExistsInTable(COLUMN_ARRAY[i], db)) {
                        db.execSQL("ALTER TABLE " + SQLITE_TABLE + " ADD COLUMN " + COLUMN_ARRAY[i] + " TEXT");
                    }
                }
            }

            onCreate(db);

        }

        public boolean columnExistsInTable(String column, SQLiteDatabase database) {
            Cursor mCursor = null;
            try {
                // Query 1 row
                mCursor = database.rawQuery("SELECT * FROM " + SQLITE_TABLE + " LIMIT 0", null);

                // getColumnIndex() gives us the index (0 to ...) of the column - otherwise we get a -1
                if (mCursor.getColumnIndex(column) != -1)
                    return true;
                else
                    return false;

            } catch (Exception Exp) {
                return false;
            } finally {
                if (mCursor != null) mCursor.close();
            }
        }
    }


}
