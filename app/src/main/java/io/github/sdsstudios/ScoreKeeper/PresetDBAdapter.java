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

public class PresetDBAdapter {
    public static final String KEY_ROWID = "_id";
    public static final String KEY_PLAYERS = "_players";
    public static final String KEY_TIME_LIMIT = "_timelimit";
    public static final String KEY_TITLE = "_title";
    public static final String SQLITE_TABLE = "presets";
    private static final String TAG = "PresetDBAdapter";
    private static final String DATABASE_NAME = "PresetDatabase";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_CREATE =
            "CREATE TABLE if not exists " + SQLITE_TABLE + " (" +
                    KEY_ROWID + " ," +
                    KEY_PLAYERS + "," +
                    KEY_TIME_LIMIT + "," +
                    KEY_TITLE +
                    " );";

    private final Context mCtx;
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    public PresetDBAdapter(Context ctx) {
        this.mCtx = ctx;
    }


    public PresetDBAdapter open() throws SQLException {
        close();
        if (mDbHelper == null) {
            mDbHelper = new DatabaseHelper(mCtx);
        }
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

    public void updatePreset(ArrayList players, String timelimitortitle, String request, int id) {

        ContentValues initialValues = new ContentValues();

        if (request == KEY_PLAYERS){
            initialValues.put(request, convertToString(players));
        }else if(request == KEY_TIME_LIMIT|| request == KEY_TITLE){
            initialValues.put(request, timelimitortitle);

        }
        open();
        mDb.update(SQLITE_TABLE, initialValues, KEY_ROWID + "=" + id, null);
        close();

    }

    public long createPreset(ArrayList players, String timelimit, String title) {

        ContentValues initialValues = new ContentValues();

        if (numRows() == 0){
            initialValues.put(KEY_ROWID, 1);
        }else{
            initialValues.put(KEY_ROWID, numRows() + 1);
        }

        initialValues.put(KEY_PLAYERS, convertToString(players));
        initialValues.put(KEY_TIME_LIMIT, timelimit);
        initialValues.put(KEY_TITLE, title);

        return mDb.insert(SQLITE_TABLE, null, initialValues);
    }

    public int numRows(){
        Cursor cursor = mDb.query(SQLITE_TABLE, new String[]{KEY_ROWID, KEY_PLAYERS, KEY_TIME_LIMIT, KEY_TITLE}, null, null, null, null, null);

        return  cursor.getCount();
    }

    public boolean deletePreset(int id){

        open();
        mDb.delete(SQLITE_TABLE, KEY_ROWID + "=" + String.valueOf(id), null);
        Cursor cursor = mDb.query(SQLITE_TABLE, new String[]{KEY_ROWID, KEY_PLAYERS, KEY_TIME_LIMIT, KEY_TITLE}, null, null, null, null, null);

        for (int i = 1; i <= numRows(); i++){
            ContentValues initialValues = new ContentValues();
            initialValues.put(KEY_ROWID, i);
            cursor.moveToNext();
            int index = cursor.getColumnIndex(KEY_ROWID);
            int rowID = cursor.getInt(index);
            mDb.update(SQLITE_TABLE, initialValues,KEY_ROWID + "=" + rowID, null);
        }

        cursor.close();
        close();
        return true;
    }

    public boolean deleteAllPresets() {

        int doneDelete = 0;
        doneDelete = mDb.delete(SQLITE_TABLE, null , null);
        return doneDelete > 0;
    }

    public Cursor fetchPresetById(int id) throws SQLException {
        Cursor mCursor = null;
        if (id == 0) {
            Cursor cursor = mDb.query(SQLITE_TABLE, new String[]{KEY_ROWID, KEY_PLAYERS, KEY_TIME_LIMIT, KEY_TITLE}, null, null, null, null, null);

        } else {
            mCursor = mDb.query(true, SQLITE_TABLE, new String[]{KEY_ROWID, KEY_PLAYERS, KEY_TIME_LIMIT, KEY_TITLE},
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

            onCreate(db);
        }
    }


}