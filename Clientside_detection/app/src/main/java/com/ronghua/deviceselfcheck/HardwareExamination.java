package com.ronghua.root_emu;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.ResultReceiver;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HardwareExamination {
    public Handler mainHandler;
    private WiFiDetection wiFiDetection;
    private HotSpot hotSpot;
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
        hotSpot = new HotSpot(context);
        mainHandler = new Handler(Looper.getMainLooper());
        handlerThread.start();
        bgHandler = new Handler(handlerThread.getLooper());
    }

    public void scanWifi(){
        wiFiDetection.enableWiFi();
        wiFiDetection.wifiScanList();
    }

    public boolean enableAp(){
        return hotSpot.setHotSpot(true);
    }

    public boolean disableAp(){
        return hotSpot.setHotSpot(false);
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

        public void enableWiFi(){
            if(!mWifiManager.isWifiEnabled())
                mWifiManager.setWifiEnabled(true);
        }

        public void wifiScanList(){
            mWifiList.clear();
            mWifiManager.startScan();
            bgHandler.postDelayed(new Runnable() {
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

    @Deprecated
    public class HotSpot{
        private Context mContext;

        private HotSpot(Context context){
            mContext = context;
        }

        public boolean setHotSpot(final boolean enabled) {
            WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            final WifiConfiguration apConfig = new WifiConfiguration();
            apConfig.SSID = "MasterThesisClients";
            apConfig.preSharedKey = "88888888";
            apConfig.allowedKeyManagement.set(4);

            if(Build.VERSION.SDK_INT >= 26){
                bgHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        setWifiApEnabledForAndroidO(mContext, enabled, apConfig);
                    }
                });
                return true;
            }

            if (enabled&&!wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false);
            }
            try {
                Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
                return (Boolean) method.invoke(wifiManager, apConfig, enabled);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        public void setWifiApEnabledForAndroidO(Context context, boolean isEnable, WifiConfiguration apConfig){
            ConnectivityManager connManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            Field iConnMgrField = null;
            try {
                iConnMgrField = connManager.getClass().getDeclaredField("mService");
                iConnMgrField.setAccessible(true);
                Object iConnMgr = iConnMgrField.get(connManager);
                Class<?> iConnMgrClass = Class.forName(iConnMgr.getClass().getName());

                Method mMethod = wifiManager.getClass().getMethod("setWifiApConfiguration", WifiConfiguration.class);
                mMethod.invoke(wifiManager, apConfig);

                if(isEnable){
                    Method startTethering = iConnMgrClass.getMethod("startTethering", int.class, ResultReceiver.class, boolean.class);
                    startTethering.invoke(iConnMgr, 0, null, true);
                }else{
                    Method startTethering = iConnMgrClass.getMethod("stopTethering", int.class);
                    startTethering.invoke(iConnMgr, 0);
                }

            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}

