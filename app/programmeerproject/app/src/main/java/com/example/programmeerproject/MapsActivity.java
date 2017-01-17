package com.example.programmeerproject;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, AdapterView.OnItemSelectedListener, View.OnClickListener {

    private GoogleMap mMap;
    String radarquery, textquery, json, query;
    public Double lng, lat;
    int rad;
    String type, keyword, apikey, fromitem, toitem, fromquery, toquery;
    Spinner fromspinner, tospinner;
    String[]types = {"amusement_park", "aquarium", "art_gallery", "bar", "book_store", "bowling_alley", "cafe", "campground", "casino", "department_store", "movie_theater", "museum", "night_club", "park", "restaurant", "shopping_mall", "zoo"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        findViewById(R.id.gobutton).setOnClickListener(this);

        fromspinner = (Spinner)findViewById(R.id.fromspinner);
        ArrayAdapter<String>fromadapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, types);
        fromadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromspinner.setAdapter(fromadapter);
        fromspinner.setOnItemSelectedListener(this);

        tospinner = (Spinner)findViewById(R.id.tospinner);
        ArrayAdapter<String>toadapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, types);
        toadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tospinner.setAdapter(toadapter);
        tospinner.setOnItemSelectedListener(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        lng = 4.895168;
        lat = 52.370216;
        rad = 1000;
        apikey = getString(R.string.google_api_key);

        //textquery = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + query + "&key=" + apikey;
        //queryJson(radarquery);

    }

    public void queryJson(String url) {
        MyAsyncTask asyncTask = new MyAsyncTask();

        Log.d("URL", url);

        try {
            json = asyncTask.execute(url).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        Log.d("JSON", json);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Set up listener
        mMap.setOnMapClickListener(this);

        // Add a marker in Amsterdam and move the camera
        LatLng amsterdam = new LatLng(52.370216, 4.895168);
        mMap.addMarker(new MarkerOptions().position(amsterdam).title("Marker in Amsterdam"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(amsterdam, 14));
    }


    @Override
    public void onMapClick(LatLng point) {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(point)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

        Log.d("CLICKED:", String.valueOf(point));

        lat = point.latitude;
        lng = point.longitude;
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
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gobutton:
                prepareQuery();
                break;
        }
    }

    public void prepareQuery() {
        Log.d("LAT", String.valueOf(lat));
        Log.d("LON", String.valueOf(lng));
        Log.d("FROM", fromitem);
        Log.d("TO", toitem);

        fromquery = "https://maps.googleapis.com/maps/api/place/radarsearch/json?location=" +
                lat + "," + lng + "&radius=" + rad +
                "&type=" + fromitem +
                "&key=" + apikey;

        toquery = "https://maps.googleapis.com/maps/api/place/radarsearch/json?location=" +
                lat + "," + lng + "&radius=" + rad +
                "&type=" + toitem +
                "&key=" + apikey;


        Log.d("FROMQ", fromquery);
        Log.d("TOQ", toquery);


    }
}

