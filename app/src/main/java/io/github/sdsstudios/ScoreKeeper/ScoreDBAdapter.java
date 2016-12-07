package io.github.sdsstudios.ScoreKeeper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.List;

public class ScoreDBAdapter{

    public static final String KEY_ROWID = "_id";
    public static final String KEY_GAME = "_game";
    private static final String SQLITE_TABLE = "score";
    private static final String TAG = "ScoreDBAdapter";
    private static final String DATABASE_NAME = "ScoreKeeper";
    private static final int DATABASE_VERSION = 5;
    private static final String DATABASE_CREATE =
            "CREATE TABLE if not exists " + SQLITE_TABLE + " (" +
                    KEY_ROWID + " ," +
                    KEY_GAME +
                    " );";

    private static String[] COLUMN_ARRAY = {KEY_ROWID, KEY_GAME};
    private final Context mCtx;
    private DatabaseHelper mDbHelper;
    private static SQLiteDatabase DATABASE;

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

    void close() {
        if (mDbHelper != null) {
            mDbHelper.close();
        }
    }


    private String convertGameToString(Game game){
        Gson gson = new GsonBuilder().serializeNulls().create();
        return gson.toJson(game);
    }

    void updateGame(Game game){
        game.setGameListener(null);

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

    long createGame(Game game) {
        game.setGameListener(null);

        ContentValues initialValues = new ContentValues();

        if (numRows() == 0){
            initialValues.put(KEY_ROWID, 1);
        }else{
            initialValues.put(KEY_ROWID, getNewestGame() + 1);
        }


        initialValues.put(KEY_GAME, convertGameToString(game));

        return DATABASE.insert(SQLITE_TABLE, null, initialValues);
    }

    int numRows(){
        Cursor cursor = DATABASE.query(SQLITE_TABLE, COLUMN_ARRAY, null, null, null, null, null);

        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    int getNewestGame(){

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

    void deleteSelectedGames(List<Integer> idList){
        DataHelper dataHelper = new DataHelper();


        for (Integer id : idList) {
            open();
            DATABASE.delete(SQLITE_TABLE, KEY_ROWID + "=" + String.valueOf(id), null);
            close();
        }

        int numRows = open().numRows();
        Cursor cursor = DATABASE.query(SQLITE_TABLE, COLUMN_ARRAY, null, null, null, null, null);

        //updates the IDs of the other games after they have been deleted
        for (int i = 1; i <= numRows; i++){
            ContentValues initialValues = new ContentValues();
            initialValues.put(KEY_ROWID, i);
            cursor.moveToNext();
            int index = cursor.getColumnIndex(KEY_ROWID);
            int rowID = cursor.getInt(index);

            open();
            DATABASE.update(SQLITE_TABLE, initialValues,KEY_ROWID + "=" + rowID, null);

            //gets the game and updates the ID int in the Game object
            Game game = dataHelper.getGame(i, this);
            game.setmID(i);
            updateGame(game);

        }

        cursor.close();

        close();
    }

    void deleteGame(int id){
        DataHelper dataHelper = new DataHelper();

        open();

        DATABASE.delete(SQLITE_TABLE, KEY_ROWID + "=" + String.valueOf(id), null);

        int numRows = open().numRows();
        Cursor cursor = DATABASE.query(SQLITE_TABLE, COLUMN_ARRAY, null, null, null, null, null);

        //updates the IDs of the other games after they have been deleted
        for (int i = 1; i <= numRows; i++){
            ContentValues initialValues = new ContentValues();
            initialValues.put(KEY_ROWID, i);
            cursor.moveToNext();
            int index = cursor.getColumnIndex(KEY_ROWID);
            int rowID = cursor.getInt(index);

            open();
            DATABASE.update(SQLITE_TABLE, initialValues,KEY_ROWID + "=" + rowID, null);

            //gets the game and updates the ID int in the Game object
            Game game = dataHelper.getGame(i, this);
            game.setmID(i);
            updateGame(game);

        }

        cursor.close();
        close();

    }

    boolean deleteAllGames() {

        int doneDelete = 0;
        doneDelete = DATABASE.delete(SQLITE_TABLE, null , null);
        return doneDelete > 0;
    }

    Cursor fetchGamesById(int id) throws SQLException {
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
        private Context mCtx;

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.mCtx = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            if (oldVersion < 5){
                File sd = Environment.getExternalStorageDirectory();
                String DB_PATH;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    DB_PATH = mCtx.getFilesDir().getAbsolutePath().replace("files", "databases") + File.separator;
                } else {
                    DB_PATH =  mCtx.getDatabasePath("ScoreKeeper").toString();
                }

                if (sd.canWrite()) {
                    String currentDBPath = "ScoreKeeper";
                    String backupDBPath = "/ScoreKeeper/PRE_1.1_BACKUP.db";
                    File currentDB = new File(DB_PATH, currentDBPath);
                    File backupDB = new File(sd, backupDBPath);

                    try{
                        FileChannel src = new FileInputStream(currentDB).getChannel();
                        FileChannel dst = new FileOutputStream(backupDB).getChannel();
                        dst.transferFrom(src, 0, src.size());
                        src.close();
                        dst.close();

                    }catch (Exception e){
                        e.printStackTrace();
                        Log.e("Settings", e.toString());
                    }
                }

                db.delete(SQLITE_TABLE, null, null);

                DialogHelper.createAlertDialog(mCtx, "ALL YOUR GAMES HAVE BEEN DELETED!"
                        , "All your games have been deleted when you updated to version 1.1 due to an overhaul in the code for the app." +
                                "This is the only time this will happen." +
                                "A backup of your old games has been made" +
                        "in the ScoreKeeper folder on your phone. You can import it, if you roll back" +
                                " to the old version by leaving the beta. I am extremely sorry for any inconvenience this may have caused.");


            }

            db.execSQL("DROP TABLE IF EXISTS " + SQLITE_TABLE);
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
