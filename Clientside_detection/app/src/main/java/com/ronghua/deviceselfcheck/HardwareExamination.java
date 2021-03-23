package com.ronghua.deviceselfcheck;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class HardwareExamination {
    public Handler mainHandler;
    private WiFiDetection wiFiDetection;
    private static HardwareExamination instance;
    private HandlerThread handlerThread = new HandlerThread("bg_thread2");
    private Handler bgHandler;

    public WiFiDetection getWiFiDetection() {
        return wiFiDetection;
    }

    public static HardwareExamination getInstance(Context context) {
        if(instance == null)
            instance = new HardwareExamination(context);
        return instance;
    }

    private HardwareExamination(Context context){
        wiFiDetection = new WiFiDetection(context);
        mainHandler = new Handler(Looper.getMainLooper());
        handlerThread.start();
        bgHandler = new Handler(handlerThread.getLooper());
    }

    public void scanWifi(){
        wiFiDetection.enableWiFi(true);
        wiFiDetection.wifiScanList();
    }

    public class WiFiDetection {
        private WifiManager mWifiManager;
        private List<String> mWifiList = new ArrayList<>();
        private Context mContext;

        public WifiManager getmWifiManager() {
            return mWifiManager;
        }

        public List<String> getmWifiList() {
            Set<String> set = new HashSet<>(mWifiList);
            return new ArrayList<>(set);
        }

        private WiFiDetection(Context context){
            mContext = context;
            mWifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        }
        public boolean isWifiEnabled(){
            return false;
        }

        public void enableWiFi(boolean enable){
            mWifiManager.setWifiEnabled(enable);
        }

        public void wifiScanList(){
            mWifiList.clear();
            mWifiManager.startScan();
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    List<ScanResult> results = mWifiManager.getScanResults();
                    for (ScanResult result: results){
                        mWifiList.add("WiFi name: " + result.SSID);
                    }
                    Intent intent = new Intent(mContext, DetectResultActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("com.ronghua.deviceselfcheck", "wifi");
                    mContext.startActivity(intent);
                }
            }, 1000);
        }


    }
}

