package com.example.programmeerproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Plnnr
 * Sidney de Vries (10724087)
 *
 * Dialog to change the name of a group.
 * Is reached by the menu in GroupActivity.class
 *
 */

public class ChangeNameDialog extends Activity implements View.OnClickListener {

    String groupName;
    String groupId;

    private DatabaseReference mDatabase;
    private FirebaseUser user;

    EditText nameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_name_dialog);

        // Get data from intent
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupid");
        groupName = intent.getStringExtra("groupname");

        // Get current user
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Find views
        findViewById(R.id.changenamebutton).setOnClickListener(this);
        nameEditText = (EditText) findViewById(R.id.changenameedittext);
    }

    public void changeGroupName() {
        /* Changes the group name from what is entered in the edit text */
        String newName = nameEditText.getText().toString();

        // If something is entered
        if (!newName.matches("")) {
            // Make correct database reference
            DatabaseReference group = mDatabase.child("groups").child(groupId);

            // Change the name
            group.child("name").setValue(newName);

            Toast.makeText(this, "Name changed", Toast.LENGTH_SHORT).show();

            // Send correct data back with intent
            Intent intent = new Intent(getApplicationContext(), PinboardTabActivity.class);
            intent.putExtra("groupid", groupId);
            intent.putExtra("groupname", newName);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Can not be empty", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.changenamebutton:
                changeGroupName();
                break;
            default:
                break;
        }
    }
}
