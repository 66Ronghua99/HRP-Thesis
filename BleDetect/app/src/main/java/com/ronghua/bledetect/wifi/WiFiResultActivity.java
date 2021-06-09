package com.ronghua.bledetect.wifi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

import com.ronghua.bledetect.MainActivity;
import com.ronghua.bledetect.R;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class WiFiResultActivity extends AppCompatActivity {
    List<WiFiInfo.WifiResultDetail> adapterList = new ArrayList<>();
    SwipeRefreshLayout swipeRefreshLayout;
    WiFiInfo wiFiInfo;
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
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        wiFiInfo = WiFiInfo.getInstance(this);
        adapterList = wiFiInfo.getmWifiList();
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
                if (WiFiInfo.LIST_UPDATED.equals(action)){
                    adapterList = wiFiInfo.getmWifiList();
                    swipeRefreshLayout.setRefreshing(false);
                    adapter.notifyDataSetChanged();
                }
            }
        };
        intentFilter = new IntentFilter();
        intentFilter.addAction(WiFiInfo.LIST_UPDATED);
    }

    private void longTimeOperation() {
        // true，刷新开始，所以启动刷新的UI样式.
        swipeRefreshLayout.setRefreshing(true);
        wiFiInfo.wifiScanList();
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
                convertView = LayoutInflater.from(WiFiResultActivity.this).inflate(R.layout.wifi_list_item, null);
                holder = new Holder();
                holder.name = convertView.findViewById(R.id.name);
                holder.mac = convertView.findViewById(R.id.ssid);
                holder.rssi = convertView.findViewById(R.id.rssi);
                convertView.setTag(holder);
            }else{
                holder = (Holder)convertView.getTag();
            }
            WiFiInfo.WifiResultDetail item = (WiFiInfo.WifiResultDetail)getItem(position);
            holder.name.setText("Name: " + item.getName());
            holder.mac.setText("Addr: "+ item.getMacAddr());
            holder.rssi.setText("RSSI value: " + String.valueOf(item.getRssi()) + " dBm");
            return convertView;
        }

        class Holder{
            TextView name;
            TextView mac;
            TextView rssi;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
        Log.i(MainActivity.TAG, "register detect list receiver");
    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        Log.i(MainActivity.TAG, "unregister detect list receiver");
    }
}
