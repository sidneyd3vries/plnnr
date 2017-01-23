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

    int id = 2;

    private DatabaseReference mDatabase;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_group_dialog);
        setTitle("Add friends");

        // Get Firebase instance, database and current user
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = mAuth.getCurrentUser();

        findViewById(R.id.create_group_button).setOnClickListener(this);
        findViewById(R.id.add_edit_text).setOnClickListener(this);
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

    public ArrayList<String> getEditTextData() {
        ArrayList<String> names = new ArrayList<>();

        EditText staticEditText = (EditText) findViewById(R.id.et1);
        names.add(getUser(staticEditText));

        for (int i = 2; i < id; i++) {
            //noinspection ResourceType
            EditText dynamicEditText = (EditText)findViewById(i);
            names.add(getUser(dynamicEditText));
        }
        return names;
    }

    public String joinArrayList(ArrayList<String> arraylist) {
        return TextUtils.join("", arraylist).replaceAll("[.#$\\[\\]]","");
    }

    public void updateGroups(ArrayList<String> users){
        // Write json to database
        String groupId = joinArrayList(users);
        Log.d("TEST", groupId);
        for (int i = 0; i < users.size() ; i++) {
            mDatabase.child("groups/" + groupId + "/").child(users.get(i).replaceAll("[.#$\\[\\]]","")).setValue(true);
        }
    }

    public void updateUsers(ArrayList<String> users) {
        String groupId = joinArrayList(users);
        Log.d("TEST", groupId);
        mDatabase.child("users/" + user.getEmail().replaceAll("[.#$\\[\\]]","") + "/" + groupId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_group_button:
                updateGroups(getEditTextData());
                updateUsers(getEditTextData());
                returnHome();
                break;
            case R.id.add_edit_text:
                addEditText(this,(LinearLayout) findViewById(R.id.edittext_layout));
                break;
        }
    }
}
