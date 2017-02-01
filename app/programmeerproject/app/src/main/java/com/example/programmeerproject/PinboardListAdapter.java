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

/**
 * Plnnr
 * Sidney de Vries (10724087)
 *
 * List adapter for the lists in FromTab and ToTab. Takes HashMap of String and ArrayList<String>
 * and converts the ArrayList in an amount of votes and shows the amount in the listView.
 */

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

        // ArrayList is list of all user id's that voted on item
        // If people have voted on item including current user, set color to darker
        if (item.getValue().size() > 0 && item.getValue().contains(userid)) {
            count.setText(String.valueOf(item.getValue().size()) + "x");
            container.setBackgroundColor(Color.parseColor("#39A679"));
        // Else if there are votes but not of current user, set color to background color
        } else if (item.getValue().size() > 0){
            count.setText(String.valueOf(item.getValue().size()) + "x");
            container.setBackgroundColor(Color.parseColor("#E1EfE9"));
        // Else there are no votes, set color to backgorund color
        } else {
            count.setText("");
            container.setBackgroundColor(Color.parseColor("#E1EfE9"));
        }
        return result;
    }
}
