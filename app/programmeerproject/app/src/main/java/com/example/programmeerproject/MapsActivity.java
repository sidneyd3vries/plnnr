package com.example.programmeerproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
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

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        AdapterView.OnItemSelectedListener,
        View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    private DatabaseReference mDatabase;
    private FirebaseUser user;

    public Double lng;
    public Double lat;
    static int RAD = 10000;

    String apikey;
    String fromitem;
    String toitem;
    String fromquery;
    String toquery;
    String groupId;
    String groupName;

    String[] types = {"amusement_park", "aquarium",
            "art_gallery", "bar", "book_store",
            "bowling_alley", "cafe", "campground",
            "casino", "department_store", "movie_theater",
            "museum", "night_club", "park",
            "restaurant", "shopping_mall", "zoo"};

    Spinner fromspinner;
    Spinner tospinner;

    EditText fromedittext;
    EditText toedittext;

    String fromtext;
    String totext;

    ArrayAdapter<String> fromadapter;
    ArrayAdapter<String> toadapter;

    ArrayList<String> receivedData = new ArrayList<>();
    ArrayList<String> keyList = new ArrayList<>();

    LocationRequest mLocationRequest;
    Location mLastLocation;

    Marker mCurrLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        setUpFab();

        setTitle("Find places");

        // Get api key
        apikey = getString(R.string.google_api_key);

        // Set button listener
        findViewById(R.id.addbutton).setOnClickListener(this);

        fromedittext = (EditText) findViewById(R.id.fromedittext);
        toedittext = (EditText) findViewById(R.id.toedittext);

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

        setDatabaseListener();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.stopAutoManage(this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.stopAutoManage(this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.stopAutoManage(this);
        mGoogleApiClient.disconnect();
    }

    public void setUpGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .enableAutoManage(this, this)
                .build();
    }

    public void setUpFab() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.mapSearch);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mMap.clear();
                fromtext = fromedittext.getText().toString();
                totext = toedittext.getText().toString();

                if (lat == null || lng == null) {
                    Toast.makeText(getApplicationContext(), "Need location", Toast.LENGTH_SHORT).show();
                } else {
                    if (Objects.equals(fromitem, toitem) && Objects.equals(fromtext, "") && Objects.equals(totext, "")) {
                        Toast.makeText(getApplicationContext(), "Same category", Toast.LENGTH_SHORT).show();
                    } else {
                        mMap.clear();
                        if (Objects.equals(fromtext, "") && Objects.equals(totext, "")) {
                            prepareQuery(false, false);
                        } else if (Objects.equals(fromtext, "") && !Objects.equals(totext, "")) {
                            prepareQuery(false, true);
                        } else if (!Objects.equals(fromtext, "") && Objects.equals(totext, "")) {
                            prepareQuery(true, false);
                        } else {
                            prepareQuery(true, true);
                        }
                        try {
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
                }
            }
        });
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) { }

    public void initSpinner(Spinner spinner, String[] stringArray, ArrayAdapter<String> aAdapter) {
        aAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stringArray);
        aAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(aAdapter);
        spinner.setOnItemSelectedListener(this);
    }

    public String prepareTextQuery(String name) {
        String formattedName = name.replaceAll("[^a-zA-Z0-9 ]", "").replaceAll(" ","+");
        return "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" +
                formattedName +
                "&location=" + lat + "," + lng +
                "&key=" + apikey;
    }

    public void prepareQuery(boolean fromtext, boolean totext) {
        // TODO use pagetoken to get more than 20 results
        if (fromtext) {
            fromquery = prepareTextQuery(fromedittext.getText().toString());
        } else {
            fromquery = "https://maps.googleapis.com/maps/api/place/textsearch/json?location=" +
                    lat + "," + lng + "&radius=" + RAD +
                    "&type=" + fromitem +
                    "&key=" + apikey;
        }

        if (totext) {
            toquery = prepareTextQuery(toedittext.getText().toString());
        } else {
            toquery = "https://maps.googleapis.com/maps/api/place/textsearch/json?location=" +
                    lat + "," + lng + "&radius=" + RAD +
                    "&type=" + toitem +
                    "&key=" + apikey;
        }
    }

    public String queryJson(String url) throws ExecutionException, InterruptedException {
        MyAsyncTask asyncTask = new MyAsyncTask();
        String result;

        result = asyncTask.execute(url).get();
        return result;
    }

    public JSONArray getResults(String json) throws JSONException {
        JSONObject input = new JSONObject(json);

        if (!Objects.equals(input.getString("status"), "OK")) {
            Toast.makeText(this, "No item found in category", Toast.LENGTH_SHORT).show();
            return new JSONArray();
        } else {
            return input.getJSONArray("results");
        }
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

    public void writeToGroupDb(final String place, final String dir, final String groupId){
        DatabaseReference toWrite = mDatabase.child("data").child(groupId).child(dir);
        toWrite.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.hasChild(stripInput(place))) {
                    mDatabase.child("data").child(groupId).child(dir).child(stripInput(place)).child("place").setValue("holder");
                    Toast.makeText(getApplicationContext(), "Added", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Item already exists", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MapsActivity.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });

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
        boolean ownmarker = true;
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(point)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
                .setTag(ownmarker);

        lat = point.latitude;
        lng = point.longitude;

        hideDetails();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        setUpGoogleApiClient();

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            mMap.setMyLocationEnabled(true);
        }

        // Set up listener
        mMap.setOnMapClickListener(this);

        if (lat != null || lng != null) {
            LatLng latLng = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions().position(latLng));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
        } else {
            LatLng amsterdam = new LatLng(52.370216, 4.895168);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(amsterdam, 7));
        }

        // Marker click listener
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.d("MARKER", String.valueOf(marker.getTag()));
                if (Objects.equals(marker.getTag(), true)) {
                    Toast.makeText(MapsActivity.this, "Own marker", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        showDetails(marker);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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
            case R.id.addbutton:
                TextView namev = (TextView) findViewById(R.id.name);
                TextView dirv = (TextView) findViewById(R.id.direction);
                writeToGroupDb((String) namev.getText(), (String) dirv.getText(), groupId);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.maps_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mapshelp:
                showHelp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showHelp() {
        final Snackbar snackBar = Snackbar.make(findViewById(R.id.mapscontainer),
                "Select a location by clicking the map. Press go to search your categories",
                Snackbar.LENGTH_INDEFINITE);

        snackBar.setAction("Got it", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackBar.dismiss();
            }
        });
        snackBar.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.addMarker(new MarkerOptions().position(latLng).title("Current position"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));

        lat = location.getLatitude();
        lng = location.getLongitude();

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }
}

