package com.ronghua.deviceselfcheck;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.ronghua.deviceselfcheck.EmulatorDetector;

public class EmulatorDetection {
    private Context mContext;

    public EmulatorDetection(Context mContext){
        this.mContext = mContext;
    }


    public void detect(){
        EmulatorDetector.with(mContext)
                .setCheckTelephony(true)
                .addPackageName("com.bluestacks")
                .setDebug(true)
                .detect(new EmulatorDetector.OnEmulatorDetectorListener() {
                    @Override
                    public void onResult(final boolean isEmulator) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if(isEmulator)
                                    Toast.makeText(mContext, "This is an Emulator", Toast.LENGTH_LONG).show();
                                else
                                    Toast.makeText(mContext, "This is not an Emulator", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(mContext, DetectResultActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("com.ronghua.deviceselfcheck", "emulator");
                                mContext.startActivity(intent);
                            }
                        });
                    }
                });
    }
}
