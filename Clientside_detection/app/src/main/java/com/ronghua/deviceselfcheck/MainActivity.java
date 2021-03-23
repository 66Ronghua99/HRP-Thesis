package com.ronghua.deviceselfcheck;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ronghua.deviceselfcheck.Utils.Const;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.Buffer;
import java.security.Permission;

public class MainActivity extends AppCompatActivity {

    private boolean isBound = false;
    private static String TAG = "DetectMagisk";
    private IBinder mRemote;
    private RootDetection checker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(!(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, Const.READ_PHONE_STATE);
        }
        if((getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE)
                != PackageManager.PERMISSION_GRANTED) ||
                (getApplicationContext().checkSelfPermission(Manifest.permission.CHANGE_WIFI_STATE)
                != PackageManager.PERMISSION_GRANTED)){
            requestPermissions(new String[]{Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE}, Const.WIFI_STATE);
        }
        if(!(getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)){
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
        findViewById(R.id.magisk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isBound){
                    try {
                        boolean bRet = checker.detectMagiskHide();
                        if(bRet)
                            Toast.makeText(getApplicationContext(), "Magisk is found!", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(getApplicationContext(), "Magisk is not found", Toast.LENGTH_LONG).show();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Service is not bound", Toast.LENGTH_LONG).show();
                }
            }
        });
        findViewById(R.id.sometest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectEmulator();
            }
        });

        findViewById(R.id.rootDetection).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checker.isRooted();
            }
        });

        findViewById(R.id.wifi).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               HardwareExamination.getInstance(getApplicationContext()).scanWifi();
           }
       });

    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, IsolatedService.class);
        getApplicationContext().bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IIsolatedProcess mServiceBinder = IIsolatedProcess.Stub.asInterface(service);
            checker = RootDetection.getInstance(getApplicationContext(), mServiceBinder);
            mRemote = service;
            isBound = true;
            Log.i(TAG, "service is bound");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case Const.READ_PHONE_STATE:
                if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                    Toast.makeText(getApplicationContext(), "Please get the permission then continue!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            case Const.WIFI_STATE:
                if(grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED){
                    Toast.makeText(getApplicationContext(), "Please get the permission then continue!", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void detectEmulator(){
        new EmulatorDetection(getApplicationContext())
                .detect();
    }
}
