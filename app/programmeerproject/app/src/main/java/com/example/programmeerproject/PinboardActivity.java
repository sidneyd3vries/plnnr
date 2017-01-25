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
import android.widget.TextView;
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
import java.util.List;


public class PinboardActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener  {

    private DatabaseReference mDatabase;
    private FirebaseUser user;
    private GoogleApiClient mGoogleApiClient;

    ListView fromListView;
    ListView toListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pinboard_activity);

        // Get id of current group
        Intent intent = getIntent();
        String groupId = intent.getStringExtra("groupid");

        fromListView = (ListView) findViewById(R.id.fromlist);
        toListView = (ListView) findViewById(R.id.tolist);

        // Get current user
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        findViewById(R.id.add_plan_button).setOnClickListener(this);

        configSignInBuildClient();

        setDatabaseListenerForListView("from", fromListView);

        setDatabaseListenerForListView("to", toListView);

    }

    public void setUpListView(ArrayList<String> values, final ListView view) {

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);

        view.setAdapter(adapter);

        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View vw,
                                    int position, long id) {

                int itemPosition     = position;

                String  itemValue    = (String) view.getItemAtPosition(position);

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

    public void setDatabaseListenerForListView(String dir, final ListView view) {
        final ArrayList<String> keys = new ArrayList<>();
        final ArrayList<String> values = new ArrayList<>();
        // Database reader
        DatabaseReference personalDb = mDatabase.child("users").child(user.getUid() + "/" + dir);
        personalDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    keys.clear();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        values.add(String.valueOf(child.getValue()));
                        keys.add(child.getKey());
                    }
                    setUpListView(keys, view);
                } else {
                    Log.d("dbListener", "no items for user yet");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("MainActivity", String.valueOf(databaseError));
            }
        });
    }

    public void configSignInBuildClient() {
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
                startActivity(intent);
                break;
        }
    }
}
