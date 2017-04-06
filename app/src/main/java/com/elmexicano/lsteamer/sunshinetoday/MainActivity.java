package com.elmexicano.lsteamer.sunshinetoday;


import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.preference.PreferenceManager;
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

import java.text.SimpleDateFormat;
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
    private static final String NOT_ONLINE = "No connection detected or incorrect values entered. Please check them again and reload the app.";

    //Variables that might be changed in Settings later on
    private String units;
    private String days;
    private String latitude="";
    private String longitude="";
    private String postalCode;
    private boolean locationCurrent;
    private boolean notificationActive;
    private String notificationTime="1";


    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private DownloadTask weatherAsyncTask = null;


    //Array List that will hold the weather forecast.
    private ArrayList<String> weatherForecast = new ArrayList<String>();


    private LocationManager locationManager;

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
            String[] uncleanedJsonCode= new String[2];
            try {
                //if we don't have a new Postal Code to try out
                if(postalCode==null)
                    uncleanedJsonCode = weatherAsyncTask.execute(latitude,longitude,units,days).get();
                else
                    uncleanedJsonCode = weatherAsyncTask.execute(postalCode,units,days).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            if(uncleanedJsonCode!=null){

                Log.i("FULL first",uncleanedJsonCode[0]);


                String[] weatherInfo = jsonCleaner(uncleanedJsonCode);

                tab1.populateScreen(weatherInfo);
                setNotification(latitude,longitude,notificationActive,units);

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
    public String[] jsonCleaner(String[] result) {

        ArrayList<String> table = new ArrayList<String>();


        cal = Calendar.getInstance();
        SimpleDateFormat dayDateStack = new SimpleDateFormat("EEE, MMM d");

        String[] weatherInfoStrings = new String[9];
        try {


            JSONObject weatherJSONObj = new JSONObject(result[1]);

            //String that will get the Name of the location.
            JSONObject cityInfo = weatherJSONObj.getJSONObject("city");
            weatherInfoStrings[6] = cityInfo.getString("name");

            //And the following process will get the info for all of the Following days.
            JSONArray weatherJSONArray = weatherJSONObj.getJSONArray("list");

            for (int i = 0; i < weatherJSONArray.length(); i++) {

                String mainTemperature;
                String description;
                String highAndLow;
                String imageId;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherJSONArray.getJSONObject(i);


                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray("weather").getJSONObject(0);
                mainTemperature = weatherObject.getString("main");
                description = weatherObject.getString("description");
                imageId = weatherObject.getString("id");
                description = description.substring(0,1).toUpperCase() + description.substring(1);

                JSONObject temperatureObject = dayForecast.getJSONObject("temp");

                double high = temperatureObject.getDouble("max");
                double low = temperatureObject.getDouble("min");

                highAndLow = formatHighLows(high, low);

                if(i==0){
                    //weatherInfoStrings [1] = Long.toString(Math.round((high+low)/2)) + "\u00B0";
                    weatherInfoStrings [2] = mainTemperature;
                    weatherInfoStrings [3] = description;
                    weatherInfoStrings [4] = highAndLow;
                    weatherInfoStrings [5] = imageId;
                    weatherInfoStrings [7] = Long.toString(Math.round((high)));
                    weatherInfoStrings [8] = Long.toString(Math.round((low)));
                }
                else{

                    cal = Calendar.getInstance();
                    //Getting the calendar instance and then getting
                    cal.add(Calendar.DAY_OF_YEAR, i);

                    //table.add(String.format("%s %-20s: %s",dayDateStack.format(cal.getTime()), mainTemperature, highAndLow));

                    table.add(dayDateStack.format(cal.getTime()) +" -    " +description + ": " + highAndLow );
                }


            }

            if(weatherJSONArray.length()>1)
                tab2.populateList(table);

            weatherJSONObj = new JSONObject(result[0]);




            //Object to get the Temperature
            JSONObject tempInfo = weatherJSONObj.getJSONObject("main");
            double currentTemp = tempInfo.getDouble("temp");
            weatherInfoStrings [1] = Long.toString(Math.round(currentTemp)) + "\u00B0";



            //Object gets the location
            weatherInfoStrings[6] = weatherJSONObj.getString("name");

            //Object to get the icon ID, the Main Weather and it's description
            JSONObject weatherInfo = weatherJSONObj.getJSONObject("weather");
            //MAIN
            weatherInfoStrings [2] = weatherInfo.getString("main");
            //DESCRIPTION
            String description = weatherInfo.getString("description");
            weatherInfoStrings [3] = description.substring(0,1).toUpperCase() + description.substring(1);
            //ID
            weatherInfoStrings [5] = weatherInfo.getString("id");




        } catch (JSONException e) {
            e.printStackTrace();
        }

        cal = Calendar.getInstance();
        dayDateStack = new SimpleDateFormat("MMMM d, h:mm a");
        weatherInfoStrings[0]=dayDateStack.format(cal.getTime())+" - "+ weatherInfoStrings[6];

        return weatherInfoStrings;


    }













    //JSON CLEANER For Weather. Current day
    public String[] jsonCleanerWeather(String result) {


        cal = Calendar.getInstance();
        SimpleDateFormat dayDateStack = new SimpleDateFormat("EEE, MMM d");

        String[] weatherInfoStrings = new String[9];
        String locationWeather="";
        try {


            JSONObject weatherJSONObj = new JSONObject(result);

            //Object gets the location
            locationWeather = weatherJSONObj.getString("name");

            //Object to get the icon ID, the Main Weather and it's description
            JSONObject weatherInfo = weatherJSONObj.getJSONObject("weather");
            //MAIN
            weatherInfoStrings [2] = weatherInfo.getString("main");
            //DESCRIPTION
            String description = weatherInfo.getString("description");
            weatherInfoStrings [3] = description.substring(0,1).toUpperCase() + description.substring(1);
            //ID
            weatherInfoStrings [5] = weatherInfo.getString("id");




        } catch (JSONException e) {
            e.printStackTrace();
        }

        cal = Calendar.getInstance();
        dayDateStack = new SimpleDateFormat("MMMM d, h:mm a");
        weatherInfoStrings[0]=dayDateStack.format(cal.getTime())+" - "+ locationWeather;
        weatherInfoStrings[6] = locationWeather;

        return weatherInfoStrings;


    }




    //Gives you an average for the day temperature
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user only cares about full numbers
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "\u00B0/" + roundedLow+"\u00B0";
        return highLowStr;
    }


    @Override
    public void onSaveInstanceState(Bundle bundle){
        super.onSaveInstanceState(bundle);

        //Variables that help build the app
        bundle.putString("Latitude",latitude);
        bundle.putString("Longitude",longitude);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        if(savedInstanceState!=null){

            latitude = (String) savedInstanceState.get("Latitude");
            longitude = (String) savedInstanceState.get("Longitude");
        }


        //Creating the GoogleApiClient for the Locale
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

        // An Adapter returning a Fragment for each of the sections used in the tabs
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //The Tab layout
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        loadPreferences();
    }


    //Sets or deactivates the Notification
    protected void setNotification(String lat, String lon, boolean activeNotification, String unitValue){


        Calendar calen = Calendar.getInstance();
        calen.set(Calendar.MINUTE, 30);
        calen.set(Calendar.HOUR_OF_DAY, 06);
        //Setting the notification

        if(activeNotification) {
            calen.set(Calendar.MINUTE, 0);


            //This intent will lead to the Broadcast receiver
            Intent notificationIntent = new Intent(getApplicationContext(), Notification_receiver.class);

            //Adding Longitute and Latitude
            notificationIntent.putExtra("Latitude", lat);
            notificationIntent.putExtra("Longitude", lon);
            notificationIntent.putExtra("Unit",unitValue);


            //Pending intent for the Notification(Intent) created above)
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 237, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            //Instance of the alarm Manager
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            //Setting the Alarm. RTC_WAKEUP will Go even if the device is sleep, next is when is the alarm going off, next is how often (INTERVAL_DAY is each a day)
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calen.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
        else{
            Intent intent = new Intent(this, Notification_receiver.class);
            PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), 237, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.cancel(sender);
        }
    }




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
            Intent intent = new Intent(this, AppPreferences.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.why_about) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://en.wikipedia.org/wiki/Sun_dog"));
            startActivity(browserIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadPreferences(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        units = sharedPreferences.getString("degree_preference","metric");
        days = sharedPreferences.getString("day_preference","10");
        notificationActive = sharedPreferences.getBoolean("daily_notifications",true);
        locationCurrent = sharedPreferences.getBoolean("location_preference",true);
        if(locationCurrent)
            postalCode = null;
        else
            postalCode = sharedPreferences.getString("postal_code",null);

        /*
        if(weatherAsyncTask!=null)
            refillApp();
        * */


    }
    /*
    CODE THAT MIGHT BE RENDERED USELESS
    private void refillApp(){
        //For some reason constantly calling weatherAsyncTask caused the app to crash
        weatherAsyncTask=null;
        weatherAsyncTask = new DownloadTask();

        Log.i("watwat",postalCode);
        //String to receive the code
        String uncleanedJsonCode="";
        try {
            //if we don't have a new Postal Code to try out
            if(postalCode==null)
                uncleanedJsonCode = weatherAsyncTask.execute(latitude,longitude,units,days).get();
            else
                uncleanedJsonCode = weatherAsyncTask.execute(postalCode,units,days).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if(uncleanedJsonCode!="ERROR"){


            String[] weatherInfo = jsonCleaner(uncleanedJsonCode);

            tab1.populateScreen(weatherInfo);
            setNotification(latitude,longitude);

        }
        else{
            Toast.makeText(this, NOT_ONLINE,
                    Toast.LENGTH_LONG).show();
        }


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
                    return "Today";
                case 1:
                    return "Next Days";
            }
            return null;
        }
    }


}
