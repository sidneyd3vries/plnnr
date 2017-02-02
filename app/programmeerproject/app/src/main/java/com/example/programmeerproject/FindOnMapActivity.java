package com.example.programmeerproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.RatingBar;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

/**
 * Plnnr
 * Sidney de Vries (10724087)
 *
 * Activity is reached from PinboardItemDialog.class.
 * Here the selected item is shown on a map. Map also
 * shows users current location if available.
 *
 * Source for location:
 * Source:https://www.androidtutorialpoint.com/intermediate/android-map-app-showing-current-location-android/
 */

public class FindOnMapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    LocationRequest mLocationRequest;
    Location mLastLocation;

    Marker mCurrLocationMarker;

    public Double lng;
    public Double lat;
    public int rad;

    String apikey;
    String placeName;

    MapsMethods mm = new MapsMethods();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_on_map_activity);

        // Get data from intent
        Intent intent = getIntent();
        placeName = intent.getStringExtra("name");

        setTitle(placeName);

        // Get api key
        apikey = getString(R.string.google_api_key);

        setUpGoogleApiClient();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.findonmap);
        mapFragment.getMapAsync(this);

        // Set search radius
        rad = 5000;
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

    @Override
    public void onConnected(Bundle connectionHint) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) { }

    public void setUpGoogleApiClient() {
        /* Builds google api client with correct api's */
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .enableAutoManage(this, this)
                .build();
    }

    public String prepareTextQuery(String name) {
        /* Returns correctly formatted query to be used in the api search */
        String formattedName = name.replaceAll("[^a-zA-Z0-9 ]", "").replaceAll(" ","+");
        return "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + formattedName + "&key=" + apikey;
    }

    public String queryJson(String url) throws ExecutionException, InterruptedException {
        /* Queries url from prepareTextQuery() and returns the resulting json */
        PlacesApiAsyncTask asyncTask = new PlacesApiAsyncTask();
        String result;
        result = asyncTask.execute(url).get();
        return result;
    }

    public void addMarker(String jsonString) throws JSONException {
        /* Adds marker of selected place on the map by getting data from queryJson() */
        // Convert string to JSONObject
        JSONObject json = new JSONObject(jsonString);

        // Get result from jsonArray
        JSONArray results = json.getJSONArray("results");
        JSONObject result = results.getJSONObject(0);

        // Get coordinates
        Double lat = result.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
        Double lng = result.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
        LatLng point = new LatLng(lat, lng);

        // Get name
        String name = result.getString("name");

        // Add marker of selected place
        mMap.addMarker(new MarkerOptions()
                .position(point)
                .title(name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));

        // Move camera to marker
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 14));
    }

    public void fillDetailView(String jsonString) throws JSONException {
        /* Fills detailView with data from a json string */
        JSONObject json = new JSONObject(jsonString);

        // Get result from jsonArray
        JSONArray results = json.getJSONArray("results");
        JSONObject result = results.getJSONObject(0);

        // Get data from result
        String name = result.getString("name");
        String address = result.getString("formatted_address");

        // Find and fill views
        TextView nameView = (TextView) findViewById(R.id.nameView);
        TextView addressView = (TextView) findViewById(R.id.addressView);
        RatingBar ratingView = (RatingBar) findViewById(R.id.ratingView);

        // Set color of rating stars to yellow
        Drawable progress = ratingView.getProgressDrawable();
        DrawableCompat.setTint(progress, Color.BLACK);

        nameView.setText(name);
        addressView.setText(address);

        // Convert double to int if needed
        if (result.get("rating") instanceof Double) {
            final Double rating = result.getDouble("rating");
            ratingView.setNumStars((int) Math.round(rating));
        } else {
            final int rating = result.getInt("rating");
            ratingView.setNumStars(rating);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Connection error", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Initialize Google Play Services
        mm.checkGooglePlayServices(mMap, this);

        // Set up listener
        mMap.setOnMapClickListener(this);

        if (lat != null || lng != null) {
            LatLng latLng = new LatLng(lat, lng);
            mMap.addMarker(new MarkerOptions().position(latLng).title("My location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
        }

        // Add marker of place and current location
        try {
            String json = queryJson(prepareTextQuery(placeName));
            addMarker(json);
            fillDetailView(json);
        } catch (ExecutionException | InterruptedException | JSONException e) {
            e.printStackTrace();
        }

        // Marker click listener
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Toast.makeText(FindOnMapActivity.this, marker.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    @Override
    public void onMapClick(LatLng latLng) { }

    @Override
    public void onLocationChanged(Location location) {
        /* Responsible for adding current location marker to map if current
         location is available
         */
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
