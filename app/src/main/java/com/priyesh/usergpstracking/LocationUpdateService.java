package com.priyesh.usergpstracking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;
import static com.priyesh.usergpstracking.Utilities.isLocationEnabled;

public class LocationUpdateService extends Service {

    private Context context;
    private DatabaseHelper db;
    private String TAG = "LOCATIONTRACKING";
    public static final int notify = 5000;
    private Handler mHandler = new Handler();
    private Timer mTimer = null;

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        context = getApplicationContext();
        db = new DatabaseHelper(context);
        startLocationUpdates();
        if (mTimer != null) // Cancel if already existed
            mTimer.cancel();
        else
            mTimer = new Timer();   //recreate new
        mTimer.scheduleAtFixedRate(new TimeDisplay(), 0, notify);   //Schedule task
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTimer.cancel();    //For Cancel Timer
        Toast.makeText(context, "Service is Destroyed", Toast.LENGTH_SHORT).show();
    }

    //class TimeDisplay for handling task
    class TimeDisplay extends TimerTask {
        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (ActivityCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Log.i(TAG, "Location access permission not provided");
                    } else {
                        if (!isLocationEnabled(context)) {
                            Log.i(TAG, "GPS is turned off");
                        } else {
                            if (latLng != null) {
                                int lastEntry = db.getEntriesCount();

                                if (lastEntry != 0) {
                                    LocationEntryModel entryDetails = db.getLocationEntry(lastEntry);


                                    Location previousLocation = new Location("");
                                    previousLocation.setLatitude(Double.valueOf(entryDetails.latitude));
                                    previousLocation.setLongitude(Double.valueOf(entryDetails.longitude));

                                    Location currentLocation = new Location("");
                                    currentLocation.setLatitude(latLng.latitude);
                                    currentLocation.setLongitude(latLng.longitude);

                                    float differenceInMeters = currentLocation.distanceTo(previousLocation);

                                    if (differenceInMeters > 10) {

                                        Log.i(TAG, "Location inserted successfully");
                                        Toast.makeText(context, "Location inserted successfully", Toast.LENGTH_SHORT).show();
                                        db.insertLocation(String.valueOf(latLng.latitude), String.valueOf(latLng.longitude));
                                    } else {
                                        Log.i(TAG, "Distance from previous location entry " + new DecimalFormat("##.##").format(differenceInMeters) + " meters. It should be greater than 10 meters");
                                        Toast.makeText(context, "Distance from previous location entry " + new DecimalFormat("##.##").format(differenceInMeters) + " meters. It should be greater than 10 meters", Toast.LENGTH_SHORT).show();
                                    }

                                } else {

                                    Log.i(TAG, "Location inserted successfully");
                                    Toast.makeText(context, "Location inserted successfully", Toast.LENGTH_SHORT).show();
                                    db.insertLocation(String.valueOf(latLng.latitude), String.valueOf(latLng.longitude));
                                }

                            } else {
                                Log.i(TAG, "Failed to insert location");
                            }
                        }
                    }


                }
            });
        }
    }

    private LocationRequest mLocationRequest;

    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    LatLng latLng;

    @SuppressLint("RestrictedApi")
    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        // do work here
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

    public void onLocationChanged(Location location) {
        // New location has now been determined
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());

        latLng = new LatLng(location.getLatitude(), location.getLongitude());
    }
}
