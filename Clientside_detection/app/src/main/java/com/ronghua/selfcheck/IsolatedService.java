package com.ronghua.selfcheck;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.ronghua.selfcheck.IIsolatedProcess;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class IsolatedService extends Service {
    private static String[] blackListMountPaths = {"/sbin/.magisk/",
            "/sbin/.core/mirror",
            "/sbin/.core/img",
            "/sbin/.core/db-0/magisk.db"};
    private static String TAG = "DetectMagisk";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private IBinder mBinder = new IIsolatedProcess.Stub() {
        @Override
        public boolean detectMagiskHide() throws RemoteException {
            try{
                Native.isLibLoaded();
            }catch(UnsatisfiedLinkError e){
                Log.i(TAG, "Native lib is not loaded successfully");
                return true;
            }
            boolean isMagiskDetect = false;
            int count = 0;
            try {
                FileReader fr = new FileReader("/proc/self/mounts");
                BufferedReader br = new BufferedReader(fr);
                String line = "";
                while((line = br.readLine()) != null){
                    for(String path:blackListMountPaths){
                        Log.i(TAG, "Checking mount path: " + path);
                        if(line.contains(path)){
                            count++;
                            Log.i(TAG, "Magisk path detected: " + path);
                            break;
                        }
                    }
                }
                if(count > 0)
                    isMagiskDetect = true;
                else{
                    isMagiskDetect = Native.detectMagiskNative();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return isMagiskDetect;
        }
    };


}
