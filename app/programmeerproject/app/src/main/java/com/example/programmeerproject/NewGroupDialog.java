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


public class NewGroupDialog extends Activity implements View.OnClickListener {

    //TODO user input validation

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
        //setTitle("Create group");

        // Get Firebase instance, database and current user
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = mAuth.getCurrentUser();

        findViewById(R.id.create_group_button).setOnClickListener(this);
        findViewById(R.id.add_edit_text).setOnClickListener(this);


        groupName = (EditText) findViewById(R.id.groupnameedittext);
        ownEmail = (EditText) findViewById(R.id.et1);

        ownEmail.setText(user.getEmail());

        getUserIds();
    }

    public void returnHome() {
        Intent home_intent = new Intent(getApplicationContext(), GroupActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(home_intent);
    }

    @SuppressWarnings("ResourceType") // Without this setID won't work
    public void addEditText(Context context, LinearLayout view) {
        EditText editText = new EditText(context);

        float width = getResources().getDimension(R.dimen.etsize);

        editText.setLayoutParams(new LinearLayout.LayoutParams((int) width,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        editText.setHint("Enter gmail");
        editText.setId(id);

        id++;

        view.addView(editText);

        editText.requestFocus();
    }

    public String getUser(EditText et) {
        if(!Objects.equals(et.getText().toString(), "")) {
            return et.getText().toString();
        } else {
            return "";
        }
    }

    public ArrayList<String> getEditTextEmails() {
        ArrayList<String> emails = new ArrayList<>();

        EditText staticEditText = (EditText) findViewById(R.id.et1);
        emails.add(getUser(staticEditText));

        for (int i = 2; i < id; i++) {
            //noinspection ResourceType
            EditText dynamicEditText = (EditText)findViewById(i);
            emails.add(getUser(dynamicEditText));
        }
        return emails;
    }

    public void createGroup(ArrayList<String> uids, String name){
        String groupId = TextUtils.join("", uids);
        mDatabase.child("groups/" + groupId + "/name").setValue(name);
        for (int i = 0; i < uids.size() ; i++) {
            mDatabase.child("groups/" + groupId + "/").child(uids.get(i)).setValue(true);
        }
    }

    public void getUserIds() {

        // Database reader
        DatabaseReference userIds = mDatabase.child("userids");
        userIds.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

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
        // Returns list of uid's from entered emails
        ArrayList<String> uids = new ArrayList<>();

        for (String s: emails) {
            if (emailList.contains(s)){
                int index = emailList.indexOf(s);
                uids.add(uidList.get(index));
            } else {
                Toast.makeText(this, "Invalid email: " + s, Toast.LENGTH_SHORT).show();
            }
        }
        return uids;
    }

    public String getGroupName() {
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
                // Clean this code up
                createGroup(getUidFromEmail(getEditTextEmails()), getGroupName());
                returnHome();
                break;
            case R.id.add_edit_text:
                addEditText(this,(LinearLayout) findViewById(R.id.edittext_layout));
                break;
        }
    }
}
