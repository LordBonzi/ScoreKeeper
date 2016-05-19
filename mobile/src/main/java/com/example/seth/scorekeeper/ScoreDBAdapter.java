package com.example.seth.scorekeeper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;


/**
 * Created by Seth Schroeder on 27/11/2015.
 */
public class ScoreDBAdapter {
    public static final String KEY_ROWID = "_id";
    public static final String KEY_SCORE = "_score";
    public static final String KEY_PLAYERS = "_players";

    private String[] allColumns = {KEY_ROWID, KEY_SCORE, KEY_PLAYERS};

    private static final String TAG = "ScoreDBAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static final String DATABASE_NAME = "ScoreKeeper";
    private static final String SQLITE_TABLE = "score";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;

    private static final String DATABASE_CREATE =
            "CREATE TABLE if not exists " + SQLITE_TABLE + " (" +
                    KEY_ROWID + " integer PRIMARY KEY autoincrement," +
                    KEY_SCORE + "," +
                    KEY_PLAYERS +

                    " );";

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.w(TAG, DATABASE_CREATE);
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + SQLITE_TABLE);
            onCreate(db);
        }
    }

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

    public long updateGame(ArrayList array, String request) {

        ContentValues initialValues = new ContentValues();
        initialValues.put(request, String.valueOf(array));

        int index = getNewestGame(KEY_ROWID).getColumnIndex(request);
        Log.i(TAG, String.valueOf(index));
        String valueStr = getNewestGame(KEY_ROWID).getString(index);
        Log.i(TAG, valueStr);

        Log.i(TAG, "updateGame successful" + array + " , " + request);

        return mDb.update(SQLITE_TABLE, initialValues, KEY_ROWID + "=" + Integer.valueOf(valueStr) , null);
    }

    public long createGame(ArrayList<String> players,ArrayList<String> scoreArray, boolean biggame) {

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_SCORE, String.valueOf(scoreArray));
        initialValues.put(KEY_PLAYERS, String.valueOf(players));

        Log.e(TAG, "createGame successful" + players + " , " + scoreArray + " , " + biggame);

        return mDb.insert(SQLITE_TABLE, null, initialValues);
    }

    public Cursor getNewestGame(String request){
        Cursor cursor = mDb.query(SQLITE_TABLE, allColumns, null, null, null, null, null);
        cursor.moveToPosition(cursor.getCount() - 1);
        Log.i(TAG, "getNewestGame successful");

        return cursor;
    }

    public boolean deleteAllgames() {

        int doneDelete = 0;
        doneDelete = mDb.delete(SQLITE_TABLE, null , null);
        Log.w(TAG, Integer.toString(doneDelete));
        return doneDelete > 0;

    }

    public Cursor fetchGamesById(int id) throws SQLException {
        Log.w(TAG, String.valueOf(id));
        Cursor mCursor;

            mCursor = mDb.query(true, SQLITE_TABLE, allColumns,
                    KEY_ROWID + " like '%" + id + "%'", null,
                    null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }

        Log.i(TAG, "fetchGamesBYId successful");


        return mCursor;
    }

    public Cursor fetchAllGames() {

        Cursor mCursor = mDb.query(SQLITE_TABLE, allColumns,
                null, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }


}
