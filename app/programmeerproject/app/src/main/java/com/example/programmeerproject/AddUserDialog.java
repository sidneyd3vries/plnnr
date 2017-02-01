package com.example.programmeerproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

/**
 * Plnnr
 * Sidney de Vries (10724087)
 *
 * Dialog to add user to an existing group.
 * Is reached by the menu in GroupActivity.class
 *
 */

public class AddUserDialog extends Activity implements View.OnClickListener{

    EditText userEmail;
    String groupId;
    String groupName;

    private DatabaseReference mDatabase;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_user_dialog);

        // Get data from intent
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupid");
        groupName = intent.getStringExtra("groupname");

        // Get current user
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Find views
        findViewById(R.id.adduserbutton).setOnClickListener(this);
        userEmail = (EditText) findViewById(R.id.adduseredittext);
    }

    public void addUser() {
        /* Adds valid user to existing group */
        final String email = userEmail.getText().toString();
        // Make database reference
        DatabaseReference users = mDatabase.child("userids");
        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userid = null;
                for (DataSnapshot user: dataSnapshot.getChildren()) {
                    // If value of user id matches email, get the userid
                    if (Objects.equals(user.getValue().toString(), email)) {
                        userid = user.getKey();
                    }
                }
                // Add user to group if user is not null
                if (userid != null) {
                    mDatabase.child("groups").child(groupId).child(userid).setValue(true);
                    Toast.makeText(AddUserDialog.this, "Added to " + groupName, Toast.LENGTH_SHORT).show();
                    // Make and start intent
                    Intent intent = new Intent(getApplicationContext(), PinboardTabActivity.class);
                    intent.putExtra("groupid", groupId);
                    intent.putExtra("groupname", groupName);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(AddUserDialog.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AddUserDialog.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.adduserbutton:
                addUser();
                break;
            default:
                break;
        }
    }
}
