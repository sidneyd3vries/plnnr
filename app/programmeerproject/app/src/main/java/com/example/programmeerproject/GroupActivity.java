package com.example.programmeerproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class GroupActivity extends AppCompatActivity {

    ListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.group_activity);

        listView = (ListView) findViewById(R.id.list);

        // Static data, to be replaced
        String[] values = new String[] { "Group 1",
                "Group 2",
                "Group 3"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // Static intent, to be replaced
                Intent intent = new Intent(getApplicationContext(), PinboardActivity.class);
                startActivity(intent);

            }

        });
    }
}
