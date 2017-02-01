package com.example.programmeerproject;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Plnnr
 * Sidney de Vries (10724087)
 *
 * Class where all methods used in FromTab and ToTab are defined. Methods have a lot of
 * arguments because things like context or other variables are not defined here. Prevents
 * a lot of double code.
 *
 */

public class TabMethods {

    String groupName;

    public void getGroupNameFromId(final String id,
                                   final Context ctx,
                                   DatabaseReference mDatabase) {
        /* Finds groupname matching a groupId */
        DatabaseReference groups = mDatabase.child("groups");
        groups.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    if (Objects.equals(child.getKey(), id)) {
                        // Set groupname to the name of the groupId from singleton
                        groupName = (child.child("name").getValue().toString());
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ctx, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setDatabaseListenerForListView(String dir,
                                               final ListView lv,
                                               final String groupId,
                                               final String groupName,
                                               final TextView empty,
                                               final Context ctx,
                                               final DatabaseReference mDatabase,
                                               final FirebaseUser user) {
        /* Gets data from database and calls populateList to populate the listViews with
        data from the database
         */
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
                        empty.setVisibility(View.INVISIBLE);
                    }
                    populateList(count, lv, ctx, groupId, groupName, user, mDatabase);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ctx, String.valueOf(databaseError), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void populateList(final HashMap<String, ArrayList<String>> hashmap,
                             final ListView lv,
                             final Context ctx,
                             final String groupId,
                             final String groupName,
                             final FirebaseUser user,
                             final DatabaseReference mDatabase) {
        /* Fill listview with data from hashmap and sets onItemClickListener and
        onItemLongClickListener. Uses PinboardListAdapter
         */
        PinboardListAdapter adapter = new PinboardListAdapter(hashmap, user.getUid());
        lv.setAdapter(adapter);

        // Set onClickListener
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View vw, int position, long id) {
                int viewId = lv.getId();
                String dir = vw.getResources().getResourceEntryName(viewId).replaceAll("list", "");
                String itemKey = (new ArrayList<>(hashmap.keySet())).get(position);

                updateVotes(dir, itemKey, groupId, mDatabase, user, ctx);
            }
        });

        // Set onLongClickListener (opens PinboardItemDialog)
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int viewId = lv.getId();
                String dir = view.getResources().getResourceEntryName(viewId).replaceAll("list", "");
                String itemKey = (new ArrayList<>(hashmap.keySet())).get(position);

                Intent intent = new Intent(ctx, PinboardItemDialog.class);
                intent.putExtra("dir", dir);
                intent.putExtra("name", itemKey);
                intent.putExtra("groupid", groupId);
                intent.putExtra("groupname", groupName);
                ctx.startActivity(intent);

                return false;
            }
        });
    }

    public void updateVotes(String dir,
                            String place,
                            String groupId,
                            DatabaseReference mDatabase,
                            final FirebaseUser user,
                            final Context ctx) {
        /* Updates vote if item clicked in the list */
        final ArrayList<String> uids = new ArrayList<>();
        final DatabaseReference db = mDatabase.child("data").child(groupId).child(dir).child(place);

        // Set singleValueEventListener
        db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot userid: dataSnapshot.getChildren()) {
                        if (!Objects.equals(userid.getKey(), "place")) {
                            // Adds uids of people who voted on item to ArrayList
                            uids.add(userid.getKey());
                        }
                    }
                }
                // If users id is in the list of people who voted, remove vote
                // Else add vote of user to item
                if (uids.contains(user.getUid())) {
                    Toast.makeText(ctx, "Vote removed", Toast.LENGTH_SHORT).show();
                    db.child(user.getUid()).removeValue();
                } else {
                    Toast.makeText(ctx, "Vote added", Toast.LENGTH_SHORT).show();
                    db.child(user.getUid()).setValue(true);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ctx, "Database error", Toast.LENGTH_LONG).show();
            }
        });
    }
}
