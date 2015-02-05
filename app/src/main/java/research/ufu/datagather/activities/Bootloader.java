package research.ufu.datagather.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Bootloader extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startServiceIntent = new Intent(context, Logger.class);
        context.startService(startServiceIntent);
    }
}
