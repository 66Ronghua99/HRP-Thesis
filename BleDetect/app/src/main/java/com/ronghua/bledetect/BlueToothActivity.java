package com.ronghua.bledetect;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ronghua.bledetect.bleadv.BlueToothUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlueToothActivity extends AppCompatActivity {
    List<BlueToothUtils.BlueToothDetail> adapterList = new ArrayList<>();
    Map<String, BlueToothUtils.BlueToothDetail> map = new HashMap<>();
    SwipeRefreshLayout swipeRefreshLayout;
    BlueToothUtils mBluetooth;
    SomeAdapter adapter;
    BroadcastReceiver receiver;
    IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_detect_result);
        init();
    }

    private void init() {
        ListView listView = findViewById(R.id.list_view);
         mBluetooth = BlueToothUtils.getInstance(getApplicationContext());
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        adapterList = mBluetooth.getResult_list();
        for (BlueToothUtils.BlueToothDetail detail: adapterList){
            map.put(detail.getMac(), detail);
        }
        adapter = new SomeAdapter();
        listView.setAdapter(adapter);
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_orange_light,
                android.R.color.holo_green_light);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                longTimeOperation();
            }
        });
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BlueToothUtils.BLUETOOTH_UPDATED.equals(action)){
                    List<BlueToothUtils.BlueToothDetail> list = mBluetooth.getResult_list();
                    for (BlueToothUtils.BlueToothDetail detail:list){
                        if(map.containsKey(detail.getMac())){
                            int index = adapterList.indexOf(detail);
                            BlueToothUtils.BlueToothDetail d = adapterList.get(index);
                            d.setManufacture(detail.getManufacture());
                            d.setRssi(detail.getRssi());
                        }else {
                            adapterList.add(detail);
                            map.put(detail.getMac(), detail);
                        }
                    }
                    swipeRefreshLayout.setRefreshing(false);
                    adapter.notifyDataSetChanged();
                }
            }
        };
        intentFilter = new IntentFilter();
        intentFilter.addAction(BlueToothUtils.BLUETOOTH_UPDATED);
    }

    private void longTimeOperation() {
        // true，刷新开始，所以启动刷新的UI样式.
        swipeRefreshLayout.setRefreshing(true);
        mBluetooth.startScan();
    }

    class SomeAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return adapterList.size();
        }

        @Override
        public Object getItem(int position) {
            return adapterList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("SetTextI18n")
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            Holder holder;
            if(convertView == null){
                convertView = LayoutInflater.from(BlueToothActivity.this).inflate(R.layout.wifi_list_item, null);
                holder = new Holder();
                holder.msg = convertView.findViewById(R.id.name);
                holder.mac = convertView.findViewById(R.id.ssid);
                holder.rssi = convertView.findViewById(R.id.rssi);
                holder.time = convertView.findViewById(R.id.time);
                convertView.setTag(holder);
            }else{
                holder = (Holder)convertView.getTag();
            }
            BlueToothUtils.BlueToothDetail item = (BlueToothUtils.BlueToothDetail)getItem(position);
            holder.msg.setText("Name: " + new String(item.getManufacture()));
            holder.mac.setText("Addr: "+ item.getMac());
            holder.rssi.setText("RSSI vaslue: " + String.valueOf(item.getRssi()) + " dBm");
            holder.time.setText("Recv time: " + String.valueOf(item.getRecvTime()));
            return convertView;
        }

        class Holder{
            TextView msg;
            TextView mac;
            TextView rssi;
            TextView time;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
        Log.i(MainActivity.TAG, "register detect list receiver");
        mBluetooth.startScan();
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        Log.i(MainActivity.TAG, "unregister detect list receiver");
        mBluetooth.stopScan();
    }
}
