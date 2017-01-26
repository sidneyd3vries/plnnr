package com.example.programmeerproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;

public class GroupActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    //TODO prevent backnavigation here

    ListView listView;

    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mDatabase;
    private FirebaseUser user;

    ArrayList<String> mGroupNames = new ArrayList<>();
    ArrayList<String> mGroupIds = new ArrayList<>();

    LinkedHashMap<String, ArrayList<String>> groupMembers = new LinkedHashMap<>();
    LinkedHashMap<String, String> userMap = new LinkedHashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_activity);

        buildGoogleApiClient();

        setUpFab();

        // Get current user
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        if (user == null) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        } else {
            // Find listView
            listView = (ListView) findViewById(R.id.list);

            // Update Uid's for use in groups
            updateUserIds();

            // Get useruidmap which gets the groups of current users
            // and then populates the listview
            getUserUidMap();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_overflow_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //TODO join existing group?
        switch (item.getItemId()) {
            case R.id.signout:
                Toast.makeText(getApplicationContext(), getString(R.string.signed_out), Toast.LENGTH_SHORT).show();
                signOut();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setUpFab() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addgroupfab);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(GroupActivity.this, NewGroupDialog.class));
            }
        });
    }

    public void setUpListView(final LinkedHashMap<String, ArrayList<String>> values, final ListView view) {
        GroupListAdapter adapter = new GroupListAdapter(values);

        view.setAdapter(adapter);
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View vw,
                                    int position, long id) {
                Intent intent = new Intent(GroupActivity.this, PinboardActivity.class);
                intent.putExtra("groupid", mGroupIds.get(position));
                intent.putExtra("groupname", mGroupNames.get(position));
                startActivity(intent);
            }
        });

        view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(GroupActivity.this, "Long Click", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    public void updateUserIds() {
        if (user != null) {
            mDatabase.child("userids/").child(user.getUid()).setValue(user.getEmail());
        } else {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        }
    }

    private void signOut() {
        // Firebase sign out
        FirebaseAuth.getInstance().signOut();
        // Google sign out
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });
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

    public void getUserUidMap() {
        DatabaseReference userids = mDatabase.child("userids");
        userids.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userid: dataSnapshot.getChildren()) {
                    //Log.d("USERID", String.valueOf(userid));
                    userMap.put(userid.getKey(), userid.getValue().toString());
                }
                Log.d("@@@MAP@@@", String.valueOf(userMap));
                getUserGroups(userMap);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(GroupActivity.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getUserGroups(final HashMap<String, String> usermap) {
        // Database reader
        DatabaseReference groups = mDatabase.child("groups");
        groups.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // For every item in groups
                for (DataSnapshot group : dataSnapshot.getChildren()) {
                    ArrayList<String> userlist = new ArrayList<>();
                    // For every member of item
                    for (DataSnapshot userid: group.getChildren() ) {
                        // Make list of all emails of members of a group
                        if (!Objects.equals(userid.getKey(), "name")) {
                            userlist.add(usermap.get(userid.getKey()));
                        }
                    }

                    //Log.d("USERLIST EMAILS", String.valueOf(userlist));
                    // If user is in a group update groupMembers map used for listview
                    if (userlist.contains(user.getEmail())) {
                        mGroupIds.add(group.getKey());
                        mGroupNames.add(group.child("name").getValue().toString());
                        groupMembers.put(group.child("name").getValue().toString(), userlist);
                    }
                }
                Log.d("HASHMAP", String.valueOf(groupMembers));
                // Set up the listview with the map <group name, arraylist of emails of members>
                setUpListView(groupMembers, listView);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("GroupActivity", String.valueOf(databaseError));
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
