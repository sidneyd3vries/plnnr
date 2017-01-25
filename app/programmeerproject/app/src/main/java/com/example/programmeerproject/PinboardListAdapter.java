package com.example.programmeerproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

public class PinboardListAdapter extends BaseAdapter {
    private final ArrayList mData;

    public PinboardListAdapter(Map<String, Integer> map) {
        mData = new ArrayList();
        mData.addAll(map.entrySet());
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Map.Entry<String, Integer> getItem(int position) {
        return (Map.Entry) mData.get(position);
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

        Map.Entry<String, Integer> item = getItem(position);

        TextView place = (TextView) result.findViewById(R.id.place);
        TextView count = (TextView) result.findViewById(R.id.count);

        place.setText(item.getKey());

        if (item.getValue() > 0) {
            count.setText(String.valueOf(item.getValue()) + "x");
        } else {
            count.setText("");
        }

        return result;
    }
}
