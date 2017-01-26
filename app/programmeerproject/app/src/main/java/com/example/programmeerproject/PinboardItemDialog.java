package com.example.programmeerproject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PinboardItemDialog extends Activity implements View.OnClickListener {

    String name;
    String dir;
    String groupId;
    String groupName;

    TextView title;

    private DatabaseReference mDatabase;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pinboard_item_dialog);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        dir = intent.getStringExtra("dir");
        groupId = intent.getStringExtra("groupid");
        groupName = intent.getStringExtra("groupname");

        title = (TextView) findViewById(R.id.title);
        title.setText(name);

        findViewById(R.id.deleteitem).setOnClickListener(this);
        findViewById(R.id.findonmap).setOnClickListener(this);
    }

    public void returnHome() {
        Intent home_intent = new Intent(getApplicationContext(), PinboardActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        home_intent.putExtra("dir", dir);
        home_intent.putExtra("name", name);
        home_intent.putExtra("groupid", groupId);
        home_intent.putExtra("groupname", groupName);
        startActivity(home_intent);
    }

    public void deleteFromDb(final String name, final String dir) {
        Snackbar.make(findViewById(android.R.id.content), "Are you sure you want to delete this item?", Snackbar.LENGTH_INDEFINITE)
                .setAction("Yes!", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseReference toDelete = mDatabase.child("data").child(groupId).child(dir).child(name);
                        toDelete.setValue(null);
                        returnHome();
                    }
                })
                .setActionTextColor(Color.WHITE)
                .show();

    }

    public void findOnMap(String name) {
        //new map activity
        Toast.makeText(this, "TODO", Toast.LENGTH_SHORT).show();
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
