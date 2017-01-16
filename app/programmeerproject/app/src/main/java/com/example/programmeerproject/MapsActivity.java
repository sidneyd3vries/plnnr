package com.example.programmeerproject;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    String radarquery, textquery, json, query;
    Double lng, lat;
    int rad;
    String type, keyword, apikey;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        lng = 4.895168;
        lat = 52.370216;
        rad = 1000;
        apikey = getString(R.string.google_api_key);
        type = "museum";

        radarquery = "https://maps.googleapis.com/maps/api/place/radarsearch/json?location=" +
                     lat + "," + lng + "&radius=" + rad +
                     "&type=" + type +
                     //"&keyword=" + keyword +
                     "&key=" + apikey;


        textquery = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" + query + "&key=" + apikey;

        queryJson(radarquery);

    }

    public void queryJson(String url) {
        MyAsyncTask asyncTask = new MyAsyncTask();

        Log.d("URL", url);

        try {
            json = asyncTask.execute(url).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
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
        // TODO move marker to current/saved location
        LatLng amsterdam = new LatLng(52.370216, 4.895168);
        mMap.addMarker(new MarkerOptions().position(amsterdam).title("Marker in Amsterdam"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(amsterdam, 14));
    }


    @Override
    public void onMapClick(LatLng point) {
        Log.d("CLICKED:", String.valueOf(point));
    }
}

