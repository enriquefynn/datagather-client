package research.ufu.datagather.activities;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import research.ufu.datagather.db.LocalDB;
import research.ufu.datagather.db.LocalDBSingleton;
import research.ufu.datagather.model.Constants;
import research.ufu.datagather.model.ResponseHelper;
import research.ufu.datagather.model.User;
import research.ufu.datagather.utils.ForbiddenException;
import research.ufu.datagather.utils.Global;
import research.ufu.datagather.utils.Protocol;

public class Logger extends Service {
    LocalDB db;

    private static final String TAG = "Logger";
    private static final float LOCATION_DISTANCE = 10f;
    private LocationManager mLocationManager = null;
    private double lon = 0.;
    private double lat = 0.;
    List<ScanResult> results;
    private long lastSync = 0;
    boolean shouldSend = true;
    SharedPreferences.Editor editor;
    boolean wifiStatus = false;

    private Messenger messageHandler = new Messenger(new ConvertHanlder());

    class ConvertHanlder extends Handler {

        @Override
        public void handleMessage(Message msg) {
        }
    }

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
        shouldSend = true;
        return messageHandler.getBinder();
    }

    public class MyBinder extends Binder {
        Logger getService() {
            return Logger.this;
        }
    }

    public void sendTick(long tick) {
        if (shouldSend == false)
            return;
        Message message = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putLong("TICK", tick);
        message.setData(bundle);
        message.arg1 = Constants.MSG_TICK;
        try {
            messageHandler.send(message);
        } catch (RemoteException e) {
            shouldSend = false;
            e.printStackTrace();
        }
    }
    public void sendLastSync(long date) {
        if (shouldSend == false)
            return;
        Message message = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putLong("LASTSYNC", date);
        message.setData(bundle);
        message.arg1 = Constants.MSG_LAST_SYNC;
        try {
            messageHandler.send(message);
        } catch (RemoteException e) {
            shouldSend = false;
            e.printStackTrace();
        }
    }
    public void sendError(int error) {
        if (shouldSend == false)
            return;
        Message message = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putInt("ERROR", error);
        message.setData(bundle);
        message.arg1 = Constants.MSG_SERVER_ERROR;
        try {
            messageHandler.send(message);
        } catch (RemoteException e) {
            shouldSend = false;
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId){
        Global.loggerctx = getApplicationContext();
        try {
            editor = getSharedPreferences(
                    Constants.SHARED_DATA, MODE_PRIVATE).edit();
        }
        catch(Exception ex){
            Log.e(TAG, "Failed to get app context");
        }

        Constants.SERVICE_RUNNING = true;
        try {
            Bundle extras = intent.getExtras();
            messageHandler = (Messenger) extras.get("MESSENGER");
        }catch (Exception ex){
            Log.e(TAG, "Could not get main application");
            shouldSend = false;
        }

        db = LocalDBSingleton.getDB();
        super.onStartCommand(intent, flags, startId);

        Timer timer = new Timer();
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, (Constants.TIME_STEP-1)*1000, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.e(TAG, "fail to request location update, ignore " + ex.getMessage());
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

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(new BroadcastReceiver(){
            public void onReceive(Context c, Intent i){
                Log.i(TAG, "Got wifi results!");
                final WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                results = wifi.getScanResults();
                if (Build.VERSION.SDK_INT >= 18 && wifi.isScanAlwaysAvailable())
                    return;
                sendBroadcast(new Intent(Constants.WIFI_CHANGED_ACTION).putExtra("DISCONNECT",true));
            }
        }, intentFilter);

        IntentFilter wifiChangedFilter = new IntentFilter(Constants.WIFI_CHANGED_ACTION);
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context c, Intent i) {
                Log.i(TAG, "YOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO");
                final WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                if (i.getExtras().getBoolean("DISCONNECT", false)) {
                    wifi.setWifiEnabled(wifiStatus);
                }
                else {
                    Log.i(TAG, "SETTING STATUS: " + wifi.isWifiEnabled());
                    wifiStatus = wifi.isWifiEnabled();
                    if (!wifi.isWifiEnabled() &&
                            !(Build.VERSION.SDK_INT >= 18 && wifi.isScanAlwaysAvailable())) {
                        Log.i(TAG, "Wifi disabled, turning on...");
                        wifi.setWifiEnabled(true);
                    }
                    wifi.startScan();
                }
            }
        }, wifiChangedFilter);

        final WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        timer.schedule(new TimerTask(){
            ResponseHelper responseHelper;
            @Override
            public void run() {
                Intent getWifiStatus = new Intent(Constants.WIFI_CHANGED_ACTION);
                getWifiStatus.putExtra("DISCONNECT", false);
                sendBroadcast(getWifiStatus);

                int tick = db.getNumberOfData();
                sendTick(tick);
                long timestamp = System.currentTimeMillis();
                StringBuilder bssids = new StringBuilder();
                try {
                    try {
                        editor.putInt("tick", tick);
                    }
                    catch(Exception ex){
                        Log.e(TAG, "Failed to use editor");
                    }

                    if (results == null)
                        results = new ArrayList<ScanResult>();
                    Log.i(TAG, results.toString());
                    for (int i = 0; i < results.size(); ++i) {
                        bssids.append(results.get(i).BSSID + ":" + results.get(i).level);
                        if (i < results.size()-1)
                            bssids.append(",");
                    }
                }
                catch(Exception ex){
                    Log.e(TAG, "Could not gather wifi data");
                }
                db.addLocation(lat, lon, timestamp);
                db.addWifi(bssids.toString(), timestamp);

                Log.v(TAG, db.getLocation());
                Log.v(TAG, db.getWifi());

                if (tick > Constants.THRESHOLD && hasWifi()) {
                    Log.v(TAG, "Trying to send data");
                    User usr = db.getUser();
                    Log.v(TAG, usr.getUsername() + ' ' + usr.getPassword());
                    //TODO: Async
                    try{
                        responseHelper = Protocol.POSTJson(Constants.URL_ADD_LOCATION, db.getLocation());
                        if (responseHelper.getResultCode() == 403) {
                            sendError(403);
                            throw new ForbiddenException("Forbidden");
                        }
                        if (responseHelper.getResultCode() != 200) {
                            sendError(responseHelper.getResultCode());
                            throw new Exception("Error in connection when tried to send location "
                                    + responseHelper.getResultCode());
                        }

                        responseHelper = Protocol.POSTJson(Constants.URL_ADD_WIFI, db.getWifi());
                        if (responseHelper.getResultCode() == 403) {
                            sendError(responseHelper.getResultCode());
                            throw new ForbiddenException("Forbidden");
                        }
                        if (responseHelper.getResultCode() != 200) {
                            sendError(responseHelper.getResultCode());
                            throw new Exception("Error in connection when tried to send Wifi "
                                    + responseHelper.getResultCode());
                        }

                        Log.v(TAG, "All data sent, deleting local DB");
                        sendError(200);
                        try {
                            editor.putLong("lastSync", timestamp);
                        }
                        catch(Exception ex){
                            Log.e(TAG, "Failed to get editor");
                        }
                        sendLastSync(timestamp);
                        db.deleteAll();
                    }
                    catch (ForbiddenException e) {
                        try {
                            Log.v(TAG, "Got a forbidden, trying to log in");
                            responseHelper = Protocol.createOrlogin(usr);
                            if (responseHelper.getResultCode() != 200) {
                                sendError(responseHelper.getResultCode());
                                throw new Exception("Error " + responseHelper.getResultCode());
                            }
                        } catch (Exception ex) {
                            Log.e(TAG, ex.getMessage());
                        }
                    }
                    catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
                try {
                    editor.commit();
                    editor.clear();
                }
                catch(Exception ex){
                    Log.e(TAG, "Failed to get editor");
                }
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

    private boolean hasWifi(){
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mWifi.isConnected()) {
            //TODO: Test if can access the server
            return true;
        }
        return false;
    }


}
