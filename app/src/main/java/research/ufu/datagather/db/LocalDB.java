package research.ufu.datagather.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import research.ufu.datagather.model.User;

public class LocalDB extends SQLiteOpenHelper {
    private static final String TAG = "DB";
    private static String tables_reset[];
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "datagather.db";
    public static final String TABLE_LOCATION = "location";
    public static final String TABLE_WIFI = "wifi";
    public static final String TABLE_USER = "user";

    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";

    public static final String COLUMN_LON = "long";
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_TIMESTAMP = "timestamp";


    public LocalDB(Context context, String name, SQLiteDatabase.CursorFactory factory,
                          int version)
    {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOCATION_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_LOCATION + "("
                + COLUMN_LAT + " REAL,"
                + COLUMN_LON + " REAL,"
                + COLUMN_TIMESTAMP + " INTEGER"
                + ")";
        db.execSQL(CREATE_LOCATION_TABLE);

        String CREATE_WIFI_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_WIFI + "("
                + COLUMN_NAME + " TEXT,"
                + COLUMN_TIMESTAMP + " INTEGER"
                + ")";
        db.execSQL(CREATE_WIFI_TABLE);

        String CREATE_USER_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_USER + "("
                + COLUMN_USERNAME + " TEXT,"
                + COLUMN_PASSWORD + " TEXT"
                + ")";
        db.execSQL(CREATE_USER_TABLE);

        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, LocalDB.generateString(20));
        values.put(COLUMN_PASSWORD, LocalDB.generateString(20));
        db.insert(TABLE_USER, null, values);

    }

    public void reset()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        tables_reset = new String[3];
        tables_reset[0] = TABLE_USER;
        tables_reset[1] = TABLE_WIFI;
        tables_reset[2] = TABLE_LOCATION;
        onUpgrade(db, 1, 1);
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for(int i = 0; i < tables_reset.length; ++i)
        {
            String com = "DROP TABLE IF EXISTS " + tables_reset[i];
            db.execSQL(com);
        }
        onCreate(db);
    }

    public User getUser(){
        String username, password;
        username = password = "";
        String usernameQ = "Select " + COLUMN_USERNAME + " FROM " + TABLE_USER;
        String passwordQ = "Select " + COLUMN_PASSWORD + " FROM " + TABLE_USER;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursorUsr = db.rawQuery(usernameQ, null);
        Cursor cursorPsw = db.rawQuery(passwordQ, null);
        cursorUsr.moveToFirst();
        cursorPsw.moveToFirst();
        try
        {
            username = cursorUsr.getString(cursorUsr.getColumnIndex(COLUMN_USERNAME));
            password = cursorPsw.getString(cursorPsw.getColumnIndex(COLUMN_PASSWORD));
        }
        catch(Exception e) {

            System.out.println(e);
        }
        db.close();
        return  new User(username, password);
    }

    public void addLocation(double latitude, double longitude, long date){
        ContentValues values = new ContentValues();
        values.put(COLUMN_LAT, latitude);
        values.put(COLUMN_LON, longitude);
        values.put(COLUMN_TIMESTAMP, date);
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            db.insert(TABLE_LOCATION, null, values);
        }
        catch(Exception e){
        }
        db.close();
    }

    public void addWifi(String name, long date){
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_TIMESTAMP, date);
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            db.insert(TABLE_WIFI, null, values);
        }
        catch(Exception e){
        }
        db.close();
    }

    public boolean isSynced(long lastLocationDate, long lastWifiDate){
        //SELECT * FROM Table ORDER BY ID DESC LIMIT 1
        String queryLoc = "Select * FROM " + TABLE_LOCATION + " ORDER_BY " + COLUMN_TIMESTAMP + " DESC LIMIT 1";
        String queryWifi = "Select * FROM " + TABLE_WIFI + " ORDER_BY " + COLUMN_TIMESTAMP + " DESC LIMIT 1";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursorLoc = db.rawQuery(queryLoc, null);
        Cursor cursorWifi = db.rawQuery(queryWifi, null);
        cursorLoc.moveToFirst();
        cursorWifi.moveToFirst();
        try
        {
            if (cursorLoc.getLong(cursorLoc.getColumnIndex(COLUMN_TIMESTAMP)) < lastLocationDate)
                return false;
            if (cursorWifi.getLong(cursorWifi.getColumnIndex(COLUMN_TIMESTAMP)) < lastLocationDate)
                return false;
        }
        catch(Exception e) {
            cursorLoc.close();
            cursorWifi.close();
            System.out.println(e);
        }
        return true;
    }

    public boolean deleteAll(){
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.delete(TABLE_LOCATION, null, null);
            db.delete(TABLE_WIFI, null, null);

        }
        catch(Exception e){
            Log.e(TAG, e.getMessage());
        }
        return true;
    }

    public String getLocation(){
        JSONArray array = new JSONArray();

        String locationQ = "Select * FROM " + TABLE_LOCATION;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(locationQ, null);
        if(cursor.moveToFirst())
        {
            while(!cursor.isAfterLast())
            {
                double lat = cursor.getDouble(cursor.getColumnIndex(COLUMN_LAT));
                double lon = cursor.getDouble(cursor.getColumnIndex(COLUMN_LON));
                double timestamp = cursor.getDouble(cursor.getColumnIndex(COLUMN_TIMESTAMP));
                try {
                    array.put(new JSONObject().put("lon", lon).put("lat", lat).put("timestamp", timestamp));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return array.toString();
    }

    public String getWifi(){
        JSONArray array = new JSONArray();

        String wifiQ = "Select * FROM " + TABLE_WIFI;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(wifiQ, null);
        if(cursor.moveToFirst())
        {
            while(!cursor.isAfterLast())
            {
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                double timestamp = cursor.getDouble(cursor.getColumnIndex(COLUMN_TIMESTAMP));
                try {
                    array.put(new JSONObject().put("name", name).put("timestamp", timestamp));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                cursor.moveToNext();
            }
        }
        cursor.close();
        db.close();
        return array.toString();
    }


    public static String generateString(int length)
    {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwvxyz0123456789";
        Random randomGenerator = new Random();
        char[] text = new char[length];
        for (int i = 0; i < length; i++)
            text[i] = characters.charAt(randomGenerator.nextInt(characters.length()));
        return new String(text);
    }
}
