package com.elmexicano.lsteamer.sunshinetoday;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    //If app is offline
    private static final String NOT_ONLINE = "No connection detected. Please check your Internet connection or wait until the servers are back online and restart the app";

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private DownloadTask weatherAsyncTask = null;


    //Array List that will hold the weather forecast.
    private ArrayList<String> weatherForecast = new ArrayList<String>();


    private LocationManager locationManager;
    private String latitude, longitude;

    @Override
    public void onConnected(Bundle connectionHint) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Asking for permission to use the thing
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }


        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            latitude = Double.toString(mLastLocation.getLatitude());
            longitude = Double.toString(mLastLocation.getLongitude());
        }

        if(weatherAsyncTask==null){
            weatherAsyncTask = new DownloadTask();


            //String to receive the code
            String uncleanedJsonCode="";
            try {
                uncleanedJsonCode = weatherAsyncTask.execute(latitude,longitude).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            if(uncleanedJsonCode!="ERROR"){


                jsonCleaner(uncleanedJsonCode);

            }
            else{
                Toast.makeText(this, NOT_ONLINE,
                        Toast.LENGTH_LONG).show();
            }

        }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Toast.makeText(this, NOT_ONLINE,
                Toast.LENGTH_LONG).show();

        /*
            Fill with placeholder info.
         */

    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    //JSON info gets sent and cleans the relevant info
    public void jsonCleaner(String result) {

        try {


            JSONObject weatherJSONObj = new JSONObject(result);
            JSONArray weatherJSONArray = weatherJSONObj.getJSONArray("list");





            for (int i = 0; i < weatherJSONArray.length(); i++) {

                String mainTemperature;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherJSONArray.getJSONObject(i);


                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray("weather").getJSONObject(0);
                mainTemperature = weatherObject.getString("main");
                description = weatherObject.getString("description");

                JSONObject temperatureObject = dayForecast.getJSONObject("temp");

                double high = temperatureObject.getDouble("max");
                double low = temperatureObject.getDouble("min");

                highAndLow = formatHighLows(high, low);

                Log.i("Day Forecast", mainTemperature + " - " + highAndLow + " - " + description);


            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    //Gives you an average for the day temperature
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user only cares about full numbers
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Asking for permission to use the thing
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }


}
