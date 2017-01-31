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
import android.util.Log;
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

public class FindOnMapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;

    public Double lng;
    public Double lat;
    public int rad;

    String apikey;
    String placeName;

    LocationRequest mLocationRequest;
    Location mLastLocation;

    Marker mCurrLocationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_on_map_activity);

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

        rad = 3000;
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
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .enableAutoManage(this, this)
                .build();
    }

    public String prepareTextQuery(String name) {
        String formattedName = name.replaceAll("[^a-zA-Z0-9 ]", "").replaceAll(" ","+");
        return "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + formattedName + "&key=" + apikey;
    }

    public String queryJson(String url) throws ExecutionException, InterruptedException {
        MyAsyncTask asyncTask = new MyAsyncTask();
        String result;
        result = asyncTask.execute(url).get();
        Log.d("RESULT", result);
        return result;
    }

    public void addMarker(String jsonString) throws JSONException {
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 14));
    }

    public void fillDetailView(String jsonString) throws JSONException {
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
