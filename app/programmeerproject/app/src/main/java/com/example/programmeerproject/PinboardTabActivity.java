package com.example.programmeerproject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Plnnr
 * Sidney de Vries (10724087)
 *
 * Fragment manager of FromTab and ToTab. Tabs are set up and also shows overflow
 * menu where users can leave the group, add people to the group and change the name
 * of the group.
 */

public class PinboardTabActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    private ViewPager viewPager;

    String groupName;
    String groupId;
    String dir;

    private DatabaseReference mDatabase;
    private FirebaseUser user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pinboard_tab_activity);

        // Set up floating action button
        setUpFab();

        // Get id of current group
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupid");
        groupName = intent.getStringExtra("groupname");

        // Set singleton for fragments
        GroupIdSingleton.getInstance().setString(groupId);

        // Add toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(groupName);
        setSupportActionBar(toolbar);

        // Get user instance
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Add the tabs
        addTabs();
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

    public void setUpFab() {
        /* Sets up floating action button used to go to the map activity */
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

    private void addTabs() {
        /* Tabspageradapter used to set the tabs */
        TabsPagerAdapter tabsAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(tabsAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setText("From");
        tabLayout.getTabAt(1).setText("To");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pinboard_overflow_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.leavegroup:
                leaveGroupConfirmation();
                return true;
            case R.id.help:
                getHelp();
                return true;
            case R.id.changename:
                goToDialog(ChangeNameDialog.class);
                return true;
            case R.id.adduser:
                goToDialog(AddUserDialog.class);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void goToDialog(Class dialogClass) {
        /* Universal method to go to a dialog */
        Intent intent = new Intent(this, dialogClass);
        intent.putExtra("groupid", groupId);
        intent.putExtra("groupname", groupName);
        startActivity(intent);
        finish();
    }

    public void leaveGroupConfirmation() {
        /* Removes user from group after asking if they're sure */
        Snackbar.make(findViewById(android.R.id.content),
                "Are you sure you want to leave the group?",
                Snackbar.LENGTH_LONG)
                .setAction("Yes!", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        leaveGroup();
                    }
                })
                .setActionTextColor(Color.WHITE)
                .show();
    }

    public void leaveGroup() {
        final DatabaseReference groupToLeave = mDatabase.child("groups").child(groupId);
        groupToLeave.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = 0;
                for (DataSnapshot child: dataSnapshot.getChildren()) {
                    count++;
                }
                // If one user left, delete group
                if (count == 2) {
                    deleteGroup(groupToLeave);
                } else {
                    groupToLeave.child(user.getUid()).setValue(null);
                    exit();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(),
                        "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteGroup(DatabaseReference group) {
        group.setValue(null);
        DatabaseReference dataToDelete = mDatabase.child("data").child(groupId);
        dataToDelete.setValue(null);
        exit();
    }

    public void getHelp() {
        /* Show a small help snackbar to user */
        final Snackbar snackBar = Snackbar.make(findViewById(android.R.id.content),
                "Click an item to vote. Long click an item to delete it or find it on the map.",
                Snackbar.LENGTH_INDEFINITE);

        snackBar.setAction("Got it", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackBar.dismiss();
            }
        })
        .setActionTextColor(Color.WHITE);
        snackBar.show();
    }

    public void exit() {
        /* Makes user exit group after leaving */
        Intent intent = new Intent(getApplicationContext(), GroupActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
            viewPager.setCurrentItem(tab.getPosition());
            }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) { }

    @Override
    public void onTabReselected(TabLayout.Tab tab) { }
}

