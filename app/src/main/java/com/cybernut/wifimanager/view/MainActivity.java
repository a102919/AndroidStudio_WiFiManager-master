package com.cybernut.wifimanager.view;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.cybernut.wifimanager.common.RetainedFragmentManager;
import com.cybernut.wifimanager.controller.MainActivityOps;
import com.cybernut.wifimanager.R;

public class MainActivity extends AppCompatActivity {

    /**
     * Debugging tag used by the Android logger.
     */
    private static final String TAG =
            MainActivity.class.getSimpleName();

    /**
     * Used to retain the MainActivityOps state between runtime configuration
     * changes.
     */
    protected final RetainedFragmentManager mRetainedFragmentManager =
            new RetainedFragmentManager(this.getFragmentManager(),
                    TAG);
    private static final String MAIN_OPS_STATE = "MAIN_OPS_STATE";

    private MainActivityOps mMainActivityOps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleConfigurationChanges();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_96) {
            mMainActivityOps.setIp("http://192.168.10.96/zm/index.php");
            return true;
        }if (id == R.id.action_169) {
            mMainActivityOps.setIp("http://192.168.10.169/zm/index.php");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mMainActivityOps.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Handle hardware reconfigurations, such as rotating the display.
     */
    protected void handleConfigurationChanges() {
        // If this method returns true then this is the first time the
        // Activity has been created.
        if (mRetainedFragmentManager.firstTimeIn()) {
            Log.d(TAG,
                    "First time onCreate() call");

            // Create the MainActivityOps object one time.
            mMainActivityOps = new MainActivityOps(this);

            // Store the LoginOps into the RetainedFragmentManager.
            mRetainedFragmentManager.put(MAIN_OPS_STATE,
                    mMainActivityOps);

        } else {
            // The RetainedFragmentManager was previously initialized,
            // which means that a runtime configuration change
            // occurred.

            Log.d(TAG,
                    "Second or subsequent onCreate() call");

            // Obtain the LoginOps object from the
            // RetainedFragmentManager.
            mMainActivityOps =
                    mRetainedFragmentManager.get(MAIN_OPS_STATE);

            // This check shouldn't be necessary under normal
            // circumstances, but it's better to lose state than to
            // crash!
            if (mMainActivityOps == null) {
                // Create the MainActivityOps object one time.
                mMainActivityOps = new MainActivityOps(this);

                // Store the LoginOps into the RetainedFragmentManager.
                mRetainedFragmentManager.put(MAIN_OPS_STATE,
                        mMainActivityOps);

            } else
                // Inform it that the runtime configuration change has
                // completed.
                mMainActivityOps.onConfigurationChange(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMainActivityOps.unregisterReceiverAndDestroy();
    }
    public void open(View view){
        openGPSSettings();
    }
    private void openGPSSettings() {
        Intent intent = new Intent("android.location.GPS_ENABLED_CHANGE");
        intent.putExtra("enabled", false);
        this.sendBroadcast(intent);
        boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled( getContentResolver(), LocationManager.GPS_PROVIDER );
        Log.e("gps", Boolean.toString(gpsEnabled));

        String provider = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if(provider.contains("gps")) { //if gps is enabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            this.sendBroadcast(poke);
        }
    }
    public static final void openGPS(Context context) {
        Intent GPSIntent = new Intent();
        GPSIntent.setClassName("com.android.settings",
                "com.android.settings.widget.SettingsAppWidgetProvider");
        GPSIntent.addCategory("android.intent.category.ALTERNATIVE");
        GPSIntent.setData(Uri.parse("custom:3"));
        try {
            PendingIntent.getBroadcast(context, 0, GPSIntent, 0).send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }
}
