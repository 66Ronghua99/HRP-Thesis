package com.ronghua.bledetect.bleadv;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.ParcelUuid;

import com.ronghua.bledetect.authentication.Authentication;
import com.ronghua.bledetect.exceptions.InitializationException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Broadcast {

    private static Broadcast instance;
    private Authentication auth;
    private BlueToothUtils bleUtils;

    private Set<String> impersonationSet = new HashSet<>();

    private ScanCallback impersonationCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            List<ScanResult> results = new ArrayList<>();
            results.add(result);
            onBatchScanResults(results);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {

        }
    };


    public static Broadcast getInstance() {
        if (instance == null)
            instance = new Broadcast();
        return instance;
    }

    private Broadcast(){
        auth = Authentication.getInstance();
        try {
            bleUtils = BlueToothUtils.getInstance();
        } catch (InitializationException e) {
            e.printStackTrace();
        }
    }

    public void startBroadcast(){

    }

    private void scanForImpersonation(){
        bleUtils.startScan();
    }


}
