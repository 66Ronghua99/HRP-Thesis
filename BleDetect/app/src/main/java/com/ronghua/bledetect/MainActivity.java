package com.ronghua.bledetect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.view.View;
import android.widget.Toast;

import com.ronghua.bledetect.authentication.Authentication;
import com.ronghua.bledetect.authentication.CsrHelper;
import com.ronghua.bledetect.bleadv.BlueToothUtils;
import com.ronghua.bledetect.network.NetworkRequest;
import com.ronghua.bledetect.wifi.HotSpotInfo;
import com.ronghua.bledetect.wifi.WiFiInfo;
import com.ronghua.bledetect.wifi.WiFiResultActivity;

import org.spongycastle.operator.OperatorCreationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public final static String TAG = "WiFiDirect";

    WifiP2pManager manager;
    Channel channel;
    BroadcastReceiver receiver;
    IntentFilter intentFilter;
    WiFiInfo mWifiInfo;
    HotSpotInfo mHotSpotInfo;
    BlueToothUtils mBluetooth;
    CsrHelper csrHelper;
    Authentication authentication;
    NetworkRequest networkRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();
//        initP2p();
//        initWlan();
        initNetwork();
        initBluetooth();
    }

    private void initNetwork() {
        authentication = Authentication.getInstance();
        try {
            csrHelper = CsrHelper.getInstance()
                    .generateKeyPair()
                    .generateCSR("Sweden");
        } catch (IOException | OperatorCreationException e) {
            e.printStackTrace();
        }
        networkRequest = NetworkRequest.getInstance();
        findViewById(R.id.network).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    networkRequest.requestCertificate(NetworkRequest.CA_URL);
                }
            }
        });
    }

    private void initBluetooth() {
        mBluetooth = BlueToothUtils.getInstance(getApplicationContext());
        findViewById(R.id.bstart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetooth.startAdvertising();
            }
        });
        findViewById(R.id.bstart3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetooth.startAdvertising(new AdvertiseCallback() {
                    @Override
                    public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                        super.onStartSuccess(settingsInEffect);
                    }

                    @Override
                    public void onStartFailure(int errorCode) {
                        super.onStartFailure(errorCode);
                    }
                });
            }
        });
        findViewById(R.id.bstart2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetooth.stopAdvertising();
            }
        });
        findViewById(R.id.sstart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BlueToothActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
            }
        });
        findViewById(R.id.replay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetooth.stopAdvertising();
                mBluetooth.startScan(new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        List<ScanResult> results = new ArrayList<>();
                        results.add(result);
                        onBatchScanResults(results);
                    }


                    @Override
                    public void onBatchScanResults(List<ScanResult> results) {
                        for (ScanResult scanResult:results){
                            ScanRecord sr = scanResult.getScanRecord();
                            byte[] data = sr.getManufacturerSpecificData(0x01AC);
                            if(data == null)
                                continue;
                            String manu = new String(data);
                            if (manu.startsWith("Hi")){
                                manu = manu.substring(5);
//                                mBluetooth.startAdvertising(manu.getBytes());
                            }
                        }
                    }

                    @Override
                    public void onScanFailed(int errorCode) {
                        super.onScanFailed(errorCode);
                    }
                });
            }
        });
    }

    private void requestPermissions(){
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
        || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CHANGE_WIFI_STATE) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CHANGE_WIFI_STATE}, 0);
        }
    }

    private void initWlan() {
        mWifiInfo = WiFiInfo.getInstance(this);
        findViewById(R.id.wifi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWifiInfo.enableWiFi();
                mWifiInfo.wifiScanList();
                Intent intent = new Intent(getApplicationContext(), WiFiResultActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
            }
        });
    }

    private void initP2p() {
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


    }

    public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
        private WifiP2pManager manager;
        private Channel channel;
        private MainActivity activity;

        public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
                                           MainActivity activity) {
            super();
            this.manager = manager;
            this.channel = channel;
            this.activity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                return;
            Log.i(TAG, "intent msg: "+action);
            if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                WifiP2pDevice device = (WifiP2pDevice) intent
                        .getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
                String myMac = device.deviceAddress;
                Log.i(TAG, "Device WiFi P2p MAC Address: " + myMac);
            }
            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    Log.i(TAG, "enabled");
                } else {
                    Log.i(TAG, "not enabled");
                }
            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                manager.requestPeers(channel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList peers) {
                        Iterator<WifiP2pDevice> iterator = peers.getDeviceList().iterator();
                        while (iterator.hasNext()){
                            WifiP2pDevice device = iterator.next();
                            Log.i(TAG, device.toString());
                            Toast.makeText(getApplicationContext(), device.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                // Respond to new connection or disconnections
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // Respond to this device's wifi state changing
            }
        }
    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
//        registerReceiver(receiver, intentFilter);
//        Log.i(TAG, "register receiver");
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
//        unregisterReceiver(receiver);
//        Log.i(TAG, "unregister receiver");
    }



}