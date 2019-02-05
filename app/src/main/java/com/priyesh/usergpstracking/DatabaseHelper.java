package com.priyesh.usergpstracking;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static String DB_PATH = "";
    private static String DB_NAME = "LocationDB";
    private static final int DATABASE_VERSION = 1;
    private SQLiteDatabase sqLiteDatabase;
    private final Context context;

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < newVersion) {
            System.out.println("Upgrading.....");
            try {
                copyDataBase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
        this.context = context;

        if (android.os.Build.VERSION.SDK_INT >= 4.2) {
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        } else {
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        }

        System.out.println("Database path : " + DB_PATH);
    }

    public void UpdateDB() {

    }

    public void createDataBase(boolean dbExist) {
        if (!dbExist) {
            this.getReadableDatabase();
            try {
                System.out.println("Database copying started");
                copyDataBase();
                System.out.println("Database copying completed");
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    private void copyDataBase() throws IOException {
        InputStream myInput = context.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public boolean copyChecklistDbToFolder() throws IOException {
        SimpleDateFormat sd = new SimpleDateFormat("dd-MM-yy");

        try {
            File folder = new File(Environment.getExternalStorageDirectory()
                    + File.separator + "Location Tracking" + File.separator
                    + "Database Backup");
            if (!folder.exists())
                folder.mkdirs();
            File backupDB = new File(folder, "LocTrackingDB" + sd.format(new Date()));

            File currentDB = new File(DB_PATH + DB_NAME);
            if (currentDB.exists()) {
                FileInputStream fis = new FileInputStream(currentDB);
                FileOutputStream fos = new FileOutputStream(backupDB);
                fos.getChannel().transferFrom(fis.getChannel(), 0,
                        fis.getChannel().size());
                fis.close();
                fos.close();
                Log.i("Database successfully", " copied to Location Tracking folder");
                return true;
            } else {
                Log.i("Copying Database", " fail database not found");
                return false;
            }
        } catch (IOException e) {
            Log.d("Copying Database", "fail, reason:", e);
            return false;
        }
    }

    public void insertLocation(String latitude, String longitude) {
        SQLiteDatabase database = this.getWritableDatabase();
        long result = -1;

        try {
            ContentValues values = new ContentValues();
            values.put("latitude", latitude);
            values.put("longitude", longitude);

            database.beginTransaction();

            result = database.insertOrThrow("location_entries", null, values);

            if (result == -1) {
                database.endTransaction();
            } else
                database.setTransactionSuccessful();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            database.endTransaction();
            database.close();
        }

    }

    public int getEntriesCount() {
        String countQuery = "SELECT * FROM " + "location_entries";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public LocationEntryModel getLocationEntry(int lastEntry) {
        LocationEntryModel data = new LocationEntryModel();
        SQLiteDatabase database = this.getWritableDatabase();

        Cursor cursor = database.query("location_entries", new String[]{"ID",
                        "latitude", "longitude","DateTime"},
                "ID=?", new String[]{String.valueOf(lastEntry)}, null,
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                data.latitude = cursor.getString(1);
                data.longitude = cursor.getString(2);
            } while (cursor.moveToNext());
        }
        cursor.close();
        database.close();
        return data;
    }


}
