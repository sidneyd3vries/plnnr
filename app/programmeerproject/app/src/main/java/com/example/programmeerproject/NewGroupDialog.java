package com.example.programmeerproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Plnnr
 * Sidney de Vries (10724087)
 *
 * Dialog to create a new group. Reached from GroupActivity.class
 */

public class NewGroupDialog extends Activity implements View.OnClickListener {

    int id = 2;

    private DatabaseReference mDatabase;
    private FirebaseUser user;

    ArrayList<String> uidList = new ArrayList<>();
    ArrayList<String> emailList = new ArrayList<>();

    EditText groupName;
    EditText ownEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_group_dialog);

        // Get Firebase instance, database and current user
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = mAuth.getCurrentUser();

        // Set listeners
        findViewById(R.id.create_group_button).setOnClickListener(this);
        findViewById(R.id.add_edit_text).setOnClickListener(this);

        // FInd views
        groupName = (EditText) findViewById(R.id.groupnameedittext);
        ownEmail = (EditText) findViewById(R.id.et1);

        // Set first editText with own email
        ownEmail.setText(user.getEmail());

        getUserIds();
    }

    public void returnHome() {
        /* Used to return to GroupActivity after group is created */
        Intent home_intent = new Intent(getApplicationContext(), GroupActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(home_intent);
    }

    @SuppressWarnings("ResourceType") // Without this setID won't work
    public void addEditText(Context context, LinearLayout view) {
        /* Adds new editText to dialog */
        EditText editText = new EditText(context);

        float width = getResources().getDimension(R.dimen.etsize);

        editText.setLayoutParams(new LinearLayout.LayoutParams((int) width,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        editText.setHint("Enter gmail");

        // Set view id to 2
        editText.setId(id);

        // Add 1 to id
        id++;

        view.addView(editText);

        // Get focus on new editText
        editText.requestFocus();
    }

    public String getUser(EditText et) {
        /* Returns email from single editText */
        if(!Objects.equals(et.getText().toString(), "")) {
            return et.getText().toString();
        }
        return null;
    }

    public ArrayList<String> getEditTextEmails() {
        /* Uses getUser() to make an arraylist of all emails filled in in editTexts */
        ArrayList<String> emails = new ArrayList<>();

        // Get text from static editText
        EditText staticEditText = (EditText) findViewById(R.id.et1);
        emails.add(getUser(staticEditText));

        // Loop over dynamic added editTexts and get their data
        for (int i = 2; i < id; i++) {
            //noinspection ResourceType
            EditText dynamicEditText = (EditText)findViewById(i);
            if (getUser(dynamicEditText) != null) {
                emails.add(getUser(dynamicEditText));
            }
        }
        // Return arraylist of all filled in emails
        return emails;
    }

    public void createGroup(ArrayList<String> uids, String name){
        /* Create a group with groupId the userid's of all members sticked together */
        String groupId = TextUtils.join("", uids);

        // Check if group with same id exists
        if (mDatabase.child("groups").child(groupId).child("name") != null) {
            groupId = groupId + "1";
        }

        // Set group Id
        mDatabase.child("groups").child(groupId).child("name").setValue(name);

        // Add all users apart as members
        for (int i = 0; i < uids.size() ; i++) {
            mDatabase.child("groups/" + groupId + "/").child(uids.get(i)).setValue(true);
        }
    }

    public void getUserIds() {
        /* Adds uids to uidlist and matching emails to emailList. ArrayLists
        are used in getUidFromEmail()
        */
        DatabaseReference userIds = mDatabase.child("userids");
        userIds.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Add keys and values to global ArrayLists
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    emailList.add(String.valueOf(child.getValue()));
                    uidList.add(child.getKey());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("NewGroupDialog", String.valueOf(databaseError));
            }
        });
    }

    public ArrayList<String> getUidFromEmail(ArrayList<String> emails) {
        /* Returns list of uid's matching the entered emails */
        ArrayList<String> uids = new ArrayList<>();

        // Returns null if entered email is not valid, the group wont be made
        // with this invalid email
        for (String s: emails) {
            if (emailList.contains(s)){
                int index = emailList.indexOf(s);
                uids.add(uidList.get(index));
            } else {
                Toast.makeText(this, "Invalid email: " + s, Toast.LENGTH_LONG).show();
                return null;
            }
        }
        return uids;
    }

    public String getGroupName() {
        /* Gets group name from editText, or return Defaut name if field is empty */
        if (!Objects.equals(groupName.getText().toString(), "")) {
            return groupName.getText().toString();
        } else {
            return "Default name";
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_group_button:
                String groupName = getGroupName();
                ArrayList<String> emails = getEditTextEmails();
                ArrayList<String> uids = getUidFromEmail(emails);
                // Create group if all entered emails are valid
                if (uids != null) {
                    createGroup(uids, groupName);
                    returnHome();
                }
                break;
            case R.id.add_edit_text:
                addEditText(this,(LinearLayout) findViewById(R.id.edittext_layout));
                break;
            default:
                break;
        }
    }
}
