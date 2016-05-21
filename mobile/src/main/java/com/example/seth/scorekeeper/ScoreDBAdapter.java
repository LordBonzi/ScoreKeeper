package com.example.seth.scorekeeper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;


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
                    KEY_SCORE + "," +
                    KEY_PLAYERS +

                    " );";
    private final Context mCtx;
    private String[] allColumns = {KEY_ROWID, KEY_SCORE, KEY_PLAYERS};
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

    public String[] convertToStringArray(ArrayList<String> arrayList) {

        String[] str = new String[arrayList.size()];
        str = arrayList.toArray(str);

        Log.i(TAG + "strarray", Arrays.toString(str));

        return str;
    }

    public ArrayList<String> convertToArrayList(String[] str) {

        ArrayList<String> stringList = new ArrayList<String>(Arrays.asList(str));

        for (int i = 0; i < stringList.size(); i++) {
            String s = stringList.get(i);
            Log.i(TAG, s);
        }
        return stringList;
    }

    long updateGame(ArrayList array, String request) {
        array = new ArrayList();
        ContentValues initialValues = new ContentValues();
        initialValues.put(request, Arrays.toString(convertToStringArray(array)));

        int index = getNewestGame(KEY_ROWID).getColumnIndex(KEY_ROWID);
        String valueStr = getNewestGame(KEY_ROWID).getString(index);

        Log.i(TAG, "updateGame successful" + Arrays.toString(convertToStringArray(array)) + " , " + request);

        return mDb.update(SQLITE_TABLE, initialValues, KEY_ROWID + "=" + Integer.valueOf(valueStr) , null);
    }

    long createGame(ArrayList<String> players, ArrayList scoreArray) {

        players = new ArrayList<String>();
        scoreArray = new ArrayList<>();

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_SCORE, Arrays.toString(convertToStringArray(scoreArray)));
        initialValues.put(KEY_PLAYERS, Arrays.toString(convertToStringArray(players)));

        Log.i(TAG, String.valueOf(convertToStringArray(players)));

        Log.e(TAG, "createGame successful" + convertToStringArray(players) + " , " + convertToStringArray(scoreArray));

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
