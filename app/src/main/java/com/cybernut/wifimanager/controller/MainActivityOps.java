package com.cybernut.wifimanager.controller;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.cybernut.wifimanager.R;
import com.cybernut.wifimanager.model.WiFiList;
import com.cybernut.wifimanager.permission.PermissionUtils;
import com.cybernut.wifimanager.view.MainActivity;
import com.cybernut.wifimanager.view.playActivity;
import com.wevey.selector.dialog.MDEditDialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

public class MainActivityOps {
    /**
     * Debugging tag used by the Android logger.
     */
    private static final String TAG =
            MainActivityOps.class.getSimpleName();
    private MDEditDialog dialog6;
    private WifiManager wifiManagerr;
    private int size = 0;
    private List<ScanResult> results;
    private ArrayList<HashMap<String, String>> mArrayList = new ArrayList<>();
    private SimpleAdapter mAdapter;
    private BroadcastReceiver mBroadcastReceiver;
    private Switch openwifi;

    /**
     * Used to enable garbage collection.
     */
    private WeakReference<MainActivity> mMainActivity;
    private WeakReference<FloatingActionButton> mFab;
    private WeakReference<TextView> mTextView;

    /**
     * Id to identity ACCESS_COARSE_LOCATION permission request.
     */
    private static final int REQUEST_ACCESS_LOCATION = 101;
    String ip="http://192.168.10.169/zm/index.php";
private Context mycontext;
    public void setIp(String ip){
        this.ip=ip;
    }
    public MainActivityOps(MainActivity mainActivity) {
        // Initialize the WeakReference.
        mMainActivity = new WeakReference<>(mainActivity);
        mycontext=mainActivity.getApplicationContext();
        // Finish the initialization steps.
        initializeViewFields();
        initializeNonViewFields();
    }

    private void initializeViewFields() {
        Log.d(TAG, "initializeViewFields");
        // Get references to the UI components.
        mMainActivity.get().setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) mMainActivity.get().findViewById(R.id.toolbar);
        mMainActivity.get().setSupportActionBar(toolbar);
//        mTextView = new WeakReference<>
//                ((TextView) mMainActivity.get().findViewById(R.id.textView2));
        WeakReference<ListView> mListView = new WeakReference<>
                ((ListView) mMainActivity.get().findViewById(R.id.listView1));

        populateAutoComplete  ();

        wifiManagerr = (WifiManager) mMainActivity.get().getSystemService(Context.WIFI_SERVICE);
        mAdapter = new SimpleAdapter(mMainActivity.get(), mArrayList, R.layout.list_wifi,
                new String[] {"ssid", "power", "freq"}, new int[] {R.id.ssid, R.id.power, R.id.freq});
        mListView.get().setAdapter(mAdapter);
        mListView.get().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                WifiInfo mWifiInfo = wifiManagerr.getConnectionInfo();
                Toast.makeText(mMainActivity.get(), "wifi連線"+mWifiInfo.getIpAddress(), Toast.LENGTH_SHORT).show();

                if(mWifiInfo.getIpAddress()==973777088){
                    Intent intt=new Intent(mMainActivity.get(),playActivity.class);
                    intt.putExtra("ip",ip);
                    mMainActivity.get().startActivity(intt);
                }
                int NETWORKID;
                if((NETWORKID = IsEX(mArrayList.get(position).get("ssid")))!=0){
                    wifiManagerr.enableNetwork(NETWORKID,true);
                }else {
                dialog6 = new MDEditDialog.Builder(mMainActivity.get())
                        .setTitleVisible(true)
                        .setTitleText("輸入WIFI密碼")
                        .setTitleTextSize(20)
                        .setTitleTextColor(R.color.black_light)
                        .setContentTextSize(18)
                        .setMaxLength(18)
                        .setHintText("8位字符")
                        .setMaxLines(1)
                        .setContentTextColor(R.color.colorPrimary)
                        .setButtonTextSize(14)
                        .setLeftButtonTextColor(R.color.colorPrimary)
                        .setLeftButtonText("取消")
                        .setRightButtonTextColor(R.color.colorPrimary)
                        .setRightButtonText("确定")
                        .setLineColor(R.color.colorPrimary)
                        .setOnclickListener(new MDEditDialog.OnClickEditDialogListener() {
                            @Override
                            public void clickLeftButton(View view, String text){
                                dialog6.dismiss();
                            }
                            @Override
                            public void clickRightButton(View view, String text){
                                wifiManagerr.disconnect();
                                int NETWORKID;
                                if((NETWORKID = IsEX(mArrayList.get(position).get("ssid")))!=0){
                                    wifiManagerr.enableNetwork(NETWORKID,true);
                                }else {
                                    // wifiManagerr.enableNetwork(mArrayList.get(position).get("ssid"),true);
                                    addNetwork(CreateWifiInfo(mArrayList.get(position).get("ssid"), text, 3));
                                }dialog6.dismiss();
                            }
                        })
                        .setMinHeight(0.3f)
                        .setWidth(0.8f)
                        .build();
                dialog6.show();
                Toast.makeText(mMainActivity.get(), "你選擇的是" + mArrayList.get(position).get("ssid"), Toast.LENGTH_SHORT).show();
            }
        }});
        mFab = new WeakReference<>
                ((FloatingActionButton) mMainActivity.get().findViewById(R.id.fab));
        mFab.get().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wifiManagerr.startScan();

                Snackbar.make(view, "掃描...", Snackbar.LENGTH_LONG)
                        .setAction("行動", null).show();
            }
        });
        //sw
        openwifi=(Switch)mMainActivity.get().findViewById(R.id.switch1);
        openwifi.setChecked(wifiManagerr.isWifiEnabled());
        openwifi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {
                    populateAutoComplete();
                    wifiManagerr.setWifiEnabled(true);
                    openwifi.setText("開");
                } else {
                    mArrayList.clear();
                    mAdapter.notifyDataSetChanged();
                    wifiManagerr.setWifiEnabled(false);
                    openwifi.setText("關");

                }
            }
        });
//        if(!wifiManagerr.isWifiEnabled())
//        {
//            AlertDialog.Builder dialog = new AlertDialog.Builder(mMainActivity.get());
//            dialog.setTitle("提醒");
//            dialog.setMessage("您的Wifi已禁用，啟用？");
//            dialog.setIcon(android.R.drawable.ic_dialog_info);
//            dialog.setCancelable(false);
//            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    // TODO Auto-generated method stub
//                    openGPS(mycontext);
//                    wifiManagerr.setWifiEnabled(true);
//                    Snackbar.make(mFab.get(),
//                            "WiFi被禁用...使其啟用", Snackbar.LENGTH_LONG)
//                            .setAction("Action", null).show();
//
//                }
//            });
//            dialog.show();
//        }

        mBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
                    WifiInfo mWifiInfo = wifiManagerr.getConnectionInfo();
                    Toast.makeText(mMainActivity.get(), "wifi連線"+mWifiInfo.getIpAddress(), Toast.LENGTH_SHORT).show();

                    if(mWifiInfo.getIpAddress()==973777088){
                        Intent intt=new Intent(mMainActivity.get(),playActivity.class);
                        intt.putExtra("ip",ip);
                        mMainActivity.get().startActivity(intt);
                    }
                }
                    // TODO Auto-generated method stub
                results = wifiManagerr.getScanResults();
                size = results.size();
                Log.d(TAG, "獲取wifi "+size);
                mArrayList.clear();

                for(int i=0; i<size; i++) {
                    //Log.d(TAG, results.get(i).SSID);
                    //Log.d(TAG, results.get(i).level+" dBm");
                    //Log.d(TAG, "freq "+results.get(i).frequency);

                    HashMap<String, String> item = new HashMap<>();
                    item.put("ssid", results.get(i).SSID);
                    item.put("power", results.get(i).level+" dBm");
                    item.put("id", results.get(i).level+" dBm");
                    String wifichn = WiFiList.WIFI_CHANNELS.containsKey(
                            Integer.toString(results.get(i).frequency))?
                            WiFiList.WIFI_CHANNELS.
                                    get(Integer.toString(results.get(i).frequency)):"5G";
                    item.put("freq", wifichn);
                    item.put("capabilities",results.get(i).capabilities);
                    mArrayList.add(item);
                }

                // Sort by power
                Collections.sort(mArrayList, new Comparator<HashMap<String, String>>() {

                    @Override
                    public int compare(HashMap<String, String> lhs,
                                       HashMap<String, String> rhs) {
                        // TODO Auto-generated method stub
                        return (lhs.get("power")).compareTo(rhs.get("power"));
                    }
                });

                if(size > 0) {
                    //mTextView.get().setText(mArrayList.get(0).get("ssid"));
                }
                mAdapter.notifyDataSetChanged();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mMainActivity.get().registerReceiver(mBroadcastReceiver,
                filter);

        if(size > 0) {
           // mTextView.get().setText(mArrayList.get(0).get("ssid"));
        }

    }

    /**
     * (Re)initialize the non-view fields (e.g.,
     * GenericServiceConnection objects).
     */
    private void initializeNonViewFields() {
        Log.d(TAG, "initializeNonViewFields");

    }

    private void populateAutoComplete() {
        if (!mayRequestLocation()) {
            return;
        }

    }

    private boolean mayRequestLocation() {
        Log.d(TAG, "mayRequestLocation");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        Log.d(TAG, "newer than M");
        if (mMainActivity.get().checkSelfPermission(ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        Log.d(TAG, "no permission");

        if (PermissionUtils.hasPermissions(mMainActivity.get(), Manifest.permission.CAMERA)) {
            mMainActivity.get().
                    requestPermissions(new String[]{
                                    ACCESS_COARSE_LOCATION},
                            REQUEST_ACCESS_LOCATION);
        } else {
            PermissionUtils.requestPermissions(mMainActivity.get(), "", REQUEST_ACCESS_LOCATION,new String[]{
                            ACCESS_COARSE_LOCATION});
        }

//        if (mMainActivity.get().
//                shouldShowRequestPermissionRationale(ACCESS_COARSE_LOCATION)) {
//            Log.d(TAG, "request permission");
//            Snackbar.make(mFab.get(),
//                    R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
//                    .setAction(android.R.string.ok, new View.OnClickListener() {
//                        @Override
//                        @TargetApi(Build.VERSION_CODES.M)
//                        public void onClick(View v) {
//                            mMainActivity.get().
//                                    requestPermissions(new String[]{
//                                                    ACCESS_COARSE_LOCATION},
//                                            REQUEST_ACCESS_LOCATION);
//                        }
//                    });
//        } else {
//            Log.d(TAG, "Permission OK");
//            mMainActivity.get().
//                    requestPermissions(new String[]{
//                                    ACCESS_COARSE_LOCATION},
//                            REQUEST_ACCESS_LOCATION);
//        }
        return false;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");
        if (requestCode == REQUEST_ACCESS_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    /**
     * Called after a runtime configuration change occurs to finish
     * the initialization steps.
     */
    public void onConfigurationChange(MainActivity mainActivity) {
        Log.d(TAG,
                "onConfigurationChange() called");
        // Reset the mActivity WeakReference.
        mMainActivity = new WeakReference<>(mainActivity);
        // (Re)initialize all the View fields.
        initializeViewFields();
    }

    public void unregisterReceiverAndDestroy() {
        Log.d(TAG, "go to unregisterReceiverAndDestroy");
        if(mBroadcastReceiver!=null) {
            mMainActivity.get().unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
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

    public WifiConfiguration CreateWifiInfo(String SSID, String Password, int Type)
    {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";

        WifiConfiguration tempConfig = this.IsExsits(SSID);
        if(tempConfig != null) {
            wifiManagerr.removeNetwork(tempConfig.networkId);
            Toast.makeText(mMainActivity.get(), "wifi連接0", Toast.LENGTH_SHORT).show();

        }

        if(Type == 1) //WIFICIPHER_NOPASS
        {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if(Type == 2) //WIFICIPHER_WEP
        {
            config.hiddenSSID = true;
            config.wepKeys[0]= "\""+Password+"\"";
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if(Type == 3) //WIFICIPHER_WPA
        {
            config.preSharedKey = "\""+Password+"\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
            Toast.makeText(mMainActivity.get(), "wifi連接3", Toast.LENGTH_SHORT).show();
//           // if(config.SSID.equals("Chellange Power")){
//                Intent intt=new Intent(mMainActivity.get(),playActivity.class);
//                mMainActivity.get().startActivity(intt);
//            //}

        }
        return config;
    }
    private WifiConfiguration IsExsits(String SSID)
    {
        List<WifiConfiguration> existingConfigs = wifiManagerr.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs)
        {
            if (existingConfig.SSID.equals("\""+SSID+"\""))
            {
                return existingConfig;
            }
        }
        return null;
    }
    private int IsEX(String SSID)
    {
        List<WifiConfiguration> existingConfigs = wifiManagerr.getConfiguredNetworks();
        for (WifiConfiguration existingConfig : existingConfigs)
        {
            if (existingConfig.SSID.equals("\""+SSID+"\""))
            {
                return existingConfig.networkId;
            }
        }
        return 0;
    }
    public void addNetwork(WifiConfiguration wcg) {
        int wcgID = wifiManagerr.addNetwork(wcg);
        boolean b =  wifiManagerr.enableNetwork(wcgID, true);
        Toast.makeText(mMainActivity.get(), "wifi連接結果"+b, Toast.LENGTH_SHORT).show();
        System.out.println("a--" + wcgID);
        System.out.println("b--" + b);
    }
}
