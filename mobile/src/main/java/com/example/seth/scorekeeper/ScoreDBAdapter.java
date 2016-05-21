package com.example.seth.scorekeeper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;


/**
 * Created by Seth Schroeder on 27/11/2015.
 */
public class ScoreDBAdapter {
    public static final String KEY_ROWID = "_id";
    public static final String KEY_SCORE = "_score";
    public static final String KEY_PLAYERS = "_players";
    private static final String TAG = "ScoreDBAdapter";
    private static final String DATABASE_NAME = "ScoreKeeper";
    private static final String SQLITE_TABLE = "score";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_CREATE =
            "CREATE TABLE if not exists " + SQLITE_TABLE + " (" +
                    KEY_ROWID + " integer PRIMARY KEY autoincrement," +
                    KEY_PLAYERS + "," +
                    KEY_SCORE +

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

    public long updateGame(ArrayList array, String request, int id) {

        ContentValues initialValues = new ContentValues();
        initialValues.put(request, convertToString(array));

        return mDb.update(SQLITE_TABLE, initialValues, KEY_ROWID + "=" + id, null);
    }

    public long createGame(ArrayList players, ArrayList scoreArray) {

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_PLAYERS, convertToString(players));
        initialValues.put(KEY_SCORE, convertToString(players));

        return mDb.insert(SQLITE_TABLE, null, initialValues);
    }

    public String getNewestGame(){

        Cursor cursor = mDb.query(SQLITE_TABLE, new String[]{KEY_ROWID, KEY_PLAYERS, KEY_SCORE}, null, null, null, null, null);
        cursor.moveToLast();

        int index = cursor.getColumnIndex(KEY_ROWID);
        String value = cursor.getString(index);

        return value;
    }

    public boolean deleteAllgames() {

        int doneDelete = 0;
        doneDelete = mDb.delete(SQLITE_TABLE, null , null);
        Log.w(TAG, Integer.toString(doneDelete));
        return doneDelete > 0;
    }

    public Cursor fetchGamesById(int id) throws SQLException {
        Log.w(TAG, String.valueOf(id));
        Cursor mCursor = null;
        if (id == 0) {
            mCursor = mDb.query(SQLITE_TABLE, new String[]{KEY_ROWID, KEY_PLAYERS, KEY_SCORE},
                    null, null, null, null, null);

        } else {
            mCursor = mDb.query(true, SQLITE_TABLE, new String[]{KEY_ROWID, KEY_SCORE, KEY_PLAYERS},
                    KEY_ROWID + " like '%" + id + "%'", null,
                    null, null, null, null);
        }
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public Cursor fetchAllGames() {

        Cursor mCursor = mDb.query(SQLITE_TABLE, new String[]{KEY_ROWID, KEY_SCORE, KEY_PLAYERS},
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


}
