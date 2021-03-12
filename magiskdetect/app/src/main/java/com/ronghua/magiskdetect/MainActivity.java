package com.ronghua.magiskdetect;

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
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private IIsolatedProcess serviceBinder;
    private boolean bServiceBound;
    private static final String TAG = "DetectMagisk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bServiceBound){
                    try {
                        boolean bRet = serviceBinder.detectMagisk();
                        if(bRet){
                            Toast.makeText(getApplicationContext(), "Magisk or Su file is found!", Toast.LENGTH_LONG).show();
                        }else{
                            Toast.makeText(getApplicationContext(), "Magisk is not found", Toast.LENGTH_LONG).show();
                        }
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

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceBinder = IIsolatedProcess.Stub.asInterface(service);
            bServiceBound =true;
            Log.i(TAG, "Service bound");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBinder = null;
            bServiceBound = false;
            Log.i(TAG, "Service unbound");
        }
    };


}
