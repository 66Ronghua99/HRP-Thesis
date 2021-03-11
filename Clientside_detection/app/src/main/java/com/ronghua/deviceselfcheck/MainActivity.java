package com.ronghua.deviceselfcheck;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private IIsolatedProcess mServiceBinder;
    private boolean isBound = false;
    private static String TAG = "DetectMagisk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button detectMagisk = findViewById(R.id.magisk);
        detectMagisk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isBound){
                    try {
                        boolean bRet = mServiceBinder.detectMagiskHide();
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
            mServiceBinder = IIsolatedProcess.Stub.asInterface(service);
            isBound = true;
            Log.i(TAG, "service is bound");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBinder = null;
            isBound = false;
        }
    };


}
