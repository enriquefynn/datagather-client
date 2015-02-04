package research.ufu.datagather.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


import research.ufu.datagather.R;
import research.ufu.datagather.db.LocalDB;
import research.ufu.datagather.db.LocalDBSingleton;
import research.ufu.datagather.utils.Global;

public class MainActivity extends Activity {
    LocalDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Global.appctx = getApplicationContext();
        db = LocalDBSingleton.getDB();
        //db.reset();
        db.getUser();
        startService(new Intent(getBaseContext(), Logger.class));
        setContentView(R.layout.main);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /*private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.                                      
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.                                                         
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.                                                          
            if (mMap != null) {
                setUpMap();
            }
        }
    }


    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
    }
    */
}                                                                                                                         
                                                                                                                          