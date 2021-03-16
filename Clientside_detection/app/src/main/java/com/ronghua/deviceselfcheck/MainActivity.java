package com.ronghua.deviceselfcheck;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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

    public Context applicationContext(){
        return getApplicationContext();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button detectMagisk = findViewById(R.id.magisk);
        Button rootDetect = findViewById(R.id.rootDetection);
        detectMagisk.setOnClickListener(new View.OnClickListener() {
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
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    try {
                        _data.writeInterfaceToken("com.ronghua.deviceselfcheck.IIsolatedProcess");
                        boolean _status = mRemote.transact(IIsolatedProcess.Stub.TRANSACTION_detectMagiskHide, _data, _reply, 0);
                        _reply.readException();
                        boolean bRet = (0 != _reply.readInt());
                        if(bRet)
                            Toast.makeText(getApplicationContext(), "Magisk is found!", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(getApplicationContext(), "Magisk is not found", Toast.LENGTH_LONG).show();
                    } finally {
                        _reply.recycle();
                        _data.recycle();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            }
        });

        rootDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checker.isRooted();
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
            checker = new RootDetection(getApplicationContext(), mServiceBinder);
            mRemote = service;
            isBound = true;
            Log.i(TAG, "service is bound");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };


}
