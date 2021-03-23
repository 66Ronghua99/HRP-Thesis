package com.ronghua.deviceselfcheck;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DetectResultActivity extends AppCompatActivity {
    List<String> adapterList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_result);
        ListView listView = findViewById(R.id.list_view);
        Intent intent = getIntent();
        String from = intent.getStringExtra("com.ronghua.deviceselfcheck");
        if (from.equals("root")){
            RootDetection rd = RootDetection.getInstance();
            adapterList = rd.getRootTraitsList();
        }else{
            EmulatorDetector ed = EmulatorDetector.with(this);
            adapterList = ed.getmResultList();
        }
        Log.i("RootDetect", adapterList.toString());
        SomeAdapter adapter = new SomeAdapter();
        listView.setAdapter(adapter);
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

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            Holder holder;
            if(convertView == null){
                convertView = LayoutInflater.from(DetectResultActivity.this).inflate(R.layout.list_item, null);
                holder = new Holder();
                holder.text = convertView.findViewById(R.id.listTextView);
                convertView.setTag(holder);
            }else{
                holder = (Holder)convertView.getTag();
            }
            holder.text.setText((String)getItem(position));
            return convertView;
        }

        class Holder{
            TextView text;
        }
    }
}