package com.example.programmeerproject;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.vision.text.Text;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        AdapterView.OnItemSelectedListener,
        View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener {

    String test = "{'geometry': {'location': {'lat': 52.373925, 'lng': 4.89635}}, 'place_id': 'ChIJ9do5m7gJxkcRHy7o8MDGAHI', 'id': '36d153440484164e20105c38a28b0c1b420de9b8', 'reference': 'CmRRAAAALfAgQCk5Om2IMeBXXwVCk3T09aldhUu-qhBpOdEkOCeefugHf6FkueixOjVbfV9rQvLfDc8VwlPJspBBzlkTvTQ4wKK6ovcvBIBp2NhPdEerpNuD2g5Fk23WUfyV6s1HEhDzesFzcJz6At6skVMblO71GhT7fwnPqtTiOKkZnD0ghsOdrRszgw'}" ;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    String radarquery, textquery, json, query;
    public Double lng, lat;
    int rad;
    String type, keyword, apikey, fromitem, toitem, fromquery, toquery, globaltitle;
    Spinner fromspinner, tospinner;
    String[]types = {"amusement_park", "aquarium", "art_gallery", "bar", "book_store", "bowling_alley", "cafe", "campground", "casino", "department_store", "movie_theater", "museum", "night_club", "park", "restaurant", "shopping_mall", "zoo"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        setUpSpinners();

        findViewById(R.id.gobutton).setOnClickListener(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        lng = 4.895168;
        lat = 52.370216;
        rad = 1000;
        apikey = getString(R.string.google_api_key);

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

    public void showTitle(String id) {
        /* maybe redundant because of textsearch instead of radarsearch*/
                Places.GeoDataApi.getPlaceById(mGoogleApiClient, id)
                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        if (places.getStatus().isSuccess() && places.getCount() > 0) {
                            final Place myPlace = places.get(0);
                            String title = String.valueOf(myPlace.getName());
                            Toast.makeText(MapsActivity.this, title, Toast.LENGTH_SHORT).show();
                            Log.d("FOUND PLACE", String.valueOf(myPlace.getName()));
                        } else {
                            Log.d("getTitle", "Place not found " + places.getStatus().toString());
                        }
                        places.release();
                    }
                });
    }

    public String queryJson(String url) throws ExecutionException, InterruptedException {
        MyAsyncTask asyncTask = new MyAsyncTask();
        String result;

        result = asyncTask.execute(url).get();

        Log.d("QUERYJSON", result);

        return result;

    }

    public JSONArray getResults(String json) throws JSONException {
        JSONObject input = new JSONObject(json);

        if (!Objects.equals(input.getString("status"), "OK")) {
            Toast.makeText(this, "No item found in category", Toast.LENGTH_SHORT).show();
        }

        JSONArray results = input.getJSONArray("results");

        return results;
    }

    public LatLng getCoordinates(JSONObject json) throws  JSONException {
        Double lat = json.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
        Double lng = json.getJSONObject("geometry").getJSONObject("location").getDouble("lng");

        LatLng coordinates = new LatLng(lat, lng);

        return coordinates;
    }

    public String getId(JSONObject json) throws JSONException {
        String id = json.getString("place_id");
        return id;
    }

    public String getRating(JSONObject json) throws JSONException {
        if (json.has("rating")) {
            Double rating = json.getDouble("rating");
            return String.valueOf(rating);
        } else {
            return "No ratings yet";
        }
    }

    public String getName(JSONObject json) throws JSONException {
        String name = json.getString("name");
        return name;
    }


    public String getAddress(JSONObject json) throws JSONException {
        String address = json.getString("formatted_address");
        return address;
    }


    public void addMarkers(JSONArray array, float color) {

        for(int n = 0; n < array.length(); n++)
        {
            try {
                JSONObject object = array.getJSONObject(n);

                Marker marker = mMap.addMarker(new MarkerOptions().position(getCoordinates(object))
                        .title(getName(object))
                        .icon(BitmapDescriptorFactory.defaultMarker(color)));

                marker.setTag(object);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

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

    public void showDetails(Marker marker) throws JSONException{
        JSONObject data = (JSONObject) marker.getTag();
        String name = getName(data);
        String address = getAddress(data);
        //String rating = getRating(data);

        View layout = findViewById(R.id.detailspopup);
        TextView namev = (TextView) findViewById(R.id.name);
        TextView addressv = (TextView) findViewById(R.id.address);
        //TextView ratingv = (TextView) findViewById(R.id.rating);

        namev.setText(name);
        addressv.setText(address);
        //ratingv.setText(rating);

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

    public void setUpSpinners() {
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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.gobutton:
                if (Objects.equals(fromitem, toitem)) {
                    Toast.makeText(this, "Same category", Toast.LENGTH_SHORT).show();
                } else {
                    prepareQuery();
                    try {
                        String from = queryJson(fromquery);
                        String to = queryJson(toquery);

                        JSONArray fromArray = getResults(from);
                        JSONArray toArray = getResults(to);

                        addMarkers(fromArray, 270);
                        addMarkers(toArray, 120);
                    } catch (ExecutionException | InterruptedException | JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }
}

