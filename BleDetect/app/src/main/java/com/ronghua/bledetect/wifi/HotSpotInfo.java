package com.ronghua.bledetect.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ResultReceiver;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Deprecated
public class HotSpotInfo {
        private Context mContext;
        private HandlerThread ht = new HandlerThread("bgThread");
        private Handler bgHandler = new Handler(ht.getLooper());

        private HotSpotInfo(Context context){
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

            } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
}
