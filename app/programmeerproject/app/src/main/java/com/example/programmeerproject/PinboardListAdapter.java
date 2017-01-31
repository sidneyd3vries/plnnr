package com.example.programmeerproject;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

class PinboardListAdapter extends BaseAdapter {
    private final ArrayList<Map.Entry<String, ArrayList<String>>> mData;
    private final String userid;

    PinboardListAdapter(Map<String, ArrayList<String>> map, String uid) {
        mData = new ArrayList<>();
        mData.addAll(map.entrySet());
        userid = uid;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Map.Entry<String, ArrayList<String>> getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final View result;

        if (view == null) {
            result = LayoutInflater.from(parent.getContext()).inflate(R.layout.pinboard_adapter_item, parent, false);
        } else {
            result = view;
        }

        Map.Entry<String, ArrayList<String>> item = getItem(position);

        TextView place = (TextView) result.findViewById(R.id.place);
        TextView count = (TextView) result.findViewById(R.id.count);
        RelativeLayout container = (RelativeLayout) result.findViewById(R.id.container);

        place.setText(item.getKey());

        if (item.getValue().size() > 0 && item.getValue().contains(userid)) {
            count.setText(String.valueOf(item.getValue().size()) + "x");
            container.setBackgroundColor(Color.parseColor("#DDDDDD"));
        } else if (item.getValue().size() > 0){
            count.setText(String.valueOf(item.getValue().size()) + "x");
            container.setBackgroundColor(Color.parseColor("#FBFFFB"));
        } else {
            count.setText("");
            container.setBackgroundColor(Color.parseColor("#FBFFFB"));
        }
        return result;
    }
}
