package research.ufu.datagather.activities;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import research.ufu.datagather.db.LocalDB;
import research.ufu.datagather.db.LocalDBSingleton;
import research.ufu.datagather.model.Constants;

public class Logger extends Service {
    private static final String TAG = "Logger";
    private static final float LOCATION_DISTANCE = 10f;
    private LocationManager mLocationManager = null;
    private double lon = 0.;
    private double lat = 0.;

    private class LocationListener implements android.location.LocationListener{
        Location mLastLocation;
        public LocationListener(String provider)
        {
            Log.v(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }
        @Override
        public void onLocationChanged(Location location)
        {
            lon = location.getLongitude();
            lat = location.getLatitude();
            Log.v(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
        }
        @Override
        public void onProviderDisabled(String provider)
        {
            Log.v(TAG, "onProviderDisabled: " + provider);
        }
        @Override
        public void onProviderEnabled(String provider)
        {
            Log.v(TAG, "onProviderEnabled: " + provider);
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
           Log.v(TAG, "onStatusChanged: " + provider);
        }
    }
    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        final LocalDB db = LocalDBSingleton.getDB();
        super.onStartCommand(intent, flags, startId);

        final WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifi.isWifiEnabled() == false)
        {
           Log.e(TAG, "wifi is disabled..making it enabled");
           wifi.setWifiEnabled(true);
        }

        Timer timer = new Timer();
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, (Constants.TIME_STEP-1)*1000, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.e(TAG, "fail to request location update, ignore" + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, (Constants.TIME_STEP-1)*1000, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.e(TAG, "fail to request location update, ignore" + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "gps provider does not exist " + ex.getMessage());
        }

        timer.schedule(new TimerTask(){

            @Override
            public void run() {
                long timestamp = System.currentTimeMillis();

                wifi.startScan();
                List<ScanResult> results = wifi.getScanResults();
                StringBuilder bssids = new StringBuilder();
                for (ScanResult result : results){
                    bssids.append(result.BSSID);
                }

                db.addLocation(lat, lon, timestamp);
                db.addWifi(bssids.toString(), timestamp);
                Log.v(TAG, db.getLocation());
                Log.v(TAG, db.getWifi());
            }
        }, 0, Constants.TIME_STEP*1000);
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    private void initializeLocationManager() {
        System.out.println("initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

}
