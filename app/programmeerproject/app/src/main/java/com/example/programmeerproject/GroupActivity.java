package com.example.programmeerproject;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
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

/**
 * Plnnr
 * Sidney de Vries (10724087)
 *
 * Shows the groups the user is part of. Also asks first time users permission
 * to use their location.
 *
 * Source for permission check:
 * https://www.androidtutorialpoint.com/intermediate/android-map-app-showing-current-location-android/
 */

public class GroupActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    ListView listView;
    TextView emptyList;

    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mDatabase;
    private FirebaseUser user;

    ArrayList<String> mGroupNames = new ArrayList<>();
    ArrayList<String> mGroupIds = new ArrayList<>();

    LinkedHashMap<String, ArrayList<String>> groupMembers = new LinkedHashMap<>();
    LinkedHashMap<String, String> userMap = new LinkedHashMap<>();

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_activity);

        // Build google api client
        setUpGoogleApiClient();

        // Checks and asks for location permission
        checkLocationPermission();

        // Sets up floating action button
        setUpFab();

        // Get current user
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // If somehow user is null return to SignInActivity
        if (user == null) {
            Intent intent = new Intent(this, SignInActivity.class);
            startActivity(intent);
        } else {
            // Find listView
            listView = (ListView) findViewById(R.id.list);
            emptyList = (TextView) findViewById(R.id.emptylist);

            // Update Uid's for use in groups
            updateUserIds();

            // Get useruidmap which gets the groups of current users
            // and then populates the listview
            getUserIdMap();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Clear HashMaps and ArrayLists to remove old data
        groupMembers = new LinkedHashMap<>();
        userMap = new LinkedHashMap<>();
        mGroupNames = new ArrayList<>();
        mGroupIds = new ArrayList<>();
        // Reload data and repopulate list
        getUserIdMap();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Creates overflow menu in toolbar */
        getMenuInflater().inflate(R.menu.group_overflow_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* OnClickListener for overflow menu */
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(),
                "Connection failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission was granted.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            setUpGoogleApiClient();
                        }
                    }
                } else {
                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    public void setUpFab() {
        /* Sets up floating action button with onClick method to go to NewGroupDialog */
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.addgroupfab);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(GroupActivity.this, NewGroupDialog.class));
            }
        });
    }

    public void setUpListView(final LinkedHashMap<String, ArrayList<String>> values,
                              final ListView view) {
        /* Sets up listview with groups the user is part of. Is called in getUserGroups()
         User GroupListAdapter to populate view with HashMap
         */
        GroupListAdapter adapter = new GroupListAdapter(values);

        // Show or hide textview that indicates if a list is empty
        if (values.size() == 0) {
            emptyList.setVisibility(View.VISIBLE);
        } else {
            emptyList.setVisibility(View.GONE);
        }

        // Set adapter and onItemClickListener
        view.setAdapter(adapter);
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View vw,
                                    int position, long id) {
                Intent intent = new Intent(GroupActivity.this, PinboardTabActivity.class);
                intent.putExtra("groupid", mGroupIds.get(position));
                intent.putExtra("groupname", mGroupNames.get(position));
                startActivity(intent);
            }
        });
    }

    public void updateUserIds() {
        /* Adds user's id and their email to userids section in database */
        mDatabase.child("userids/").child(user.getUid()).setValue(user.getEmail());
    }

    public void getUserIdMap() {
        /* Gets map of userid and user email. Map is used to show who is part of the group */
        DatabaseReference userids = mDatabase.child("userids");
        userids.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot userid: dataSnapshot.getChildren()) {
                    // Add all userid's and emails to map
                    userMap.put(userid.getKey(), userid.getValue().toString());
                }
                // Get groups user is part of
                getUserGroups(userMap);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(GroupActivity.this,
                        "Database error", Toast.LENGTH_SHORT).show();
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
                    // If user is in a group update groupMembers map used for listview
                    if (userlist.contains(user.getEmail())) {
                        mGroupIds.add(group.getKey());
                        mGroupNames.add(group.child("name").getValue().toString());
                        groupMembers.put(group.child("name").getValue().toString(), userlist);
                    }
                }
                // Set up the listview with the map <group name, arraylist of emails of members>
                setUpListView(groupMembers, listView);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("GroupActivity", String.valueOf(databaseError));
            }
        });
    }


    public void setUpGoogleApiClient() {
        /* Build google api client with correct api's */
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(LocationServices.API)
                .enableAutoManage(this, this)
                .build();
    }

    private void signOut() {
        /* Signs user out */
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

    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

}
