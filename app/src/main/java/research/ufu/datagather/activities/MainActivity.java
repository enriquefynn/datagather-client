package research.ufu.datagather.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import research.ufu.datagather.R;
import research.ufu.datagather.model.Constants;
import research.ufu.datagather.utils.Global;

public class MainActivity extends Activity {
    static TextView textTick;
    static TextView textLastSync;
    static TextView textServerError;
    private ServiceConnection connection;
    Messenger mService = null;
    boolean mBound;

    class MessageHandler extends Handler {

        @Override
        public void handleMessage(Message message) {
            if (message.arg1 == Constants.MSG_TICK) {
                textTick.setText(String.valueOf(message.getData().getLong("TICK")));
            }
            else if (message.arg1 == Constants.MSG_LAST_SYNC) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                String date = sdf.format(new Date(message.getData().getLong("LASTSYNC")));
                textLastSync.setText(date);
            }
            else if (message.arg1 == Constants.MSG_SERVER_ERROR) {
                int error = message.getData().getInt("ERROR");
                if (error != 200)
                    textServerError.setText("Error: " + error);
                else
                    textServerError.setText("Responding OK");
            }
            else{
                super.handleMessage(message);
            }
        }
    }

    public Handler messageHandler = new MessageHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    protected void onStart(){
        super.onStart();
        Global.appctx = getApplicationContext();
        Log.v("Main", "Binding");
        textTick = (TextView) findViewById(R.id.tick);
        textLastSync = (TextView) findViewById(R.id.lastsync);
        textServerError = (TextView) findViewById(R.id.servererror);

        long lastSync = getSharedPreferences(Constants.SHARED_DATA, MODE_PRIVATE).
                getLong("lastSync", 0L);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String date = sdf.format(new Date(lastSync));
        if (lastSync != 0L)
            textLastSync.setText(date);

        Intent startS = new Intent(Global.appctx, Logger.class);
        startS.putExtra("MESSENGER", new Messenger(messageHandler));
        if (Constants.SERVICE_RUNNING == false) {
            Log.v("Main", "Starting Service");
            startService(startS);
        }
        connection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                mService = new Messenger(service);
                mBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                mService = null;
                mBound = false;
            }
        };
        bindService(startS, connection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(connection);
            mBound = false;
        }
    }

    public void goToUrl (View view) {
        Uri uriUrl = Uri.parse("https://sites.google.com/site/distributedsystemsandnetworks/datagather_");
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }
}                                                                                                                         
                                                                                                                          