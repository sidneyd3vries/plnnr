package com.example.programmeerproject;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Plnnr
 * Sidney de Vries (10724087)
 *
 * Tabfragment used by PinboardTabActivity.class
 * Fragment gets and shows data from database
 *
 */

public class FromTab extends Fragment {

    View view;

    public DatabaseReference mDatabase;
    public FirebaseUser user;

    public String groupId;
    public String groupName;

    ListView fromListView;

    TextView fromEmptyList;

    TabMethods tm = new TabMethods();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.from_tab, container, false);

        // Get current user
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Find ListView
        fromListView = (ListView) view.findViewById(R.id.fromlist);

        // Get groupId from singleton
        groupId = GroupIdSingleton.getInstance().getString();

        // Set group name by using method (onDataChange methods
        // can't return)
        tm.getGroupNameFromId(groupId, getContext(), mDatabase);
        fromEmptyList = (TextView) view.findViewById(R.id.fromemptylisttext);

        // Populate listView with data from FireBase
        tm.setDatabaseListenerForListView("from", fromListView, groupId, groupName,
                fromEmptyList, getContext());

        return view;
    }
}
