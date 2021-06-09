package com.ronghua.bledetect.bleadv;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.os.SystemClock;

import androidx.annotation.Nullable;

import com.ronghua.bledetect.exceptions.InitializationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 蓝牙工具类
 */
public class BlueToothUtils {
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    //蓝牙是否可用
    private boolean bleEnable = false;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private static BlueToothUtils instance;
    private Handler mainHandler;
    public final static String BLUETOOTH_UPDATED = "com.ronghua.wifidirect.BLUETOOTH_UPDATED";
    private Runnable updateTask;
    private List<AdvertiseCallback> mAdvertiseCallbackList = new ArrayList<>();
    private List<ScanResult> result_list = new ArrayList<>();

    public List<BlueToothDetail> getResult_list() {
        List<BlueToothDetail> results = new ArrayList<>();
        for (ScanResult result: result_list){
            int rssi = result.getRssi();
            byte[] data = Objects.requireNonNull(result.getScanRecord()).getManufacturerSpecificData(0x01AC);
            data = data==null? "other".getBytes():data;
            String mac = result.getDevice().getAddress();
            long time = System.currentTimeMillis() -
                    SystemClock.elapsedRealtime() +
                    result.getTimestampNanos() / 1000000;
            results.add(new BlueToothDetail(rssi, mac, data, time));
        }
        return results;
    }


    private BlueToothUtils(final Context context) {
        bleEnable = checkBlueToothEnable();
        mainHandler = new Handler(Looper.getMainLooper());
        mBluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        updateTask = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setAction(BLUETOOTH_UPDATED);
                context.sendBroadcast(intent);
            }
        };
    }


    public static BlueToothUtils getInstance(Context context) {
        if (instance == null){
            instance = new BlueToothUtils(context);
        }
        return instance;
    }

    public static BlueToothUtils getInstance() throws InitializationException {
        if (instance == null){
            throw new InitializationException("BluetoothUtils need to be initialized first", new Throwable());
        }
        return instance;
    }

    /**
     * 检测设备是否支持蓝牙
     */
    public boolean checkBlueToothEnable() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            return false;
        } else {
            return true;
        }
    }


    /**
     * 打开蓝牙
     */
    public void onBlueTooth() {
        if (bleEnable) {
            if (bluetoothAdapter.isEnabled()) {
                //lalala
            } else {
                bluetoothAdapter.enable();
            }
        }
    }

    /**
     * 关闭蓝牙
     */
    public void offBlueTooth() {
        if (bleEnable) {
            if (bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.disable();
            } else {
                //already closed
            }

        }
    }

    /**
     * 获取已经配对的设备
     */
    public Set<BluetoothDevice> getConnectedDevices() {
        if (bleEnable) {
            if (bluetoothAdapter.isEnabled()) {
                return bluetoothAdapter.getBondedDevices();
            }
        }
        return null;
    }


    /**
     * 扫描蓝牙，会走广播
     */
    public void startDiscovery() {
        if (bleEnable) {
            if (!bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.startDiscovery();
            }
        }
    }

    /**
     * 停止扫描
     */
    public void stopDiscovery() {
        if (bleEnable) {
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
        }
    }

    /**
     * 扫描蓝牙
     */

    public void startScan() {
        startScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                //信号强度，是负的，数值越大代表信号强度越大
                List<ScanResult> results = new ArrayList<>();
                results.add(result);
                onBatchScanResults(results);
                super.onScanResult(callbackType, result);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                result_list.clear();
                result_list.addAll(results);
                mainHandler.post(updateTask);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        });
    }

    public void startScan(ScanCallback callback) {
        if (bleEnable) {
            bluetoothAdapter.getBluetoothLeScanner().startScan(null, createScanSettings(),callback);
        }
    }

    /**
     * 停止扫描
     */
    public void stopScan() {
        if (bleEnable) {
            bluetoothAdapter.getBluetoothLeScanner().stopScan(new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    super.onBatchScanResults(results);
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                }
            });
        }
    }

    public ScanSettings createScanSettings(){
        ScanSettings.Builder ssBuilder = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
        return ssBuilder.build();
    }

    public void startAdvertising(){
        mAdvertiseCallbackList.add(mAdvertiseCallback);
        byte[] broadcastData =("Hi, receiver" + String.valueOf(System.currentTimeMillis())).getBytes();
        startAdvertising(mAdvertiseCallback);
    }

    public void startAdvertising(AdvertiseCallback callback){

        mAdvertiseCallbackList.add(callback);
        byte[] broadcastData ="Hi, receiver".getBytes();
        startAdvertising(new HashMap<ParcelUuid, byte[]>(), callback);
    }
    // 16 advertisements most

    public void startAdvertising(Map<ParcelUuid, byte[]> data, AdvertiseCallback callback){
        mBluetoothLeAdvertiser.startAdvertising(createAdvSettings(false, 0), createAdvertiseData(data), callback);
    }

    public void stopAdvertising() {
        for(AdvertiseCallback callback: mAdvertiseCallbackList){
            stopAdvertising(callback);
        }
    }

    public void stopAdvertising(AdvertiseCallback callback){
        mBluetoothLeAdvertiser.stopAdvertising(callback);
    }

    public AdvertiseSettings createAdvSettings(boolean connectable, int timeoutMillis) {
        AdvertiseSettings.Builder mSettingsbuilder = new AdvertiseSettings.Builder();
        mSettingsbuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY);
        mSettingsbuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH);
        mSettingsbuilder.setConnectable(connectable);
        mSettingsbuilder.setTimeout(timeoutMillis);
        return mSettingsbuilder.build();
    }

    public AdvertiseData createAdvertiseData(Map<ParcelUuid, byte[]> data) {
        AdvertiseData.Builder mDataBuilder = new AdvertiseData.Builder();
        mDataBuilder.addManufacturerData(0x01AC, "Hi, receiverrrrrrrrrrrrrrrrrrrrrrrr".getBytes());
        for(ParcelUuid uuid: data.keySet()){
            byte[] d = data.get(uuid);
            mDataBuilder.addServiceData(uuid, d);
        }
        return mDataBuilder.build();
    }

    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
        }

        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
        }
    };

    public class BlueToothDetail{
        private int rssi;
        private String mac;
        private byte[] manufacture = new byte[]{0x01};
        private long recvTime;

        public BlueToothDetail(int name, String mac, byte[] manufacture, long recvTime) {
            this.rssi = name;
            this.mac = mac;
            this.manufacture = manufacture;
            this.recvTime = recvTime;
        }

        public long getRecvTime() {
            return recvTime;
        }

        public void setRecvTime(long recvTime) {
            this.recvTime = recvTime;
        }

        public int getRssi() {
            return rssi;
        }

        public void setRssi(int rssi) {
            this.rssi = rssi;
        }

        public String getMac() {
            return mac;
        }

        public void setMac(String mac) {
            this.mac = mac;
        }

        public byte[] getManufacture() {
            return manufacture;
        }

        public void setManufacture(byte[] manufacture) {
            this.manufacture = manufacture;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            return this.getMac().equals(((BlueToothDetail)obj).getMac());
        }
    }
}
