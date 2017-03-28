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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;



import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {



    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    //Calendar instance
    private Calendar cal;

    //Fragments that constitute the tabs
    private Tab1Today tab1 = new Tab1Today();
    private Tab2Days tab2 = new Tab2Days();

    //If app is offline
    private static final String NOT_ONLINE = "No connection detected. Please check your Internet connection or wait until the servers are back online and restart the app";

    //Metric or Imperial. To change on Settings, but for now
    private static final String UNITS = "metric";

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
                uncleanedJsonCode = weatherAsyncTask.execute(latitude,longitude,UNITS).get();
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

    //JSON info gets sent and cleans the relevant info.
    //Also the pertinent methods get called
    public void jsonCleaner(String result) {

        ArrayList<String> table = new ArrayList<String>();
        cal = Calendar.getInstance();
        int dayOfMonth;

        try {


            JSONObject weatherJSONObj = new JSONObject(result);

            //String that will get the Name of the location.
            JSONObject cityInfo = weatherJSONObj.getJSONObject("city");
            String cityName = cityInfo.getString("name");

            //And the following process will get the info for all of the Following days.
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
                if(i!=0){
                    /**
                     * ADD THE DAY OF THE MONTH AS A STRING
                     */

                    table.add(mainTemperature + " - " + highAndLow + " - " + description);
                }


            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        tab2.populateList(table);


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

        //Creating the GoogleApiClient for the Locatization
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        //Granting permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Asking for permission to use the thing
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }


        //The Toolbar for whenever we decide to implement it
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // An Adapter returning a Fragment for each of the sections used
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


    }



    /*
     *
     *
     *
     * THIS IS THE SETTINGS BUTTON.
     * FUNCTIONALITY WILL COME LATER
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

     */

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return tab1;
                case 1:
                    return tab2;
            }
            return tab1;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
            }
            return null;
        }
    }


}
