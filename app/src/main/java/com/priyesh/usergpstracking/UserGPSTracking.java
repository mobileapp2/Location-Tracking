package com.priyesh.usergpstracking;

import android.app.Application;
import android.content.Context;

import java.io.File;

public class UserGPSTracking extends Application {

    private Context context;
    private boolean dbExist;
    private DatabaseHelper dataBaseHelper;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        try {
            File dbFile = context.getDatabasePath("LocationDB");
            dbExist = dbFile.exists();
            dataBaseHelper = new DatabaseHelper(context);

            dataBaseHelper.createDataBase(dbExist);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
