package com.example.programmeerproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener  {

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pinboard_activity);

        // Get id of current group
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupid");

        fromListView = (ListView) findViewById(R.id.fromlist);
        toListView = (ListView) findViewById(R.id.tolist);

        // Get current user
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        findViewById(R.id.add_plan_button).setOnClickListener(this);

        buildGoogleApiClient();

        setDatabaseListenerForListView("from", fromListView);

        setDatabaseListenerForListView("to", toListView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Get id of current group
        Intent intent = getIntent();
        String groupId = intent.getStringExtra("groupid");
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

    public void populateList(final HashMap<String, Integer> hashmap, final ListView lv) {
        PinboardListAdapter adapter = new PinboardListAdapter(hashmap);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View vw, int position, long id) {
                int viewId = lv.getId();
                String dir = getResources().getResourceEntryName(viewId).replaceAll("list", "");

                String itemKey = (new ArrayList<String>(hashmap.keySet())).get(position);

                updateVotes(dir, itemKey);
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

    public void setDatabaseListenerForListView(String dir, final ListView lv) {
        //final ArrayList<String> keys = new ArrayList<>();
        final HashMap<String, Integer> count = new HashMap<>();

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_plan_button:
                Intent intent = new Intent(this, MapsActivity.class);
                intent.putExtra("groupid", groupId);
                startActivity(intent);
                break;
        }
    }
}
