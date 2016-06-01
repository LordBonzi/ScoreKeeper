package io.github.sdsstudios.ScoreKeeper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.util.ArrayList;


/**
 * Created by Seth Schroeder on 27/11/2015.
 */
public class ScoreDBAdapter {
    public static final String KEY_ROWID = "_id";
    public static final String KEY_SCORE = "_score";
    public static final String KEY_PLAYERS = "_players";
    public static final String KEY_TIME = "_time";
    public static final String KEY_COMPLETED = "_completed";
    public static final String KEY_CHRONOMETER = "_chronometer";
    public static final String SQLITE_TABLE = "score";
    private static final String TAG = "ScoreDBAdapter";
    private static final String DATABASE_NAME = "ScoreKeeper";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_CREATE =
            "CREATE TABLE if not exists " + SQLITE_TABLE + " (" +
                    KEY_ROWID + " ," +
                    KEY_PLAYERS + "," +
                    KEY_SCORE + " , " +
                    KEY_TIME + " , " +
                    KEY_COMPLETED + " , " +
                    KEY_CHRONOMETER +
                    " );";
    private final Context mCtx;
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    public ScoreDBAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public ScoreDBAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        if (mDbHelper != null) {
            mDbHelper.close();
        }
    }

    public String convertToString(ArrayList array) {

        String str = TextUtils.join(",", array);

        return str;
    }

    public long updateGame(ArrayList array, String time, int completed, String request, int id) {

        ContentValues initialValues = new ContentValues();

        if (request.equals(KEY_TIME)){
            initialValues.put(request, time);

        }else if (request.equals(KEY_PLAYERS) || request.equals(KEY_SCORE)){
            initialValues.put(request, convertToString(array));
        }else if (request.equals(KEY_COMPLETED)){

            initialValues.put(request, completed);

        }


        return mDb.update(SQLITE_TABLE, initialValues, KEY_ROWID + "=" + id, null);
    }

    public long createGame(ArrayList players, String time, ArrayList score, int completed) {
        int id;
        try{
            id = Integer.parseInt(getNewestGame()) + 1;

        }catch (Exception e){
            id = 1;
        }

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_ROWID, id);
        initialValues.put(KEY_PLAYERS, convertToString(players));
        initialValues.put(KEY_SCORE, convertToString(score));
        initialValues.put(KEY_TIME, time);
        initialValues.put(KEY_COMPLETED, completed);

        return mDb.insert(SQLITE_TABLE, null, initialValues);
    }

    public String getNewestGame(){

        String value = null;

        Cursor cursor = mDb.query(SQLITE_TABLE, new String[]{KEY_ROWID, KEY_PLAYERS, KEY_SCORE, KEY_TIME, KEY_COMPLETED, KEY_CHRONOMETER}, null, null, null, null, null);
        cursor.moveToLast();

        int index = cursor.getColumnIndex(KEY_ROWID);

        try{
            value = cursor.getString(index);

        }catch (Exception e){
            value = cursor.getString(0);

        }

        return value;
    }

    public boolean deleteGame(int id){

        mDb.delete(SQLITE_TABLE, KEY_ROWID + "=" + String.valueOf(id), null);

        for (int i = 1; i < id; i++){
            ContentValues initialValues = new ContentValues();
            initialValues.put(KEY_ROWID, i);

        }

        return true;
    }

    public boolean deleteAllgames() {

        int doneDelete = 0;
        doneDelete = mDb.delete(SQLITE_TABLE, null , null);
        return doneDelete > 0;
    }

    public int getNumGames(String request, String args){
        int i =0;
        Cursor cursor = null;

        cursor = mDb.rawQuery("SELECT COUNT(*) FROM " + SQLITE_TABLE + " WHERE " + request + "='" + args + "'", null);

        for (int o = 0; o < Integer.valueOf(getNewestGame()); o++) {

            cursor.moveToNext();
            if (cursor.getCount() > 0) {
                i += cursor.getCount();
            }
        }
        return i;
    }

    public Cursor fetchGamesById(int id) throws SQLException {
        Cursor mCursor = null;
        if (id == 0) {
            mCursor = mDb.query(SQLITE_TABLE, new String[]{KEY_ROWID, KEY_PLAYERS, KEY_SCORE, KEY_TIME, KEY_COMPLETED, KEY_CHRONOMETER},
                    null, null, null, null, null);

        } else {
            mCursor = mDb.query(true, SQLITE_TABLE, new String[]{KEY_ROWID, KEY_SCORE, KEY_PLAYERS, KEY_TIME, KEY_COMPLETED, KEY_CHRONOMETER},
                    KEY_ROWID + " like '%" + id + "%'", null,
                    null, null, null, null);
        }
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor fetchAllGames() {

        Cursor mCursor = mDb.query(SQLITE_TABLE, new String[]{KEY_ROWID, KEY_SCORE, KEY_PLAYERS, KEY_TIME, KEY_COMPLETED, KEY_CHRONOMETER},
                null, null, null, null, null);

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
            onCreate(db);
        }
    }


}
