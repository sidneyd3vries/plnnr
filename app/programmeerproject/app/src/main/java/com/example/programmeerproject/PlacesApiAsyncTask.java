package com.example.programmeerproject;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Plnnr
 * Sidney de Vries (10724087)
 *
 * AsyncTask used to query the Google Places API
 */


public class PlacesApiAsyncTask extends AsyncTask<String,String,String> {

    URL url;
    HttpURLConnection urlConnection;
    InputStream inputStream;
    String finalString;

    @Override
    protected String doInBackground(String... params) {
        try {
            url = new URL(params[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            inputStream = urlConnection.getInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }
            finalString = total.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return finalString;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }
}

