package com.example.programmeerproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import java.util.Objects;

public class GroupActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    ListView listView;

    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mDatabase;
    private FirebaseUser user;

    ArrayList<String> mGroupNames = new ArrayList<>();
    ArrayList<String> mGroupIds = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.group_activity);

        buildGoogleApiClient();

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

            // Set button listeners
            findViewById(R.id.sign_out_button).setOnClickListener(this);
            findViewById(R.id.new_group_button).setOnClickListener(this);

            // Update Uid's for use in groups
            updateUserIds();

            // Get groups of logged in user and fill listView
            getUserGroups();
        }
    }

    public void setUpListView(ArrayList<String> values, final ListView view) {
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);

        view.setAdapter(adapter);
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View vw,
                                    int position, long id) {
                Intent intent = new Intent(GroupActivity.this, PinboardActivity.class);
                intent.putExtra("groupid", mGroupIds.get(position));
                startActivity(intent);
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

    public void getUserGroups() {
        // Database reader
        DatabaseReference groups = mDatabase.child("groups");
        groups.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot group : dataSnapshot.getChildren()) {
                    for (DataSnapshot userid: group.getChildren() ) {
                        if (Objects.equals(user.getUid(), userid.getKey())) {
                            mGroupIds.add(group.getKey());
                            mGroupNames.add(group.child("name").getValue().toString());
                        }
                    }
                }
                setUpListView(mGroupNames, listView);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_out_button:
                Toast.makeText(getApplicationContext(), getString(R.string.signed_out), Toast.LENGTH_SHORT).show();
                signOut();
                finish();
                break;
            case R.id.new_group_button:
                startActivity(new Intent(GroupActivity.this, NewGroupDialog.class));
                break;
        }
    }
}
