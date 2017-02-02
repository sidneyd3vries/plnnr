package com.example.programmeerproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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

/**
 * Plnnr
 * Sidney de Vries (10724087)
 *
 * Activity where Google places api is queried and results are written to
 * group database. Shows current location is user gave permission.
 *
 * Source for location:
 * Source:https://www.androidtutorialpoint.com/intermediate/android-map-app-showing-current-location-android/
 */

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

    String[] types = {"Amusement park", "Aquarium",
            "Art gallery", "Bar", "Book store",
            "Bowling alley", "Cafe", "Campground",
            "Casino", "Department_store", "Movie theater",
            "Museum", "Night club", "Park",
            "Restaurant", "Shopping mall", "Zoo"};

    Spinner fromspinner;
    Spinner tospinner;

    EditText fromedittext;
    EditText toedittext;

    String fromtext;
    String totext;

    ArrayAdapter<String> fromadapter;
    ArrayAdapter<String> toadapter;

    LocationRequest mLocationRequest;
    Location mLastLocation;

    Marker mCurrLocationMarker;

    MapsMethods mm = new MapsMethods();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        // Set up floating action button
        setUpFab();

        // Set title in toolbar
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
        initSpinner(fromspinner, types);
        initSpinner(tospinner, types);

        // Get FireBase instance, database and current user
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = mAuth.getCurrentUser();

        // Remember groupId from previous activity
        Intent intent = getIntent();
        groupId = intent.getStringExtra("groupid");
        groupName = intent.getStringExtra("groupname");

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
        /* Build google api client with correct api's */
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .enableAutoManage(this, this)
                .build();
    }

    public void setUpFab() {
        /* Set up floating action button with onClick action
        to query the api and show results on map
        */
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.mapSearch);
        fab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Get text from edittextx
                fromtext = fromedittext.getText().toString();
                totext = toedittext.getText().toString();

                // If user marked no location, tell them
                if (lat == null || lng == null) {
                    Toast.makeText(getApplicationContext(), "Need location", Toast.LENGTH_SHORT).show();
                } else {
                    // If spinners have same category and editTexts are empty, tell user
                    // spinners cant be same category
                    if (Objects.equals(fromitem, toitem) && Objects.equals(fromtext, "") && Objects.equals(totext, "")) {
                        Toast.makeText(getApplicationContext(), "Same category", Toast.LENGTH_SHORT).show();
                    } else {
                        // Here input is valid, so clear previous results from map
                        mMap.clear();
                        // If editTexts are empty, query both spinners
                        if (Objects.equals(fromtext, "") && Objects.equals(totext, "")) {
                            prepareQuery(false, false);
                        // Else if only one editText is filled in, query that and the other spinner
                        } else if (Objects.equals(fromtext, "") && !Objects.equals(totext, "")) {
                            prepareQuery(false, true);
                        } else if (!Objects.equals(fromtext, "") && Objects.equals(totext, "")) {
                            prepareQuery(true, false);
                        // Else query both editTexts
                        } else {
                            prepareQuery(true, true);
                        }
                        try {
                            // Set jsonStrings
                            String from = queryJson(fromquery);
                            String to = queryJson(toquery);

                            // Put query results in JSONArray
                            JSONArray fromArray = getResults(from);
                            JSONArray toArray = getResults(to);

                            // Add markers of found places
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
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) { }

    public void initSpinner(Spinner spinner, String[] stringArray) {
        /* Set up spinners and onItemSelectedListener */
        ArrayAdapter<String> aAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, stringArray);
        aAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(aAdapter);
        spinner.setOnItemSelectedListener(this);
    }

    public String prepareTextQuery(String name) {
        /* Format text query so the api will accept it */
        String formattedName = name.replaceAll("[^a-zA-Z0-9 ]", "").replaceAll(" ","+");
        return "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" +
                formattedName +
                "&location=" + lat + "," + lng +
                "&key=" + apikey;
    }

    public void prepareQuery(boolean fromtext, boolean totext) {
        /* Set from and to queries matching with filled in editTexts */
        // TODO use pagetoken to get more than 20 results
        // If editText is filled in, use data from there
        // Else use data from spinner
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
        /* Uses asynctask to query the api and returns the resulting json */
        PlacesApiAsyncTask asyncTask = new PlacesApiAsyncTask();
        String result;

        result = asyncTask.execute(url).get();
        return result;
    }

    public JSONArray getResults(String json) throws JSONException {
        /* Get all api results and put them in a JSONArray */
        JSONObject input = new JSONObject(json);

        // If status is not ok, return empty JSONArray to prevent nullpointers
        if (!Objects.equals(input.getString("status"), "OK")) {
            Toast.makeText(this, "No item found in category", Toast.LENGTH_SHORT).show();
            return new JSONArray();
        } else {
            return input.getJSONArray("results");
        }
    }

    public LatLng getCoordinates(JSONObject json) throws  JSONException {
        /* Get coordinates from JSONObject */
        Double lat = json.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
        Double lng = json.getJSONObject("geometry").getJSONObject("location").getDouble("lng");

        return new LatLng(lat, lng);
    }

    public String getName(JSONObject json) throws JSONException {
        /* Gets name from JSONObject */
        return json.getString("name");
    }

    public String getAddress(JSONObject json) throws JSONException {
        /* Gets address from JSONObject */
        return json.getString("formatted_address");
    }

    public void addMarkers(JSONArray array, float color, String dir) {
        /* Adds markers for every place found by the api */
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
        /* Strips string from dots, hashtags, dollar signs
         and square brackets. This prevents a FireBase error.
         */
        return input.replaceAll("[.#$\\[\\]]", "").replaceAll("/", " ");
    }

    public void writeToGroupDb(final String place, final String dir, final String groupId){
        /* Adds found item to group database */
        DatabaseReference toWrite = mDatabase.child("data").child(groupId).child(dir);
        toWrite.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.hasChild(stripInput(place))) {
                    // If items doesn't exist in database, add it
                    mDatabase.child("data").child(groupId).child(dir).child(stripInput(place)).child("place").setValue("holder");
                    Toast.makeText(getApplicationContext(), "Added", Toast.LENGTH_SHORT).show();
                } else {
                    // Item is already in database, so tell the user
                    Toast.makeText(getApplicationContext(), "Item already exists", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MapsActivity.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void showDetails(Marker marker) throws JSONException{
        /* Show detailview that displays information of selected place */
        JSONObject data = (JSONObject) marker.getTag();

        // Get data from JSONObject
        String name = getName(data);
        String address = getAddress(data);
        String direction = data.getString("direction");

        View layout = findViewById(R.id.detailspopup);

        // Find views
        TextView nameView = (TextView) findViewById(R.id.name);
        TextView addressView = (TextView) findViewById(R.id.address);
        TextView directionView = (TextView) findViewById(R.id.direction);

        // Add data to views
        nameView.setText(name);
        addressView.setText(address);
        directionView.setText(direction);

        // Show invisible layout
        layout.setVisibility(View.VISIBLE);
    }

    public void hideDetails() {
        /* Hides detailview */
        View layout = findViewById(R.id.detailspopup);
        layout.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onMapClick(LatLng point) {
        /* Map click listener, used to place own marker */
        mMap.clear();

        // Add marker on clicked point
        mMap.addMarker(new MarkerOptions().position(point)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
                .setTag(true);

        // Set global lat and lng
        lat = point.latitude;
        lng = point.longitude;

        // Hide detail view
        hideDetails();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        setUpGoogleApiClient();

        //Initialize Google Play Services
        mm.checkGooglePlayServices(mMap, this);

        // Set up listener
        mMap.setOnMapClickListener(this);

        // Set marker on current location
        if (lat != null || lng != null) {
            LatLng latLng = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions().position(latLng));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
        // If location is not available, show the whole country
        } else {
            LatLng netherlands = new LatLng(52.370216, 4.895168);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(netherlands, 8));
        }

        // Marker click listener
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // If marker is not own placed marker, show details
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
    public void onNothingSelected(AdapterView<?> parent) { }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        /* OnItemSelected of spinners */
        switch (parent.getId()) {
            case R.id.fromspinner:
                fromitem = parent.getItemAtPosition(position).toString().replaceAll(" ", "_").toLowerCase();
                break;
            case R.id.tospinner:
                toitem = parent.getItemAtPosition(position).toString().replaceAll(" ", "_").toLowerCase();
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
        /* Creates overflow menu in toolbar */
        getMenuInflater().inflate(R.menu.maps_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /* OnItemSelected of overflow menu */
        switch (item.getItemId()) {
            case R.id.mapshelp:
                showHelp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showHelp() {
        /* Show a small help snackbar to user */
        final Snackbar snackBar = Snackbar.make(findViewById(R.id.mapscontainer),
                "Select a location by clicking the map. Press go to search your categories.",
                Snackbar.LENGTH_INDEFINITE);

        snackBar.setAction("Got it!", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackBar.dismiss();
            }
        })
        .setActionTextColor(Color.WHITE);
        snackBar.show();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        mm.getLocation(mMap, location);

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }
}

