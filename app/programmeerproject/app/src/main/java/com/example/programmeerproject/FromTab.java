package com.example.programmeerproject;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class FromTab extends Fragment {

    View view;

    private DatabaseReference mDatabase;
    private FirebaseUser user;

    String groupId;
    String groupName;

    ListView fromListView;

    TextView fromEmptyList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.from_tab, container, false);

        // Get current user
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        fromListView = (ListView) view.findViewById(R.id.fromlist);

        groupId = GroupIdSingleton.getInstance().getString();

        // Set group name by using method (onDataChange methods
        // can't return
        getGroupNameFromId(groupId);
        fromEmptyList = (TextView) view.findViewById(R.id.fromemptylisttext);



        setDatabaseListenerForListView("from", fromListView, groupId);

        return view;
    }

    public void getGroupNameFromId(final String id) {
        DatabaseReference groups = mDatabase.child("groups");
        groups.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    if (Objects.equals(child.getKey(), id)) {
                        groupName = (child.child("name").getValue().toString());
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setDatabaseListenerForListView(String dir, final ListView lv, String groupId) {
        final HashMap<String, ArrayList<String>> count = new HashMap<>();
        DatabaseReference dbRef = mDatabase.child("data").child(groupId).child(dir);
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        ArrayList<String> votes = new ArrayList<>();
                        for (DataSnapshot uid : child.getChildren()) {
                            if (!Objects.equals(String.valueOf(uid.getKey()), "place")) {
                                votes.add(uid.getKey());
                            }
                        }
                        count.put(child.getKey(), votes);
                    }
                    if (count.size() > 0) {
                        fromEmptyList.setVisibility(View.INVISIBLE);
                    }
                    populateList(count, lv);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), String.valueOf(databaseError), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void populateList(final HashMap<String, ArrayList<String>> hashmap, final ListView lv) {
        PinboardListAdapter adapter = new PinboardListAdapter(hashmap, user.getUid());
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View vw, int position, long id) {
                int viewId = lv.getId();
                String dir = getResources().getResourceEntryName(viewId).replaceAll("list", "");
                String itemKey = (new ArrayList<>(hashmap.keySet())).get(position);

                updateVotes(dir, itemKey);
            }
        });

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int viewId = lv.getId();
                String dir = getResources().getResourceEntryName(viewId).replaceAll("list", "");
                String itemKey = (new ArrayList<>(hashmap.keySet())).get(position);

                Intent intent = new Intent(getContext(), PinboardItemDialog.class);
                intent.putExtra("dir", dir);
                intent.putExtra("name", itemKey);
                intent.putExtra("groupid", groupId);
                intent.putExtra("groupname", groupName);
                startActivity(intent);

                return false;
            }
        });
    }

    public void updateVotes(String dir, String place) {
        final ArrayList<String> uids = new ArrayList<>();
        final DatabaseReference db = mDatabase.child("data").child(groupId).child(dir).child(place);

        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot userid: dataSnapshot.getChildren()) {
                        if (!Objects.equals(userid.getKey(), "place")) {
                            uids.add(userid.getKey());
                        }
                    }
                }
                if (uids.contains(user.getUid())) {
                    Toast.makeText(getContext(), "Vote removed", Toast.LENGTH_SHORT).show();
                    db.child(user.getUid()).removeValue();
                } else {
                    Toast.makeText(getContext(), "Vote added", Toast.LENGTH_SHORT).show();
                    db.child(user.getUid()).setValue(true);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Database error", Toast.LENGTH_LONG).show();
            }
        });
    }
}
