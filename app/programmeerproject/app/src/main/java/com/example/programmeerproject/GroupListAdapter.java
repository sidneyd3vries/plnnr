package com.example.programmeerproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

public class GroupListAdapter extends BaseAdapter {
    private final ArrayList mData;

    public GroupListAdapter(Map<String, ArrayList<String>> map) {
        mData = new ArrayList();
        mData.addAll(map.entrySet());
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Map.Entry<String, ArrayList<String>> getItem(int position) {
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
            result = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_adapter_item, parent, false);
        } else {
            result = view;
        }

        Map.Entry<String, ArrayList<String>> item = getItem(position);

        TextView groupname = (TextView) result.findViewById(R.id.groupname);
        TextView members = (TextView) result.findViewById(R.id.members);

        groupname.setText(item.getKey());
        members.setText(String.valueOf(item.getValue()).replaceAll("[\\[\\]]", ""));

        return result;
    }
}

