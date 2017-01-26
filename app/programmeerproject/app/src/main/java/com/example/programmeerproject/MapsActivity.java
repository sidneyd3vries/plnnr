package com.example.programmeerproject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        AdapterView.OnItemSelectedListener,
        View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    private DatabaseReference mDatabase;
    private FirebaseUser user;

    public Double lng, lat;
    int rad;

    String apikey;
    String fromitem;
    String toitem;
    String fromquery;
    String toquery;
    String groupId;
    String groupName;
    String[]types = {"amusement_park", "aquarium",
            "art_gallery", "bar", "book_store",
            "bowling_alley", "cafe", "campground",
            "casino", "department_store", "movie_theater",
            "museum", "night_club", "park",
            "restaurant", "shopping_mall", "zoo"};

    Spinner fromspinner;
    Spinner tospinner;

    ArrayAdapter<String> fromadapter;
    ArrayAdapter<String> toadapter;

    ArrayList<String> receivedData = new ArrayList<>();
    ArrayList<String> keyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        // Get api key
        apikey = getString(R.string.google_api_key);

        // Set button listeners
        findViewById(R.id.gobutton).setOnClickListener(this);
        findViewById(R.id.addbutton).setOnClickListener(this);

        // Find and set up spinners
        fromspinner = (Spinner) findViewById(R.id.fromspinner);
        tospinner = (Spinner) findViewById(R.id.tospinner);
        initSpinner(fromspinner, types, fromadapter);
        initSpinner(tospinner, types, toadapter);

        // Get FireBase instance, database and current user
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = mAuth.getCurrentUser();

        // Remember groupId from previous activity
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupid");
        groupName = intent.getStringExtra("groupname");

        setUpGoogleApiClient();
        setDatabaseListener();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // TODO current location, not hardcoded
        lng = 4.895168;
        lat = 52.370216;
        rad = 1000;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    public void setUpGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
    }

    public void initSpinner(Spinner spinner, String[] stringArray, ArrayAdapter<String> aAdapter) {
        aAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stringArray);
        aAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(aAdapter);
        spinner.setOnItemSelectedListener(this);
    }

    public void prepareQuery() {
        // TODO use pagetoken to get more than 20 results
        fromquery = "https://maps.googleapis.com/maps/api/place/textsearch/json?location=" +
                lat + "," + lng + "&radius=" + rad +
                "&type=" + fromitem +
                "&key=" + apikey;

        toquery = "https://maps.googleapis.com/maps/api/place/textsearch/json?location=" +
                lat + "," + lng + "&radius=" + rad +
                "&type=" + toitem +
                "&key=" + apikey;
    }

    public String queryJson(String url) throws ExecutionException, InterruptedException {
        MyAsyncTask asyncTask = new MyAsyncTask();
        String result;

        result = asyncTask.execute(url).get();

        //Log.d("QUERYJSON", result);

        return result;

    }

    public JSONArray getResults(String json) throws JSONException {
        JSONObject input = new JSONObject(json);

        if (!Objects.equals(input.getString("status"), "OK")) {
            Toast.makeText(this, "No item found in category", Toast.LENGTH_SHORT).show();
        }

        return input.getJSONArray("results");

//        JSONArray results = input.getJSONArray("results");
//
//        return results;
    }

    public LatLng getCoordinates(JSONObject json) throws  JSONException {
        Double lat = json.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
        Double lng = json.getJSONObject("geometry").getJSONObject("location").getDouble("lng");

        return new LatLng(lat, lng);
    }

    public String getName(JSONObject json) throws JSONException {
        return json.getString("name");
    }

    public String getAddress(JSONObject json) throws JSONException {
        return json.getString("formatted_address");
    }

    public void addMarkers(JSONArray array, float color, String dir) {
        for(int n = 0; n < array.length(); n++) {
            try {
                JSONObject object = array.getJSONObject(n);
                Marker marker = mMap.addMarker(new MarkerOptions().position(getCoordinates(object))
                        .title(getName(object))
                        .icon(BitmapDescriptorFactory.defaultMarker(color)));
                object.put("direction", dir);
                marker.setTag(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String stripInput(String input) {
        /** Strips string from dots, hashtags, dollar signs
         * and square brackets. This prevents a FireBase error.
         */
        return input.replaceAll("[.#$\\[\\]]", "");
    }

    public void writeToGroupDb(String place, String dir, String groupId){
        //TODO check if item already in db
        mDatabase.child("data").child(groupId).child(dir).child(stripInput(place)).child("place").setValue("holder");
        Toast.makeText(this, "Added!", Toast.LENGTH_SHORT).show();
    }

    public void setDatabaseListener() {
        // Database reader
        DatabaseReference personalDb = mDatabase.child("data/").child(groupId);
        personalDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    keyList.clear();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        receivedData.add(String.valueOf(child.getValue()));
                        keyList.add(child.getKey());
                    }
                } else {
                    Log.d("dbListener", "no items yet");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("MainActivity", String.valueOf(databaseError));
            }
        });
    }

    public void showDetails(Marker marker) throws JSONException{
        JSONObject data = (JSONObject) marker.getTag();

        String name = getName(data);
        String address = getAddress(data);
        String direction = data.getString("direction");

        View layout = findViewById(R.id.detailspopup);

        TextView nameView = (TextView) findViewById(R.id.name);
        TextView addressView = (TextView) findViewById(R.id.address);
        TextView directionView = (TextView) findViewById(R.id.direction);

        nameView.setText(name);
        addressView.setText(address);
        directionView.setText(direction);

        layout.setVisibility(View.VISIBLE);
    }

    public void hideDetails() {
        View layout = findViewById(R.id.detailspopup);
        layout.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onMapClick(LatLng point) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(point)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

        lat = point.latitude;
        lng = point.longitude;

        hideDetails();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set up listener
        mMap.setOnMapClickListener(this);

        // Add a marker in Amsterdam and move the camera
        //TODO get location
        LatLng amsterdam = new LatLng(52.370216, 4.895168);
        mMap.addMarker(new MarkerOptions().position(amsterdam).title("Marker in Amsterdam"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(amsterdam, 14));

        // Marker click listener
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                try {
                    showDetails(marker);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.fromspinner:
                fromitem = parent.getItemAtPosition(position).toString();
                break;
            case R.id.tospinner:
                toitem = parent.getItemAtPosition(position).toString();
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gobutton:
                if (Objects.equals(fromitem, toitem)) {
                    Toast.makeText(this, "Same category", Toast.LENGTH_SHORT).show();
                } else {
                    mMap.clear();
                    prepareQuery();
                    try {
                        //TODO make this a method
                        String from = queryJson(fromquery);
                        String to = queryJson(toquery);

                        JSONArray fromArray = getResults(from);
                        JSONArray toArray = getResults(to);

                        addMarkers(fromArray, 270, "from");
                        addMarkers(toArray, 120, "to");
                    } catch (ExecutionException | InterruptedException | JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.addbutton:
                //TODO can still add same item
                TextView namev = (TextView) findViewById(R.id.name);
                TextView dirv = (TextView) findViewById(R.id.direction);
                writeToGroupDb((String) namev.getText(), (String) dirv.getText(), groupId);
        }
    }

    //    public void writeToDB(String place, int count, String dir){
//        setDatabaseListener();
//        // Write json to database
//        if (!keyList.contains(place)) {
//            mDatabase.child("users/" + user.getUid() + "/" + dir).child(place).setValue(count);
//            Toast.makeText(getApplicationContext(), "Added!", Toast.LENGTH_SHORT).show();
//        } else {
//            //If item already in database
//            Toast.makeText(getApplicationContext(), "Item already exists", Toast.LENGTH_SHORT).show();
//        }
//    }

    //    public void showTitle(String id) {
//        /* maybe redundant because of textsearch instead of radarsearch*/
//                Places.GeoDataApi.getPlaceById(mGoogleApiClient, id)
//                .setResultCallback(new ResultCallback<PlaceBuffer>() {
//                    @Override
//                    public void onResult(PlaceBuffer places) {
//                        if (places.getStatus().isSuccess() && places.getCount() > 0) {
//                            final Place myPlace = places.get(0);
//                            String title = String.valueOf(myPlace.getName());
//                            Toast.makeText(MapsActivity.this, title, Toast.LENGTH_SHORT).show();
//                            Log.d("FOUND PLACE", String.valueOf(myPlace.getName()));
//                        } else {
//                            Log.d("getTitle", "Place not found " + places.getStatus().toString());
//                        }
//                        places.release();
//                    }
//                });
//    }
}

