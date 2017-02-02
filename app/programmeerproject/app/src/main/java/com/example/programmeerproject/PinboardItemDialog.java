package com.example.programmeerproject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Plnnr
 * Sidney de Vries (10724087)
 *
 * Dialog for pinboard item. Here an item can be found on a map, or the item
 * can be removed from the group.
 */

public class PinboardItemDialog extends Activity implements View.OnClickListener {

    String name;
    String dir;
    String groupId;
    String groupName;

    private DatabaseReference mDatabase;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pinboard_item_dialog);

        // Get Firebase instance and user
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Receive relevant data from intent
        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        dir = intent.getStringExtra("dir");
        groupId = intent.getStringExtra("groupid");
        groupName = intent.getStringExtra("groupname");

        // Find views
        findViewById(R.id.deleteitem).setOnClickListener(this);
        findViewById(R.id.findonmap).setOnClickListener(this);
    }

    public void returnHome() {
        /* Is called when an item is deleted, returns to PinboardTabActivity */
        Intent home_intent = new Intent(getApplicationContext(), PinboardTabActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        home_intent.putExtra("dir", dir);
        home_intent.putExtra("name", name);
        home_intent.putExtra("groupid", groupId);
        home_intent.putExtra("groupname", groupName);
        startActivity(home_intent);
    }

    public void deleteFromDb(final String name, final String dir) {
        /* Deletes selected item from database after asking if the user is sure of it */
        Snackbar.make(findViewById(android.R.id.content),
                "Are you sure you want to delete this item?", Snackbar.LENGTH_LONG)
                .setAction("Yes!", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseReference toDelete = mDatabase.child("data")
                                .child(groupId).child(dir).child(name);
                        toDelete.setValue(null);
                        returnHome();
                    }
                })
                .setActionTextColor(Color.WHITE)
                .show();
    }

    public void findOnMap(String name) {
        /* Intent to go to FindOnMapActivity */
        Intent intent = new Intent(this, FindOnMapActivity.class);
        intent.putExtra("name", name);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.deleteitem:
                deleteFromDb(name, dir);
                break;
            case R.id.findonmap:
                findOnMap(name);
                break;
            default:
                break;
        }
    }
}
