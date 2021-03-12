package com.ronghua.magiskdetect;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class IsolatedService extends Service {
    private String[] blackListPaths = {"/sbin/.magisk/", "/sbin/.core/mirror", "/sbin/.core/img", "/sbin/.core/db-0/magisk.db"};
    private static final String TAG = "DetectMagisk-Isolated";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new IIsolatedProcess.Stub() {
        @Override
        public boolean detectMagisk() throws RemoteException {
            boolean isMagiskPresent = false;
            File file = new File("/proc/self/mounts");

            Log.i(TAG, file.getPath());
            try {
                FileInputStream fis = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                String str;
                int count = 0;
                while ((str = reader.readLine()) != null && (count == 0)) {
                    //Log.d(TAG, "MountPath:"+ str);
                    for (String path : blackListPaths) {
                        if (str.contains(path)) {
                            Log.d(TAG, "Blacklisted Path found " + path);
                            count++;
                            break;
                        }
                    }
                }
                reader.close();
                fis.close();
                if (count > 0) {
                    Log.i(TAG, "MagiskHide detected at Java level");
                    isMagiskPresent = true;
                } else {
                    isMagiskPresent = Native.DetectMagisk();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return isMagiskPresent;
        }
     };
}
