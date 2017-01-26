package com.example.programmeerproject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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


public class PinboardActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private GoogleApiClient mGoogleApiClient;

    ListView fromListView;
    ListView toListView;

    //TODO implement ability to add more lists
    ArrayList<String> fromKeys = new ArrayList<>();
    ArrayList<String> fromValues = new ArrayList<>();

    ArrayList<String> toKeys = new ArrayList<>();
    ArrayList<String> toValues = new ArrayList<>();

    String groupId;
    String groupName;
    String dir;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pinboard_activity);
        Log.d("$$$$$$$$$$$$$ pin", "ONCREATE");

        setUpFab();

        // Get id of current group
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupid");
        groupName = intent.getStringExtra("groupname");
        dir = intent.getStringExtra("dir");

        Log.d("%%%%%%%%", String.valueOf(groupId));


        setTitle(groupName);

        fromListView = (ListView) findViewById(R.id.fromlist);
        toListView = (ListView) findViewById(R.id.tolist);

        // Get current user
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        buildGoogleApiClient();

        setDatabaseListenerForListView("from", fromListView, groupId);

        setDatabaseListenerForListView("to", toListView, groupId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Get id of current group
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupid");
        groupName = intent.getStringExtra("groupname");
        dir = intent.getStringExtra("dir");
        setTitle(groupName);
        Log.d("ONRESUME", groupId);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

//    public void setUpListView(ArrayList<String> values, final ListView view) {
//
//        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, values);
//
//        view.setAdapter(adapter);
//
//        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View vw, int position, long id) {
//                int viewId = view.getId();
//                String dir = getResources().getResourceEntryName(viewId).replaceAll("list", "");
//
//                String  itemValue    = (String) view.getItemAtPosition(position);
//
//                updateVotes(dir, itemValue);
//            }
//        });
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pinboard_overflow_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.leavegroup:
                leaveGroup();
                return true;
            case R.id.help:
                getHelp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setUpFab() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addplanfab);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("groupid", groupId);
                intent.putExtra("groupname", groupName);
                startActivity(intent);
            }
        });
    }

    public void populateList(final HashMap<String, Integer> hashmap, final ListView lv) {
        PinboardListAdapter adapter = new PinboardListAdapter(hashmap);
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

                Intent intent = new Intent(getApplicationContext(), PinboardItemDialog.class);
                intent.putExtra("dir", dir);
                intent.putExtra("name", itemKey);
                intent.putExtra("groupid", groupId);
                intent.putExtra("groupname", groupName);
                startActivity(intent);

                return false;
            }
        });
    }

    public void leaveGroup() {
        Snackbar.make(findViewById(android.R.id.content), "Are you sure you want to leave the group?", Snackbar.LENGTH_INDEFINITE)
                .setAction("Yes!", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseReference groupToLeave = mDatabase.child("groups").child(groupId).child(user.getUid());
                        //TODO check if user is last member of group
                        groupToLeave.setValue(null);
                        Intent intent = new Intent(getApplicationContext(), GroupActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .setActionTextColor(Color.WHITE)
                .show();
    }

    public void getHelp() {
        Snackbar.make(findViewById(android.R.id.content),
                "Click an item to vote. Long click an item to delete it or find it on the map.",
                Snackbar.LENGTH_LONG).show();
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
                    Toast.makeText(PinboardActivity.this, "Vote removed", Toast.LENGTH_SHORT).show();
                    db.child(user.getUid()).removeValue();
                } else {
                    Toast.makeText(PinboardActivity.this, "Vote added", Toast.LENGTH_SHORT).show();
                    db.child(user.getUid()).setValue(true);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(PinboardActivity.this, "Database error", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void setDatabaseListenerForListView(String dir, final ListView lv, String groupId) {
        //final ArrayList<String> keys = new ArrayList<>();
        final HashMap<String, Integer> count = new HashMap<>();
        Log.d("GODVEDOMME", String.valueOf(groupId) + " " + String.valueOf(dir));
        DatabaseReference dbRef = mDatabase.child("data").child(groupId).child(dir);
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    //keys.clear();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        //keys.add(child.getKey());

                        int counter = 0;
                        for (DataSnapshot uid : child.getChildren()) {
                            if (!Objects.equals(String.valueOf(uid.getKey()), "place")) {
                                counter++;
                            }
                        }
                        count.put(child.getKey(), counter);
                    }
                    //Log.d("HASHMAP", String.valueOf(count));
                    populateList(count, lv);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(PinboardActivity.this, String.valueOf(databaseError), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void buildGoogleApiClient() {
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(), "Connection failed", Toast.LENGTH_SHORT).show();
    }

}
