package com.ronghua.bledetect.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.widget.Toast;

@Deprecated
public class WiFiInfo {

    public Handler mainHandler;
    private static WiFiInfo instance;
    private WifiManager mWifiManager;
    private List<WifiResultDetail> mWifiList = new ArrayList<>();
    private Runnable successToast;
    private Runnable failureToast;
    public final static String LIST_UPDATED = "com.ronghua.wifidirect.LIST_UPDATED";

    public static WiFiInfo getInstance(Context context) {
        if(instance == null)
            instance = new WiFiInfo(context);
        return instance;
    }

    private WiFiInfo(final Context context){
        mainHandler = new Handler(Looper.getMainLooper());
        mWifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    scanSuccess();
                } else {
                    scanFailure();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiScanReceiver, intentFilter);
        successToast = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, "WiFi scan succeeded", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setAction(LIST_UPDATED);
                context.sendBroadcast(intent);
            }
        };
        failureToast = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, "WiFi scan failed", Toast.LENGTH_SHORT).show();
            }
        };
    }

    public List<WifiResultDetail> getmWifiList() {
        return mWifiList;
    }

    public boolean isWifiEnabled(){
        return mWifiManager.isWifiEnabled();
    }

    public void enableWiFi(){
        if(!mWifiManager.isWifiEnabled())
            mWifiManager.setWifiEnabled(true);
    }

    public void wifiScanList(){
        boolean success = mWifiManager.startScan();
        if (!success) {
            scanFailure();
        }
    }

    private void scanSuccess() {
        List<ScanResult> list = mWifiManager.getScanResults();
        mWifiList.clear();
        Iterator<ScanResult> iterator = list.iterator();
        while (iterator.hasNext()){
            ScanResult result = iterator.next();
            WifiResultDetail wrd = new WifiResultDetail();
            wrd.setName(result.SSID);
            wrd.setMacAddr(result.BSSID);
            wrd.setRssi(result.level);
            mWifiList.add(wrd);
        }
        mainHandler.post(successToast);
    }

    private void scanFailure() {
        List<ScanResult> results = mWifiManager.getScanResults();
        mainHandler.post(failureToast);
    }

    public boolean enableAp(){
        WifiConfiguration config = new WifiConfiguration();
        return false;
    }

    class WifiResultDetail {
        private String name;
        private String macAddr;
        private int rssi;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMacAddr() {
            return macAddr;
        }

        public void setMacAddr(String macAddr) {
            this.macAddr = macAddr;
        }

        public int getRssi() {
            return rssi;
        }

        public void setRssi(int rssi) {
            this.rssi = rssi;
        }

    }

}
