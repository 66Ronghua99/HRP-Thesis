package com.ronghua.deviceselfcheck;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class IsolatedService extends Service {
    public static String[] blackListMountPaths = {"/sbin/.magisk/",
            "/sbin/.core/mirror",
            "/sbin/.core/img",
            "/sbin/.core/db-0/magisk.db"};
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private IBinder mBinder = new IIsolatedProcess.Stub() {
        @Override
        public boolean detectMagiskHide() throws RemoteException {
            boolean isMagiskDetect = false;
            int count = 0;
            try {
                FileReader fr = new FileReader("/proc/self/mounts");
                BufferedReader br = new BufferedReader(fr);
                String line = "";
                while((line = br.readLine()) != null){
                    for(String path:blackListMountPaths){
                        if(line.contains(path)){
                            count++;
                            break;
                        }
                    }
                }
                if(count < 0)
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
