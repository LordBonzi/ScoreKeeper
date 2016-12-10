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
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

/**
 * Created by Seth Schroeder on 27/11/2015.
 */

public class PresetDBAdapter {
    public static final String KEY_ROWID = "_id";
    public static final String KEY_GAME = "_game";
    public static final String SQLITE_TABLE = "presets";
    private static final String TAG = "PresetDBAdapter";
    private static final String DATABASE_NAME = "Presets";
    private static final int DATABASE_VERSION = 5;
    private static final String DATABASE_CREATE =
            "CREATE TABLE if not exists " + SQLITE_TABLE + " (" +
                    KEY_ROWID + " ," +
                    KEY_GAME +
                    " );";

    public static String[] COLUMN_ARRAY = {KEY_ROWID, KEY_GAME};
    public static SQLiteDatabase DATABASE;
    private final Context mCtx;
    private DatabaseHelper mDbHelper;

    public PresetDBAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public PresetDBAdapter open() throws SQLException {
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

    private String convertGameToString(Game game) {
        Gson gson = new GsonBuilder().serializeNulls().create();
        return gson.toJson(game);
    }

    public void updatePreset(Game game){
        ContentValues initialValues = new ContentValues();
        String arrayList = null;

        try {

            arrayList = convertGameToString(game);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }


        initialValues.put(KEY_GAME, arrayList);
        open();
        DATABASE.update(SQLITE_TABLE, initialValues, KEY_ROWID + "=" + game.getmID(), null);
        close();
    }

    public long createPreset(Game game) {

        ContentValues initialValues = new ContentValues();

        if (numRows() == 0){
            initialValues.put(KEY_ROWID, 1);
        }else{
            initialValues.put(KEY_ROWID, getNewestGame() + 1);
        }

        initialValues.put(KEY_GAME, convertGameToString(game));

        return DATABASE.insert(SQLITE_TABLE, null, initialValues);
    }

    public int numRows(){
        Cursor cursor = DATABASE.query(SQLITE_TABLE, COLUMN_ARRAY, null, null, null, null, null);

        int count = cursor.getCount();
        cursor.close();
        return count;
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

    public boolean deletePreset(int id){

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

    public boolean deleteAllPresets() {

        int doneDelete = 0;
        doneDelete = DATABASE.delete(SQLITE_TABLE, null , null);
        return doneDelete > 0;
    }

    public Cursor fetchPresetByID(int id) throws SQLException {
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

            if (oldVersion < 5) {
                db.delete(SQLITE_TABLE, null, null);
            }

            db.execSQL("DROP TABLE IF EXISTS " + SQLITE_TABLE);

            onCreate(db);

        }

    }


}